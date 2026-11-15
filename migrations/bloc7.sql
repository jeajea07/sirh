-- =====================================================================
-- BLOC 7 — Congés et absences (voir readme/Bloc7.md)
-- À appliquer APRÈS bloc6.sql. Script idempotent (rejouable).
-- =====================================================================

-- Nécessaire à la contrainte d'exclusion (id_employe =, plage &&) — RG-CH05.
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- ---------------------------------------------------------------------
-- 1. Types de congés et d'absences (§5 — RG-TC)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS type_conge (
    id_type_conge               INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code                        VARCHAR(30) NOT NULL UNIQUE,
    libelle                     VARCHAR(150) NOT NULL,
    categorie                   VARCHAR(10) NOT NULL CHECK (categorie IN ('CONGE','ABSENCE')),
    -- Solde consommé (NULL = aucun solde) — RG-TC02.
    type_solde                  VARCHAR(20) CHECK (type_solde IS NULL OR type_solde IN ('CONGE_PAYE','PERMISSION')),
    remuneration                VARCHAR(10) NOT NULL CHECK (remuneration IN ('PAYE','NON_PAYE','INDEMNISE')),
    mode_decompte               VARCHAR(10) NOT NULL DEFAULT 'CALENDAIRE' CHECK (mode_decompte IN ('CALENDAIRE','OUVRABLE','OUVRE')),
    justificatif_obligatoire    BOOLEAN NOT NULL DEFAULT FALSE,
    id_type_justificatif        INT REFERENCES type_document_administratif(id_type_document_administratif),
    delai_justificatif_jours    INT NOT NULL DEFAULT 2,
    delai_prevenance_jours      INT NOT NULL DEFAULT 0,
    duree_max_jours             INT,
    validation_manager          BOOLEAN NOT NULL DEFAULT TRUE,
    assimile_travail            BOOLEAN NOT NULL DEFAULT TRUE,
    urgent                      BOOLEAN NOT NULL DEFAULT FALSE,
    saisie_salarie              BOOLEAN NOT NULL DEFAULT TRUE,
    actif                       BOOLEAN NOT NULL DEFAULT TRUE
);

-- Pièces justificatives propres au Bloc 7 (rangées dans le dossier salarié du Bloc 5).
INSERT INTO type_document_administratif
    (code, libelle, categorie, obligatoire, expiration_requise, duree_validite_jours, delai_alerte_jours, niveau_confidentialite)
VALUES
    ('CERTIFICAT_MEDICAL', 'Certificat médical (arrêt de travail)', 'SANTE', FALSE, FALSE, NULL, 0, 'SECRET'),
    ('JUSTIFICATIF_ABSENCE', 'Justificatif d''absence', 'ADMINISTRATIF', FALSE, FALSE, NULL, 0, 'CONFIDENTIEL')
ON CONFLICT (code) DO NOTHING;

-- Valeurs par défaut à confirmer avec le Code du travail (Loi n° 2003-044) — Bloc7.md §4.
INSERT INTO type_conge (code, libelle, categorie, type_solde, remuneration, mode_decompte, justificatif_obligatoire,
                        id_type_justificatif, delai_prevenance_jours, duree_max_jours, validation_manager,
                        assimile_travail, urgent, saisie_salarie)
