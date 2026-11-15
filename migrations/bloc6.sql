-- =====================================================================
-- BLOC 6 — Temps de travail et pointage (voir readme/Bloc6.md)
-- À appliquer APRÈS bloc5.sql. Script idempotent (rejouable).
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Régimes et modèles horaires (§5 — RG-RT)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS modele_horaire (
    id_modele_horaire         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code                      VARCHAR(30) NOT NULL UNIQUE,
    libelle                   VARCHAR(150) NOT NULL,
    heure_debut               TIME NOT NULL,
    heure_fin                 TIME NOT NULL,
    duree_pause_minutes       INT NOT NULL DEFAULT 0 CHECK (duree_pause_minutes >= 0),
    tolerance_retard_minutes  INT NOT NULL DEFAULT 10 CHECK (tolerance_retard_minutes >= 0),
    tolerance_depart_minutes  INT NOT NULL DEFAULT 10 CHECK (tolerance_depart_minutes >= 0),
    est_nuit                  BOOLEAN NOT NULL DEFAULT FALSE,
    actif                     BOOLEAN NOT NULL DEFAULT TRUE,
    -- RG-RT05 : la fin suit le début, sauf horaire de nuit chevauchant minuit.
    CHECK (est_nuit OR heure_fin > heure_debut)
);

CREATE TABLE IF NOT EXISTS regime_travail (
    id_regime_travail          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code                       VARCHAR(30) NOT NULL UNIQUE,
    libelle                    VARCHAR(150) NOT NULL,
    duree_hebdo_heures         NUMERIC(5,2) NOT NULL CHECK (duree_hebdo_heures > 0),
    -- Jours habituellement travaillés : 1 = lundi … 7 = dimanche.
    jours_travailles           VARCHAR(20) NOT NULL DEFAULT '1,2,3,4,5',
    id_modele_horaire_defaut   INT NOT NULL REFERENCES modele_horaire(id_modele_horaire),
    pointage_obligatoire       BOOLEAN NOT NULL DEFAULT TRUE,
    heures_sup_autorisees      BOOLEAN NOT NULL DEFAULT TRUE,
    actif                      BOOLEAN NOT NULL DEFAULT TRUE
);

