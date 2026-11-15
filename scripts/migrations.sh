#!/usr/bin/env bash
# =============================================================================
#  migrations.sh — applique les migrations SIRH de façon sûre et suivie
#
#  Ordre : migrations/ordre-migrations.txt (source unique, partagée avec recette-locale.sh)
#  Suivi : table schema_migration (fichier, type, empreinte, date, mode)
#
#  Usage :
#    ./scripts/migrations.sh status
#    ./scripts/migrations.sh apply                      # n'applique que ce qui manque
#    ./scripts/migrations.sh apply --avec-donnees-test  # + jeux de démonstration
#    ./scripts/migrations.sh baseline                   # base existante : enregistre sans exécuter
#    ./scripts/migrations.sh reset --yes                # repart de zéro (efface la base)
#
#  Variables : DB_NAME DB_HOST DB_PORT DB_USER DB_PASSWORD (défaut : gestionrh@localhost:5432, postgres/postgres)
# =============================================================================

set -euo pipefail

PROJECT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

DB_NAME="${DB_NAME:-gestionrh}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-postgres}"
export PGPASSWORD="$DB_PASSWORD"

ORDRE="migrations/ordre-migrations.txt"
AVEC_TEST=0
SANS_TEST=0
ASSUME_YES=0

usage() {
  cat <<'EOF'
migrations.sh — migrations SIRH suivies (table schema_migration)

Commandes :
  status                     État de chaque migration : appliquée, détectée, à appliquer, modifiée
  apply                      Crée la base si besoin et applique, dans l'ordre, les migrations manquantes
                             (schéma + comptes/référentiels ; les jeux de test sont ignorés)
  apply --avec-donnees-test  Idem, en appliquant aussi les jeux de démonstration manquants
  baseline                   Base existante créée sans ce script : reconnaît les migrations déjà
                             présentes et les enregistre SANS les exécuter
  reset --yes                Supprime la base, la recrée et applique tout AVEC les données de test
  reset --yes --sans-donnees-test
                             Idem, schéma + comptes uniquement
  list                       Affiche l'ordre canonique

Options : -y/--yes (pas de confirmation), --avec-donnees-test, --sans-donnees-test

Sécurité :
  - chaque fichier s'exécute dans UNE transaction (psql -1) : en cas d'erreur, rien n'est appliqué ;
  - un fichier « destructif » (DROP TABLE … CASCADE) n'est jamais exécuté si ses tables existent déjà ;
  - sur une base sans suivi mais déjà remplie, « apply » fait d'abord un baseline automatique.
EOF
}

# ------------------------------------------------------------------ outils

psql_db()    { psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 "$@"; }
psql_admin() { psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres -v ON_ERROR_STOP=1 "$@"; }

confirm() {
  [[ "$ASSUME_YES" == "1" ]] && return 0
  local r
  read -r -p "$1 [oui/N] " r || return 1
  [[ "$r" =~ ^[oOyY]([uU][iI])?$ ]]
}

verifier_serveur() {
  command -v psql >/dev/null 2>&1 || { echo "psql est introuvable (installer le client PostgreSQL)." >&2; exit 1; }
  pg_isready -h "$DB_HOST" -p "$DB_PORT" >/dev/null 2>&1 || {
    echo "PostgreSQL ne répond pas sur $DB_HOST:$DB_PORT." >&2; exit 1; }
}

base_existe() {
  psql_admin -tAc "SELECT 1 FROM pg_database WHERE datname = '$DB_NAME'" 2>/dev/null | grep -qx 1
}

creer_base_si_absente() {
  if ! base_existe; then
    createdb -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME"
    echo "Base $DB_NAME créée."
  fi
}