SELECT v.code, v.libelle, v.categorie, v.solde, v.remu, v.decompte, v.justif, t.id_type_document_administratif,
       v.prevenance, v.duree_max, v.manager, v.assimile, v.urgent, v.salarie
  FROM (VALUES
    ('CONGE_PAYE',          'Congé payé',                          'CONGE',   'CONGE_PAYE', 'PAYE',      'CALENDAIRE', FALSE, NULL,                   7, NULL, TRUE,  TRUE,  FALSE, TRUE),
    ('PERMISSION',          'Permission exceptionnelle',           'CONGE',   'PERMISSION', 'PAYE',      'CALENDAIRE', TRUE,  'ACTE_ETAT_CIVIL',      0, 3,    TRUE,  TRUE,  TRUE,  TRUE),
    ('MATERNITE',           'Congé de maternité',                  'CONGE',   NULL,         'INDEMNISE', 'CALENDAIRE', TRUE,  'CERTIFICAT_MEDICAL',   0, 98,   FALSE, TRUE,  TRUE,  TRUE),
    ('PATERNITE',           'Congé de paternité',                  'CONGE',   NULL,         'PAYE',      'CALENDAIRE', TRUE,  'ACTE_ETAT_CIVIL',      0, 3,    TRUE,  TRUE,  TRUE,  TRUE),
    ('SANS_SOLDE',          'Congé sans solde',                    'CONGE',   NULL,         'NON_PAYE',  'CALENDAIRE', FALSE, NULL,                   15, NULL, TRUE,  FALSE, FALSE, TRUE),
    ('MALADIE',             'Maladie',                             'ABSENCE', NULL,         'INDEMNISE', 'CALENDAIRE', TRUE,  'CERTIFICAT_MEDICAL',   0, NULL, FALSE, TRUE,  TRUE,  TRUE),
    ('ACCIDENT_TRAVAIL',    'Accident du travail',                 'ABSENCE', NULL,         'INDEMNISE', 'CALENDAIRE', TRUE,  'JUSTIFICATIF_ABSENCE', 0, NULL, FALSE, TRUE,  TRUE,  FALSE),
    ('ABSENCE_AUTORISEE',   'Absence autorisée non payée',         'ABSENCE', NULL,         'NON_PAYE',  'CALENDAIRE', FALSE, NULL,                   0, NULL, TRUE,  FALSE, TRUE,  TRUE),
    ('ABSENCE_INJUSTIFIEE', 'Absence injustifiée',                 'ABSENCE', NULL,         'NON_PAYE',  'CALENDAIRE', FALSE, NULL,                   0, NULL, FALSE, FALSE, TRUE,  FALSE)
  ) AS v(code, libelle, categorie, solde, remu, decompte, justif, justif_code, prevenance, duree_max, manager, assimile, urgent, salarie)
  LEFT JOIN type_document_administratif t ON t.code = v.justif_code
ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------------------------
-- 2. Droits et mouvements de solde (§6 — RG-SC)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS droit_conge (
    id_droit_conge      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_employe          INT NOT NULL REFERENCES employe(id_employe),
    type_solde          VARCHAR(20) NOT NULL CHECK (type_solde IN ('CONGE_PAYE','PERMISSION')),
    periode_reference   DATE NOT NULL,
    jours               NUMERIC(6,2) NOT NULL CHECK (jours >= 0),
    origine             VARCHAR(25) NOT NULL
                         CHECK (origine IN ('ACQUISITION_MENSUELLE','ATTRIBUTION_ANNUELLE','REPRISE')),
    date_expiration     DATE,
    date_creation       TIMESTAMP NOT NULL DEFAULT NOW(),
    -- Une seule acquisition par salarié, solde, période et origine : le traitement est rejouable.
    UNIQUE (id_employe, type_solde, periode_reference, origine)
);

-- RG-SC03 : le solde est la somme des mouvements (crédit +, débit −, expiration −, ajustement ±).
CREATE TABLE IF NOT EXISTS mouvement_solde (
    id_mouvement_solde  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_employe          INT NOT NULL REFERENCES employe(id_employe),
    type_solde          VARCHAR(20) NOT NULL CHECK (type_solde IN ('CONGE_PAYE','PERMISSION')),
    type_mouvement      VARCHAR(15) NOT NULL CHECK (type_mouvement IN ('CREDIT','DEBIT','EXPIRATION','AJUSTEMENT')),
    jours               NUMERIC(6,2) NOT NULL,
    id_droit_conge      INT REFERENCES droit_conge(id_droit_conge),
    id_demande_conge    INT,
    id_auteur           INT REFERENCES employe(id_employe),
    motif               TEXT,
    date_mouvement      TIMESTAMP NOT NULL DEFAULT NOW(),
    CHECK (type_mouvement <> 'CREDIT' OR jours > 0),
    CHECK (type_mouvement NOT IN ('DEBIT','EXPIRATION') OR jours < 0),
    -- RG-SC04 : un ajustement est fait par le RH, motivé.
    CHECK (type_mouvement <> 'AJUSTEMENT' OR (id_auteur IS NOT NULL AND motif IS NOT NULL AND jours <> 0))
);
CREATE INDEX IF NOT EXISTS idx_mouvement_solde_employe ON mouvement_solde (id_employe, type_solde);