-- RG-RT01 / RG-RT04 : régime rattaché au salarié par période, jamais écrasé.
CREATE TABLE IF NOT EXISTS regime_salarie (
    id_regime_salarie   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_employe          INT NOT NULL REFERENCES employe(id_employe),
    id_regime_travail   INT NOT NULL REFERENCES regime_travail(id_regime_travail),
    date_debut          DATE NOT NULL,
    date_fin            DATE,
    CHECK (date_fin IS NULL OR date_fin >= date_debut)
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_regime_salarie_en_cours ON regime_salarie (id_employe) WHERE date_fin IS NULL;

CREATE TABLE IF NOT EXISTS jour_ferie (
    id_jour_ferie  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    jour           DATE NOT NULL UNIQUE,
    libelle        VARCHAR(150) NOT NULL
);

INSERT INTO modele_horaire (code, libelle, heure_debut, heure_fin, duree_pause_minutes, tolerance_retard_minutes, tolerance_depart_minutes, est_nuit) VALUES
    ('JOUR_8H',   'Journée 8 h (8 h – 17 h, pause 1 h)',  '08:00', '17:00', 60, 10, 10, FALSE),
    ('MATIN_6H',  'Matin 6 h – 14 h (pause 30 min)',       '06:00', '14:00', 30, 10, 10, FALSE),
    ('APREM_14H', 'Après-midi 14 h – 22 h (pause 30 min)', '14:00', '22:00', 30, 10, 10, FALSE),
    ('NUIT_22H',  'Nuit 22 h – 6 h (pause 30 min)',        '22:00', '06:00', 30, 10, 10, TRUE),
    ('JOUR_10H',  'Journée longue 7 h – 18 h (pause 1 h)', '07:00', '18:00', 60, 10, 10, FALSE),
    ('DEMI_4H',   'Demi-journée 8 h – 12 h',               '08:00', '12:00', 0,  10, 10, FALSE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO regime_travail (code, libelle, duree_hebdo_heures, jours_travailles, id_modele_horaire_defaut, pointage_obligatoire, heures_sup_autorisees)
SELECT v.code, v.libelle, v.duree, v.jours, m.id_modele_horaire, v.pointage, v.hs
  FROM (VALUES
    ('TEMPS_PLEIN',     'Temps plein 40 h (lundi – vendredi)', 40.00, '1,2,3,4,5', 'JOUR_8H', TRUE,  TRUE),
    ('TEMPS_PARTIEL_50','Temps partiel 50 % (matinées)',        20.00, '1,2,3,4,5', 'DEMI_4H', TRUE,  FALSE),
    ('CADRE_FORFAIT',   'Cadre au forfait (sans pointage)',     40.00, '1,2,3,4,5', 'JOUR_8H', FALSE, FALSE)
  ) AS v(code, libelle, duree, jours, modele, pointage, hs)
  JOIN modele_horaire m ON m.code = v.modele
ON CONFLICT (code) DO NOTHING;

-- Jours fériés de Madagascar (calendrier à confirmer chaque année par le RH).
INSERT INTO jour_ferie (jour, libelle) VALUES
    ('2026-01-01', 'Jour de l''an'),
    ('2026-03-29', 'Commémoration des martyrs de 1947'),
    ('2026-04-06', 'Lundi de Pâques'),
    ('2026-05-01', 'Fête du travail'),
    ('2026-05-14', 'Ascension'),
    ('2026-05-25', 'Lundi de Pentecôte'),
    ('2026-06-26', 'Fête de l''indépendance'),
    ('2026-08-15', 'Assomption'),
    ('2026-11-01', 'Toussaint'),
    ('2026-12-25', 'Noël'),
    ('2027-01-01', 'Jour de l''an')
ON CONFLICT (jour) DO NOTHING;

-- Reprise : tout salarié actif reçoit le régime temps plein depuis son embauche.
INSERT INTO regime_salarie (id_employe, id_regime_travail, date_debut)
SELECT e.id_employe, r.id_regime_travail, COALESCE(e.date_embauche, CURRENT_DATE)
  FROM employe e CROSS JOIN regime_travail r
 WHERE r.code = 'TEMPS_PLEIN' AND e.actif = TRUE
   AND NOT EXISTS (SELECT 1 FROM regime_salarie rs WHERE rs.id_employe = e.id_employe);

-- ---------------------------------------------------------------------
-- 2. Planning (§6 — RG-PL)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS planning (
    id_planning               INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reference                 VARCHAR(30) NOT NULL UNIQUE,
    id_departement            INT NOT NULL REFERENCES departement(id_departement),
    date_debut                DATE NOT NULL,
    date_fin                  DATE NOT NULL,
    statut                    VARCHAR(20) NOT NULL DEFAULT 'BROUILLON'
                               CHECK (statut IN ('BROUILLON','SOUMIS_RH','SOUMIS_FINANCE','ARBITRAGE_DG',
                                                 'REJETE','PUBLIE','MODIFIE','CLOTURE')),
    version                   INT NOT NULL DEFAULT 1,
    id_planning_precedent     INT REFERENCES planning(id_planning),
    impact_budgetaire         BOOLEAN NOT NULL DEFAULT FALSE,
    cout_estime               NUMERIC(14,2) NOT NULL DEFAULT 0,
    motif_rejet               TEXT,
    id_auteur                 INT NOT NULL REFERENCES employe(id_employe),
    date_creation             TIMESTAMP NOT NULL DEFAULT NOW(),
    date_soumission           TIMESTAMP,
    id_valideur_rh            INT REFERENCES employe(id_employe),
    date_validation_rh        TIMESTAMP,
    id_valideur_finance       INT REFERENCES employe(id_employe),
    date_validation_finance   TIMESTAMP,
    id_decideur_dg            INT REFERENCES employe(id_employe),
    date_decision_dg          TIMESTAMP,
    date_publication          TIMESTAMP,
    CHECK (date_fin >= date_debut),
    CHECK (date_fin - date_debut <= 31)
);
CREATE INDEX IF NOT EXISTS idx_planning_departement ON planning (id_departement, date_debut, date_fin);
CREATE INDEX IF NOT EXISTS idx_planning_statut ON planning (statut);

CREATE TABLE IF NOT EXISTS planning_ligne (
    id_planning_ligne     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_planning           INT NOT NULL REFERENCES planning(id_planning) ON DELETE CASCADE,
    id_employe            INT NOT NULL REFERENCES employe(id_employe),
    jour                  DATE NOT NULL,
    type_jour             VARCHAR(10) NOT NULL CHECK (type_jour IN ('TRAVAIL','REPOS','FERIE','CONGE','ABSENCE')),
    id_modele_horaire     INT REFERENCES modele_horaire(id_modele_horaire),
    minutes_prevues       INT NOT NULL DEFAULT 0,
    minutes_sup_prevues   INT NOT NULL DEFAULT 0,
    majore                BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (id_planning, id_employe, jour),          -- RG-PL02
    CHECK (type_jour <> 'TRAVAIL' OR id_modele_horaire IS NOT NULL)
);
CREATE INDEX IF NOT EXISTS idx_planning_ligne_employe ON planning_ligne (id_employe, jour);

-- ---------------------------------------------------------------------
-- 3. Pointage (§7 — RG-PT)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pointage (
    id_pointage          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_employe           INT NOT NULL REFERENCES employe(id_employe),
    horodatage           TIMESTAMP NOT NULL,
    sens                 VARCHAR(10) NOT NULL CHECK (sens IN ('ENTREE','SORTIE')),
    source               VARCHAR(10) NOT NULL CHECK (source IN ('WEB','BADGEUSE','IMPORT','MANUEL')),
    saisi_par_id         INT REFERENCES employe(id_employe),
    motif                TEXT,
    statut               VARCHAR(10) NOT NULL DEFAULT 'VALIDE' CHECK (statut IN ('VALIDE','ANNULE')),
    id_pointage_corrige  INT REFERENCES pointage(id_pointage),
    date_enregistrement  TIMESTAMP NOT NULL DEFAULT NOW(),
    -- RG-PT03 : un pointage manuel a toujours un auteur et un motif.
    CHECK (source <> 'MANUEL' OR (saisi_par_id IS NOT NULL AND motif IS NOT NULL))
);
CREATE INDEX IF NOT EXISTS idx_pointage_employe ON pointage (id_employe, horodatage);

-- RG-PT01 : un pointage brut n'est jamais supprimé ; seul son statut peut passer à ANNULE.
CREATE OR REPLACE FUNCTION pointage_protege() RETURNS trigger AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        RAISE EXCEPTION 'Un pointage ne se supprime pas : annulez-le (RG-PT01)';
    END IF;
    IF NEW.id_employe <> OLD.id_employe OR NEW.horodatage <> OLD.horodatage OR NEW.sens <> OLD.sens
       OR NEW.source <> OLD.source THEN
        RAISE EXCEPTION 'Un pointage ne se modifie pas : créez un pointage correctif (RG-PT01)';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS trg_pointage_protege ON pointage;
CREATE TRIGGER trg_pointage_protege BEFORE UPDATE OR DELETE ON pointage
    FOR EACH ROW EXECUTE FUNCTION pointage_protege();

-- ---------------------------------------------------------------------
-- 4. Journées calculées et anomalies (§8, §9 — RG-TT, RG-AN)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS journee_travail (
    id_journee_travail        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_employe                INT NOT NULL REFERENCES employe(id_employe),
    jour                      DATE NOT NULL,
    id_planning_ligne         INT REFERENCES planning_ligne(id_planning_ligne),
    source_prevision          VARCHAR(10) NOT NULL DEFAULT 'REGIME' CHECK (source_prevision IN ('PLANNING','REGIME')),
    type_jour                 VARCHAR(10) NOT NULL,
    minutes_prevues           INT NOT NULL DEFAULT 0,
    minutes_presence          INT NOT NULL DEFAULT 0,
    minutes_travaillees       INT NOT NULL DEFAULT 0,
    minutes_retard            INT NOT NULL DEFAULT 0,
    minutes_depart_anticipe   INT NOT NULL DEFAULT 0,
    minutes_sup               INT NOT NULL DEFAULT 0,
    minutes_nuit              INT NOT NULL DEFAULT 0,
    minutes_dimanche          INT NOT NULL DEFAULT 0,
    minutes_ferie             INT NOT NULL DEFAULT 0,
    minutes_absence           INT NOT NULL DEFAULT 0,
    statut                    VARCHAR(20) NOT NULL
                               CHECK (statut IN ('CALCULEE','A_REGULARISER','ABSENCE_JUSTIFIEE','REPOS','CLOTUREE')),
    date_calcul               TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (id_employe, jour)
);

CREATE TABLE IF NOT EXISTS anomalie_pointage (
    id_anomalie_pointage  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_employe            INT NOT NULL REFERENCES employe(id_employe),
    jour                  DATE NOT NULL,
    type_anomalie         VARCHAR(30) NOT NULL
                           CHECK (type_anomalie IN ('POINTAGE_MANQUANT_ENTREE','POINTAGE_MANQUANT_SORTIE',
                                  'ABSENCE_NON_JUSTIFIEE','RETARD','DEPART_ANTICIPE','HEURES_NON_AUTORISEES',
                                  'POINTAGE_HORS_PLANNING','POINTAGE_INCOHERENT')),
    detail                TEXT,
    statut                VARCHAR(15) NOT NULL DEFAULT 'OUVERTE'
                           CHECK (statut IN ('OUVERTE','EN_COURS','RESOLUE','CONFIRMEE','CLASSEE')),
    justification         TEXT,
    heure_proposee        TIMESTAMP,
    sens_propose          VARCHAR(10) CHECK (sens_propose IS NULL OR sens_propose IN ('ENTREE','SORTIE')),
    date_justification    TIMESTAMP,
    id_traite_par         INT REFERENCES employe(id_employe),
    date_traitement       TIMESTAMP,
    commentaire_traitement TEXT,
    escaladee             BOOLEAN NOT NULL DEFAULT FALSE,
    date_creation         TIMESTAMP NOT NULL DEFAULT NOW(),
    -- RG-AN08 : un classement sans suite est motivé.
    CHECK (statut <> 'CLASSEE' OR commentaire_traitement IS NOT NULL)
);
CREATE INDEX IF NOT EXISTS idx_anomalie_employe ON anomalie_pointage (id_employe, jour);
CREATE INDEX IF NOT EXISTS idx_anomalie_statut ON anomalie_pointage (statut);

-- ---------------------------------------------------------------------
-- 5. Période de paie et variables (§11 — RG-VP), partagées avec le Bloc 7
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS periode_paie (
    id_periode_paie  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    annee            INT NOT NULL,
    mois             INT NOT NULL CHECK (mois BETWEEN 1 AND 12),
    statut           VARCHAR(10) NOT NULL DEFAULT 'OUVERTE' CHECK (statut IN ('OUVERTE','CLOTUREE')),
    id_cloture_par   INT REFERENCES employe(id_employe),
    date_cloture     TIMESTAMP,
    UNIQUE (annee, mois)
);

CREATE TABLE IF NOT EXISTS variable_paie (
    id_variable_paie  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_periode_paie   INT NOT NULL REFERENCES periode_paie(id_periode_paie),
    id_employe        INT NOT NULL REFERENCES employe(id_employe),
    code              VARCHAR(40) NOT NULL,
    libelle           VARCHAR(150) NOT NULL,
    quantite          NUMERIC(10,2) NOT NULL,
    unite             VARCHAR(10) NOT NULL CHECK (unite IN ('HEURE','JOUR','MINUTE')),
    source            VARCHAR(10) NOT NULL CHECK (source IN ('BLOC6','BLOC7')),
    date_creation     TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (id_periode_paie, id_employe, code)
);

-- ---------------------------------------------------------------------
-- 6. Mise en service : aucun calcul ni anomalie avant cette date (pas de pointage avant le Bloc 6).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS parametre_temps (
    cle     VARCHAR(50) PRIMARY KEY,
    valeur  VARCHAR(100) NOT NULL
);
INSERT INTO parametre_temps (cle, valeur) VALUES ('DATE_MISE_EN_SERVICE', CURRENT_DATE::text)
ON CONFLICT (cle) DO NOTHING;
