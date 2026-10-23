#!/usr/bin/env bash
# =============================================================================
#  recette-locale.sh — commandes utiles du parcours SIRH (Bloc 1 → Bloc 4)
#
#  Référence : readme/userflow-test.md
#
#  Usage :
#    ./scripts/recette-locale.sh help
#    ./scripts/recette-locale.sh check
#    ./scripts/recette-locale.sh db-reset --yes
#    ./scripts/recette-locale.sh start
#
#  Le script se place à la racine du projet (dossier parent de scripts/).
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

APP_PORT="${APP_PORT:-8080}"
BASE_URL="${BASE_URL:-http://localhost:$APP_PORT}"
RUN_DIR="${RUN_DIR:-/tmp/opencode/run}"
APP_LOG="${APP_LOG:-/tmp/opencode/sirh-run.log}"
APP_PID_FILE="${APP_PID_FILE:-/tmp/opencode/sirh-run.pid}"

WAR="target/sirh-0.0.1-SNAPSHOT.war"

# Ordre canonique des migrations de la branche integration/bloc1-4.
# Complet : schéma + données de test (offres, candidats, contrats, onboarding...)
MIGRATIONS=(
  migrations/bloc1.sql
  migrations/bloc1_teste.sql        # SEED : comptes (RH, DG, FINANCE, EMPLOYE)
  migrations/b2a_recrutement.sql
  migrations/b2a_donnees_test.sql   # SEED : offres, demandes, candidats
  migrations/bloc2_test_technique.sql
  migrations/bloc3_faneva.sql
  migrations/bloc3_test.sql         # SEED Bloc 3
  migrations/bloc4.sql
  migrations/bloc4_teste.sql         # SEED Bloc 4 (contrat, employé, onboarding)
)
# Schéma + comptes uniquement (sans données de test : pas d'offres, pas de candidats, pas de contrat)
SCHEMA_WITH_ACCOUNTS_ONLY=(
  migrations/bloc1.sql
  migrations/bloc1_teste.sql
  migrations/b2a_recrutement.sql
  migrations/bloc2_test_technique.sql
  migrations/bloc3_faneva.sql
  migrations/bloc4.sql
)

ASSUME_YES=0

usage() {
  cat <<'EOF'
recette-locale.sh — commandes utiles du parcours SIRH

Usage :
  ./scripts/recette-locale.sh <commande> [options]

Options :
  -y, --yes            Ne pas demander de confirmation (commandes destructives)

Diagnostic :
  check                Vérifie Java, Maven, PostgreSQL, Git, curl
  status               État de l'application (port + HTTP)
  accounts             Affiche les comptes de test
  login-curl <role> <email> <mdp>
                       Teste une connexion et affiche le cookie de session

Base de données :
  db-create            Crée la base si elle n'existe pas
  db-init              Applique toutes les migrations dans l'ordre
  db-reset             Supprime, recrée puis applique toutes les migrations (avec données de test)
  db-reset-empty       Même chose mais sans données de test : schéma + comptes uniquement (pas d'offres, pas de candidats, pas de contrats)
  db-shell             Ouvre psql sur la base
  db-query <sql>       Exécute une requête SQL
  db-counts            Compte les lignes des tables principales

Build et exécution :
  build                ./mvnw clean package -DskipTests
  test                 ./mvnw clean test
  run                  Démarrage au premier plan (mvn spring-boot:run)
  start                Build + WAR copié hors des chemins avec espace,
                       démarrage en arrière-plan puis attente de disponibilité
  stop                 Arrête l'application
  logs                 Suit le log de l'application démarrée avec start

Recette :
  smoke                Connexion RH + contrôle HTTP des écrans principaux

Exemples :
  ./scripts/recette-locale.sh db-reset --yes
  ./scripts/recette-locale.sh start
  ./scripts/recette-locale.sh smoke
  ./scripts/recette-locale.sh stop
EOF
}

confirm() {
  [[ "$ASSUME_YES" == "1" ]] && return 0
  local reponse
  read -r -p "$1 [oui/N] " reponse || return 1
  [[ "$reponse" =~ ^[oOyY]([uU][iI])?$ ]]
}

need() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Commande requise absente : $1" >&2
    return 1
  }
}

psql_db() {
  psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" \
    -v ON_ERROR_STOP=1 "$@"
}

psql_admin() {
  psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres "$@"
}