-- ---------------------------------------------------------------------
-- 3. Demandes de congé (§7, §8 — RG-DC, RG-CH)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS demande_conge (
    id_demande_conge         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reference                VARCHAR(30) NOT NULL UNIQUE,
    id_employe               INT NOT NULL REFERENCES employe(id_employe),
    id_type_conge            INT NOT NULL REFERENCES type_conge(id_type_conge),
    date_debut               DATE NOT NULL,
    demi_journee_debut       VARCHAR(10) NOT NULL DEFAULT 'MATIN' CHECK (demi_journee_debut IN ('MATIN','APRES_MIDI')),
    date_fin                 DATE NOT NULL,
    demi_journee_fin         VARCHAR(10) NOT NULL DEFAULT 'APRES_MIDI' CHECK (demi_journee_fin IN ('MATIN','APRES_MIDI')),
    nb_jours                 NUMERIC(5,1) NOT NULL CHECK (nb_jours > 0),
    jours_pris               NUMERIC(5,1),
    motif                    TEXT,
    id_justificatif          INT REFERENCES document(id_document),
    statut                   VARCHAR(20) NOT NULL DEFAULT 'SOUMISE'
                              CHECK (statut IN ('BROUILLON','SOUMISE','VALIDEE_MANAGER','VALIDEE','REFUSEE',
                                                'ANNULEE','EN_COURS','PRISE','INTERROMPUE')),
    id_manager               INT REFERENCES employe(id_employe),
    id_valideur_manager      INT REFERENCES employe(id_employe),
    date_validation_manager  TIMESTAMP,
    commentaire_manager      TEXT,
    id_valideur_rh           INT REFERENCES employe(id_employe),
    date_validation_rh       TIMESTAMP,
    commentaire_rh           TEXT,
    motif_refus              TEXT,
    motif_annulation         TEXT,
    escaladee                BOOLEAN NOT NULL DEFAULT FALSE,
    id_saisi_par             INT NOT NULL REFERENCES employe(id_employe),
    date_creation            TIMESTAMP NOT NULL DEFAULT NOW(),
    -- Plage en demi-journées (MATIN = 1re moitié, APRES_MIDI = 2de) pour le contrôle à la demi-journée (RG-CH02).
    plage                    INT4RANGE GENERATED ALWAYS AS (
                                 int4range((date_debut - DATE '2000-01-01') * 2 + CASE WHEN demi_journee_debut = 'APRES_MIDI' THEN 1 ELSE 0 END,
                                           (date_fin - DATE '2000-01-01') * 2 + CASE WHEN demi_journee_fin = 'MATIN' THEN 1 ELSE 2 END)) STORED,
    CHECK (date_fin >= date_debut),
    CHECK (date_fin > date_debut OR NOT (demi_journee_debut = 'APRES_MIDI' AND demi_journee_fin = 'MATIN')),  -- RG-DC02
    CHECK (statut <> 'REFUSEE' OR motif_refus IS NOT NULL)                                                    -- RG-DC11
);
-- RG-CH01 / RG-CH05 : aucun chevauchement entre demandes actives, garanti par la base.
ALTER TABLE demande_conge DROP CONSTRAINT IF EXISTS ex_demande_conge_chevauchement;
ALTER TABLE demande_conge ADD CONSTRAINT ex_demande_conge_chevauchement
    EXCLUDE USING gist (id_employe WITH =, plage WITH &&)
    WHERE (statut IN ('SOUMISE','VALIDEE_MANAGER','VALIDEE','EN_COURS','PRISE','INTERROMPUE'));
CREATE INDEX IF NOT EXISTS idx_demande_conge_statut ON demande_conge (statut);
CREATE INDEX IF NOT EXISTS idx_demande_conge_manager ON demande_conge (id_manager, statut);

ALTER TABLE mouvement_solde DROP CONSTRAINT IF EXISTS fk_mouvement_demande;
ALTER TABLE mouvement_solde ADD CONSTRAINT fk_mouvement_demande
    FOREIGN KEY (id_demande_conge) REFERENCES demande_conge(id_demande_conge);