# Lit l'ordre canonique : une ligne « fichier|type|destructif|marqueur » par migration.
lire_ordre() {
  [[ -f "$ORDRE" ]] || { echo "Fichier d'ordre absent : $ORDRE" >&2; exit 1; }
  grep -vE '^\s*(#|$)' "$ORDRE" | while IFS='|' read -r f t d m; do
    printf '%s|%s|%s|%s\n' "$(echo "$f" | xargs)" "$(echo "$t" | xargs)" "$(echo "$d" | xargs)" "$(echo "$m" | sed 's/^ *//;s/ *$//')"
  done
}

empreinte() { md5sum "migrations/$1" | cut -d' ' -f1; }

creer_suivi() {
  psql_db -q -c "CREATE TABLE IF NOT EXISTS schema_migration (
      fichier      VARCHAR(100) PRIMARY KEY,
      type         VARCHAR(10)  NOT NULL,
      empreinte    VARCHAR(32)  NOT NULL,
      mode         VARCHAR(10)  NOT NULL CHECK (mode IN ('EXECUTEE','DETECTEE')),
      applique_le  TIMESTAMP    NOT NULL DEFAULT NOW(),
      duree_ms     INT
  );"
}

suivi_existe() { [[ "$(psql_db -tAc "SELECT to_regclass('public.schema_migration') IS NOT NULL")" == "t" ]]; }

# Valeur enregistrée pour un fichier (empreinte ou vide).
enregistree() { psql_db -tAc "SELECT empreinte FROM schema_migration WHERE fichier = '$1'" 2>/dev/null || true; }

# Marqueur vrai ? (une erreur SQL — table absente — vaut « non »).
marqueur_vrai() { [[ "$(psql_db -tAc "SELECT ($1)" 2>/dev/null || true)" == "t" ]]; }

base_vide() { [[ "$(psql_db -tAc "SELECT count(*) FROM pg_tables WHERE schemaname = 'public' AND tablename <> 'schema_migration'")" == "0" ]]; }

enregistrer() {
  psql_db -q -c "INSERT INTO schema_migration (fichier, type, empreinte, mode, duree_ms)
                 VALUES ('$1', '$2', '$3', '$4', ${5:-NULL})
                 ON CONFLICT (fichier) DO UPDATE SET empreinte = EXCLUDED.empreinte, mode = EXCLUDED.mode,
                   applique_le = NOW(), duree_ms = EXCLUDED.duree_ms;"
}

# ---------------------------------------------------------------- commandes

list_cmd() {
  printf '%-3s %-28s %-7s %s\n' "#" "FICHIER" "TYPE" "DESTRUCTIF"
  local i=0
  while IFS='|' read -r f t d m; do
    i=$((i + 1)); printf '%-3s %-28s %-7s %s\n' "$i" "$f" "$t" "$d"
  done < <(lire_ordre)
}

status_cmd() {
  verifier_serveur
  if ! base_existe; then echo "La base $DB_NAME n'existe pas : ./scripts/migrations.sh apply la créera."; return 0; fi
  local suivi=0; suivi_existe && suivi=1
  [[ "$suivi" == "1" ]] || echo "(pas encore de table schema_migration : état déduit des marqueurs)"
  printf '%-28s %-7s %s\n' "FICHIER" "TYPE" "ÉTAT"
  local a_faire=0 tests=0
  while IFS='|' read -r f t d m; do
    local etat e
    e=""; [[ "$suivi" == "1" ]] && e="$(enregistree "$f")"
    if [[ -n "$e" ]]; then
      if [[ "$e" == "$(empreinte "$f")" ]]; then etat="appliquée"; else etat="appliquée — FICHIER MODIFIÉ depuis (ne sera pas rejoué)"; fi
    elif marqueur_vrai "$m"; then
      etat="présente (détectée, non enregistrée → baseline)"
    else
      if [[ "$t" == "test" ]]; then
        etat="non chargée (données de test : apply --avec-donnees-test)"; tests=$((tests + 1))
      else
        etat="À APPLIQUER"; a_faire=$((a_faire + 1))
      fi
    fi
    printf '%-28s %-7s %s\n' "$f" "$t" "$etat"
  done < <(lire_ordre)
  echo; echo "$a_faire migration(s) en attente ; $tests jeu(x) de données de test non chargé(s)."
}