db_exists() {
  psql_admin -tAc "SELECT 1 FROM pg_database WHERE datname = '$DB_NAME'" 2>/dev/null \
    | grep -qx 1
}

port_pid() {
  if command -v lsof >/dev/null 2>&1; then
    lsof -tiTCP:"$APP_PORT" -sTCP:LISTEN 2>/dev/null | head -n 1 || true
  else
    ss -ltnp "sport = :$APP_PORT" 2>/dev/null \
      | sed -n 's/.*pid=\([0-9]\+\).*/\1/p' | head -n 1 || true
  fi
}

http_code() {
  curl -s -o /dev/null -w '%{http_code}' --max-time 30 "$1" 2>/dev/null || true
}

# ---------------------------------------------------------------- diagnostic

check_cmd() {
  local manquant=0
  for cmd in java psql pg_isready git curl; do
    if command -v "$cmd" >/dev/null 2>&1; then
      echo "OK        $cmd"
    else
      echo "MANQUANT  $cmd"
      manquant=1
    fi
  done
  if [[ -x "$PROJECT_DIR/mvnw" ]]; then
    echo "OK        ./mvnw"
  else
    echo "MANQUANT  ./mvnw"
    manquant=1
  fi
  echo
  pg_isready -h "$DB_HOST" -p "$DB_PORT" 2>&1 || true
  if db_exists 2>/dev/null; then
    echo "Base $DB_NAME : présente"
  else
    echo "Base $DB_NAME : absente (lancer db-create)"
  fi
  return "$manquant"
}

status_cmd() {
  echo "Projet   : $PROJECT_DIR"
  echo "URL      : $BASE_URL/connexion"
  echo "Base     : $DB_NAME @ $DB_HOST:$DB_PORT"

  local pid
  pid="$(port_pid)"
  if [[ -n "$pid" ]]; then
    echo "Port     : $APP_PORT (PID $pid)"
    ps -p "$pid" -o pid=,cmd= 2>/dev/null || true
  else
    echo "Port     : $APP_PORT libre"
  fi

  local code
  code="$(http_code "$BASE_URL/connexion")"
  echo "HTTP     : ${code:-aucune réponse}"
}