-- ---------------------------------------------------------------------
-- 4. Absences (§9 — RG-AB)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS absence (
    id_absence                INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_employe                INT NOT NULL REFERENCES employe(id_employe),
    id_type_conge             INT NOT NULL REFERENCES type_conge(id_type_conge),
    date_debut                DATE NOT NULL,
    date_fin                  DATE NOT NULL,
    origine                   VARCHAR(20) NOT NULL CHECK (origine IN ('DECLAREE_SALARIE','SAISIE_RH','CONSTATEE_POINTAGE')),
    statut                    VARCHAR(25) NOT NULL DEFAULT 'DECLAREE'
                               CHECK (statut IN ('DECLAREE','EN_ATTENTE_JUSTIFICATIF','JUSTIFIEE','INJUSTIFIEE',
                                                 'CONVERTIE','ANNULEE')),
    id_justificatif           INT REFERENCES document(id_document),
    date_limite_justificatif  DATE,
    id_demande_conge          INT REFERENCES demande_conge(id_demande_conge),
    id_anomalie_pointage      INT REFERENCES anomalie_pointage(id_anomalie_pointage),
    commentaire               TEXT,
    decision_commentaire      TEXT,
    id_saisi_par              INT NOT NULL REFERENCES employe(id_employe),
    id_decideur               INT REFERENCES employe(id_employe),
    date_decision             TIMESTAMP,
    date_creation             TIMESTAMP NOT NULL DEFAULT NOW(),
    plage                     DATERANGE GENERATED ALWAYS AS (daterange(date_debut, date_fin, '[]')) STORED,
    CHECK (date_fin >= date_debut)
);
ALTER TABLE absence DROP CONSTRAINT IF EXISTS ex_absence_chevauchement;
ALTER TABLE absence ADD CONSTRAINT ex_absence_chevauchement
    EXCLUDE USING gist (id_employe WITH =, plage WITH &&)
    WHERE (statut IN ('DECLAREE','EN_ATTENTE_JUSTIFICATIF','JUSTIFIEE','INJUSTIFIEE'));
CREATE INDEX IF NOT EXISTS idx_absence_statut ON absence (statut);

-- ---------------------------------------------------------------------
-- 5. Reprise de l'existant
-- ---------------------------------------------------------------------
-- Congés payés : droits de l'année en cours (2,5 j par mois complet depuis le 1er janvier ou l'embauche).
-- Les années antérieures sont réputées soldées par l'ancien système ; un écart se corrige par AJUSTEMENT.
WITH debut AS (
    -- Premier mois complet acquis : le mois d'embauche si elle a lieu le 1er, sinon le mois suivant.
    SELECT e.id_employe,
           GREATEST(date_trunc('year', CURRENT_DATE)::date,
                    CASE WHEN EXTRACT(DAY FROM e.date_embauche) = 1 THEN e.date_embauche
                         ELSE (date_trunc('month', e.date_embauche) + INTERVAL '1 month')::date END) AS premier_mois
      FROM employe e
     WHERE e.actif = TRUE AND e.date_embauche IS NOT NULL
), mois AS (
    SELECT id_employe,
           (EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM premier_mois)) * 12
           + EXTRACT(MONTH FROM CURRENT_DATE) - EXTRACT(MONTH FROM premier_mois) AS nb
      FROM debut
)
INSERT INTO droit_conge (id_employe, type_solde, periode_reference, jours, origine)
SELECT id_employe, 'CONGE_PAYE', (date_trunc('month', CURRENT_DATE) - INTERVAL '1 month')::date, 2.5 * nb, 'REPRISE'
  FROM mois WHERE nb > 0
ON CONFLICT DO NOTHING;
-- Permissions exceptionnelles : allocation annuelle (RG-SC11, 10 jours à confirmer).
INSERT INTO droit_conge (id_employe, type_solde, periode_reference, jours, origine)
SELECT e.id_employe, 'PERMISSION', date_trunc('year', CURRENT_DATE)::date, 10, 'ATTRIBUTION_ANNUELLE'
  FROM employe e WHERE e.actif = TRUE
ON CONFLICT DO NOTHING;
-- Chaque droit créé ci-dessus produit son mouvement de crédit.
INSERT INTO mouvement_solde (id_employe, type_solde, type_mouvement, jours, id_droit_conge, motif)
SELECT d.id_employe, d.type_solde, 'CREDIT', d.jours, d.id_droit_conge,
       CASE d.origine WHEN 'REPRISE' THEN 'Reprise des droits de l''année' ELSE 'Allocation annuelle' END
  FROM droit_conge d
 WHERE d.jours > 0
   AND NOT EXISTS (SELECT 1 FROM mouvement_solde m WHERE m.id_droit_conge = d.id_droit_conge);