baseline_cmd() {
  verifier_serveur
  base_existe || { echo "La base $DB_NAME n'existe pas." >&2; exit 1; }
  creer_suivi
  local n=0
  while IFS='|' read -r f t d m; do
    [[ -n "$(enregistree "$f")" ]] && continue
    if marqueur_vrai "$m"; then
      enregistrer "$f" "$t" "$(empreinte "$f")" "DETECTEE"
      echo "détectée   $f"; n=$((n + 1))
    fi
  done < <(lire_ordre)
  echo "$n migration(s) enregistrée(s) comme déjà présentes."
}

apply_cmd() {
  verifier_serveur
  creer_base_si_absente
  # Base remplie sans suivi : on reconnaît d'abord l'existant pour ne rien rejouer.
  if ! suivi_existe && ! base_vide; then
    echo "Base existante sans suivi : reconnaissance des migrations déjà présentes…"
    baseline_cmd
    echo
  fi
  creer_suivi
  local n=0 ignorees=0
  while IFS='|' read -r f t d m; do
    [[ -f "migrations/$f" ]] || { echo "Migration absente : migrations/$f" >&2; exit 1; }
    [[ -n "$(enregistree "$f")" ]] && continue
    if [[ "$t" == "test" && "$AVEC_TEST" != "1" ]]; then
      ignorees=$((ignorees + 1)); continue
    fi
    if marqueur_vrai "$m"; then
      # Déjà présente sans être enregistrée : jamais rejouée (surtout si destructive).
      enregistrer "$f" "$t" "$(empreinte "$f")" "DETECTEE"
      echo "détectée   $f (déjà présente, non rejouée)"; continue
    fi
    echo "--- $f"
    local debut fin
    debut=$(date +%s%3N)
    if ! psql_db -q -1 -f "migrations/$f"; then
      echo "ÉCHEC sur $f : transaction annulée, aucune modification de ce fichier n'est conservée." >&2
      exit 1
    fi
    fin=$(date +%s%3N)
    enregistrer "$f" "$t" "$(empreinte "$f")" "EXECUTEE" "$((fin - debut))"
    n=$((n + 1))
  done < <(lire_ordre)
  echo
  echo "$n migration(s) appliquée(s) sur $DB_NAME."
  [[ "$ignorees" -gt 0 ]] && echo "$ignorees jeu(x) de données de test ignoré(s) (ajouter --avec-donnees-test pour les charger)."
  return 0
}

reset_cmd() {
  verifier_serveur
  confirm "Supprimer DÉFINITIVEMENT la base $DB_NAME et la recréer ?" || { echo "Annulé."; return 0; }
  dropdb -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" --if-exists "$DB_NAME"
  createdb -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME"
  echo "Base $DB_NAME recréée."
  if [[ "$SANS_TEST" == "1" ]]; then AVEC_TEST=0; else AVEC_TEST=1; fi
  apply_cmd
}

# ------------------------------------------------------------- point d'entrée

ARGS=()
for arg in "$@"; do
  case "$arg" in
    -y|--yes) ASSUME_YES=1 ;;
    --avec-donnees-test) AVEC_TEST=1 ;;
    --sans-donnees-test) SANS_TEST=1 ;;
    *) ARGS+=("$arg") ;;
  esac
done
set -- ${ARGS[@]+"${ARGS[@]}"}

case "${1:-help}" in
  help|-h|--help) usage ;;
  list)           list_cmd ;;
  status)         status_cmd ;;
  baseline)       baseline_cmd ;;
  apply)          apply_cmd ;;
  reset)          reset_cmd ;;
  *) echo "Commande inconnue : $1" >&2; usage; exit 2 ;;
esac