accounts_cmd() {
  cat <<'EOF'
Comptes de test :

  EMPLOYEE : henika.rasoa@entreprise.mg  / EMPLOYEpassword123
  RH       : marie.rabe@entreprise.mg    / RHpassword123
  FINANCE  : paul.andria@entreprise.mg   / FINANCEpassword123
  DG       : jean.rakoto@entreprise.mg   / DGpassword123

  CANDIDAT : identifiant CANDx + mot de passe temporaire
             (affiché par la RH après « Créer l'accès » sur /rh/tests)
EOF
}

# ------------------------------------------------------------------- base

db_create_cmd() {
  need pg_isready || return 1
  pg_isready -h "$DB_HOST" -p "$DB_PORT" >/dev/null 2>&1 || {
    echo "PostgreSQL ne répond pas sur $DB_HOST:$DB_PORT." >&2
    return 1
  }
  if db_exists; then
    echo "La base $DB_NAME existe déjà."
    return 0
  fi
  createdb -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME"
  echo "Base $DB_NAME créée."
}

apply_migrations() {
  local fichier
  if [[ "${1:-}" == "empty" ]]; then
    for fichier in "${SCHEMA_WITH_ACCOUNTS_ONLY[@]}"; do
      [[ -f "$fichier" ]] || { echo "Migration absente : $fichier" >&2; return 1; }
      echo "--- $fichier"
      psql_db -q -f "$fichier"
    done
  else
    for fichier in "${MIGRATIONS[@]}"; do
      [[ -f "$fichier" ]] || { echo "Migration absente : $fichier" >&2; return 1; }
      echo "--- $fichier"
      psql_db -q -f "$fichier"
    done
  fi
}

db_init_cmd() {
  need pg_isready || return 1
  pg_isready -h "$DB_HOST" -p "$DB_PORT" >/dev/null 2>&1 || {
    echo "PostgreSQL ne répond pas sur $DB_HOST:$DB_PORT." >&2
    return 1
  }
  db_exists || db_create_cmd
  apply_migrations
  echo "Migrations appliquées sur $DB_NAME."
}

db_reset_cmd() {
  need pg_isready || return 1
  confirm "Supprimer et recréer la base $DB_NAME ?" || {
    echo "Annulé."
    return 0
  }
  dropdb -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" --if-exists "$DB_NAME"
  createdb -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME"
  apply_migrations
  echo "Base $DB_NAME réinitialisée."
}

db_reset_empty_cmd() {
  need pg_isready || return 1
  confirm "Supprimer et recréer la base $DB_NAME (sans données de test, comptes uniquement) ?" || {
    echo "Annulé."
    return 0
  }
  dropdb -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" --if-exists "$DB_NAME"
  createdb -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME"
  apply_migrations "empty"
  echo "Base $DB_NAME réinitialisée (schéma + comptes uniquement, sans candidats/offres/contrats)."
}

db_shell_cmd() {
  db_exists || {
    echo "La base $DB_NAME n'existe pas." >&2
    return 1
  }
  psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME"
}

db_query_cmd() {
  [[ $# -gt 0 ]] || {
    echo "Usage : db-query \"<sql>\"" >&2
    return 2
  }
  psql_db -c "$*"
}

db_counts_cmd() {
  psql_db -c "
    select 'demande_besoin' as table, count(*) from demande_besoin
    union all select 'offre', count(*) from offre
    union all select 'candidat', count(*) from candidat
    union all select 'candidature', count(*) from candidature
    union all select 'b3_selection', count(*) from b3_selection
    union all select 'contrat', count(*) from contrat
    union all select 'employe', count(*) from employe
    union all select 'onboarding', count(*) from onboarding
    order by 1;"
}

# ------------------------------------------------------------------ build

build_cmd() {
  "$PROJECT_DIR/mvnw" clean package -DskipTests
  echo "WAR : $WAR"
}

test_cmd() {
  "$PROJECT_DIR/mvnw" clean test
}

run_cmd() {
  echo "URL : $BASE_URL — Ctrl+C pour arrêter."
  "$PROJECT_DIR/mvnw" spring-boot:run
}

start_cmd() {
  local pid
  pid="$(port_pid)"
  if [[ -n "$pid" ]]; then
    echo "Le port $APP_PORT est déjà utilisé par PID $pid." >&2
    return 1
  fi

  build_cmd

  # Un chemin contenant un espace casse la lecture des TLD JSTL du WAR exécutable.
  # On copie donc le WAR dans un dossier neutre avant de le lancer.
  mkdir -p "$RUN_DIR"
  cp "$WAR" "$RUN_DIR/sirh.war"

  : > "$APP_LOG"
  (
    cd "$RUN_DIR"
    DB_URL="jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME" \
    DB_USER="$DB_USER" \
    DB_PASSWORD="$DB_PASSWORD" \
    setsid nohup java -jar ./sirh.war > "$APP_LOG" 2>&1 < /dev/null &
    echo $! > "$APP_PID_FILE"
  )

  echo "Démarrage (log : $APP_LOG)…"
  local i code
  for ((i = 0; i < 90; i++)); do
    code="$(http_code "$BASE_URL/connexion")"
    if [[ "$code" == "200" || "$code" == "302" ]]; then
      echo "Application prête : $BASE_URL/connexion"
      return 0
    fi
    sleep 1
  done
  echo "Délai dépassé. Dernières lignes :" >&2
  tail -n 40 "$APP_LOG" >&2 || true
  return 1
}

stop_cmd() {
  local pids=()
  local pid
  pid="$(port_pid)"
  [[ -n "$pid" ]] && pids+=("$pid")
  if [[ -f "$APP_PID_FILE" ]]; then
    pid="$(cat "$APP_PID_FILE" 2>/dev/null || true)"
    [[ -n "$pid" ]] && pids+=("$pid")
  fi

  if [[ ${#pids[@]} -eq 0 ]]; then
    echo "Aucun processus applicatif détecté."
    rm -f "$APP_PID_FILE"
    return 0
  fi

  for pid in "${pids[@]}"; do
    kill "$pid" 2>/dev/null || true
  done
  sleep 2
  for pid in "${pids[@]}"; do
    kill -0 "$pid" 2>/dev/null && kill -9 "$pid" 2>/dev/null || true
  done
  rm -f "$APP_PID_FILE"
  echo "Application arrêtée."
}

logs_cmd() {
  [[ -f "$APP_LOG" ]] || {
    echo "Aucun log : $APP_LOG" >&2
    return 1
  }
  tail -n 120 -f "$APP_LOG"
}

# --------------------------------------------------------------- recette

login_curl_cmd() {
  [[ $# -eq 3 ]] || {
    echo "Usage : login-curl <role> <email> <mot_de_passe>" >&2
    return 2
  }
  local role="$1" email="$2" mdp="$3"
  local cookies
  cookies="$(mktemp)"
  local page csrf
  page="$(curl -s --max-time 30 -c "$cookies" "$BASE_URL/connexion")"
  csrf="$(printf '%s' "$page" | grep -o 'name="_csrf"[^>]*value="[^"]*"' | head -1 | sed 's/.*value="//;s/".*//')"
  curl -s --max-time 30 -b "$cookies" -c "$cookies" -o /dev/null \
    -w "login=$role -> %{http_code} %{redirect_url}\n" \
    --data-urlencode "login=$role|$email" --data-urlencode "mdp=$mdp" \
    --data-urlencode "_csrf=$csrf" "$BASE_URL/connexion"
  echo "Cookie : $cookies"
}

smoke_cmd() {
  local cookies
  cookies="$(mktemp)"
  local page csrf
  page="$(curl -s --max-time 30 -c "$cookies" "$BASE_URL/connexion")"
  csrf="$(printf '%s' "$page" | grep -o 'name="_csrf"[^>]*value="[^"]*"' | head -1 | sed 's/.*value="//;s/".*//')"

  local login
  login="$(curl -s --max-time 30 -b "$cookies" -c "$cookies" -o /dev/null \
    -w '%{http_code}' --data-urlencode 'login=RH|marie.rabe@entreprise.mg' \
    --data-urlencode 'mdp=RHpassword123' --data-urlencode "_csrf=$csrf" \
    "$BASE_URL/connexion")"
  echo "Connexion RH : $login"
  [[ "$login" == "302" ]] || {
    echo "Connexion RH échouée." >&2
    rm -f "$cookies"
    return 1
  }

  local ecrans=(
    /accueil /rh/demandes /rh/tableau-de-bord
    /recrutement/offres /rh/recrutement/candidatures /rh/recrutement/reserve
    /rh/tests /bloc3/selections
    /rh/contrats /rh/contrats/echeances /rh/salaries /rh/affectations
    /rh/onboardings /rh/onboardings/modeles /rh/documents /rh/formalites
    /mes-taches
  )
  local echecs=0 url code
  for url in "${ecrans[@]}"; do
    code="$(curl -s --max-time 30 -b "$cookies" -o /dev/null -w '%{http_code}' "$BASE_URL$url")"
    printf '%-42s %s\n' "$url" "$code"
    [[ "$code" == "200" ]] || echecs=$((echecs + 1))
  done
  rm -f "$cookies"

  echo
  if [[ "$echecs" -eq 0 ]]; then
    echo "Smoke OK : tous les écrans répondent 200."
  else
    echo "Smoke KO : $echecs écran(s) en erreur." >&2
    return 1
  fi
}

# ------------------------------------------------------------- point d'entrée

ARGS=()
for arg in "$@"; do
  case "$arg" in
    -y|--yes) ASSUME_YES=1 ;;
    *) ARGS+=("$arg") ;;
  esac
done
set -- ${ARGS[@]+"${ARGS[@]}"}

COMMAND="${1:-help}"
[[ $# -gt 0 ]] && shift

case "$COMMAND" in
  help|-h|--help) usage ;;
  check|doctor)   check_cmd ;;
  status)         status_cmd ;;
  accounts)       accounts_cmd ;;
  login-curl)     login_curl_cmd "$@" ;;
  db-create)      db_create_cmd ;;
  db-init)        db_init_cmd ;;
  db-reset)       db_reset_cmd ;;
  db-reset-empty) db_reset_empty_cmd ;;
  db-shell)       db_shell_cmd ;;
  db-query|query) db_query_cmd "$@" ;;
  db-counts)      db_counts_cmd ;;
  build|package)  build_cmd ;;
  test|tests)     test_cmd ;;
  run)            run_cmd ;;
  start)          start_cmd ;;
  stop)           stop_cmd ;;
  logs|log)       logs_cmd ;;
  smoke)          smoke_cmd ;;
  *)
    echo "Commande inconnue : $COMMAND" >&2
    usage
    exit 2
    ;;
esac
