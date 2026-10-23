-- =====================================================================
-- BLOC 2B : compte candidat et test technique, greffes sur le socle 2A.
-- A executer APRES b2a_recrutement.sql (tables candidat, candidature, offre)
-- et AVANT les scripts Bloc 3 et Bloc 4.
--
-- Les identifiants restent des INT pour rester coherents avec le Bloc 2A
-- et avec b3_selection.id_candidature_b2 (INT).
-- =====================================================================

-- ---------------------------------------------------------------------
-- Colonnes additives sur le socle 2A (nullables pour ne rien casser)
-- ---------------------------------------------------------------------
ALTER TABLE candidat ADD COLUMN IF NOT EXISTS identifiant VARCHAR(100);
ALTER TABLE candidat ADD COLUMN IF NOT EXISTS mot_de_passe VARCHAR(255);
ALTER TABLE candidat ADD COLUMN IF NOT EXISTS actif BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE candidat ADD COLUMN IF NOT EXISTS acces_cree BOOLEAN NOT NULL DEFAULT FALSE;
CREATE UNIQUE INDEX IF NOT EXISTS uq_candidat_identifiant ON candidat(identifiant) WHERE identifiant IS NOT NULL;

ALTER TABLE candidature ADD COLUMN IF NOT EXISTS acces_cree BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE candidature ADD COLUMN IF NOT EXISTS note_finale NUMERIC(5,2)
    CHECK (note_finale IS NULL OR (note_finale >= 0 AND note_finale <= 20));

-- ---------------------------------------------------------------------
-- Session de test technique : une par candidature
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS reponse_test_technique CASCADE;
DROP TABLE IF EXISTS test_technique CASCADE;

CREATE TABLE test_technique (
    id_test_technique INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_candidature    INT NOT NULL UNIQUE,
    date_debut        TIMESTAMP NOT NULL DEFAULT NOW(),
    date_fin          TIMESTAMP,
    statut            VARCHAR(30) NOT NULL DEFAULT 'PROGRAMME'
                      CHECK (statut IN ('PROGRAMME','EN_COURS','RELANCE','CORRIGE','ADMIS','REFUSE','ABSENT')),
    seuil_note        NUMERIC(5,2) NOT NULL DEFAULT 0,
    note_generale     NUMERIC(5,2) NOT NULL DEFAULT 0,
    relance           BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature) ON DELETE CASCADE
);

CREATE INDEX idx_test_candidature ON test_technique(id_candidature);

CREATE TABLE reponse_test_technique (
    id_reponse       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_test_technique INT NOT NULL,
    question_ref     VARCHAR(100) NOT NULL,
    type_question    VARCHAR(30) NOT NULL DEFAULT 'CHOIX'
                     CHECK (type_question IN ('CHOIX','QCM','LIBRE')),
    reponse_textuelle TEXT,
    note             NUMERIC(5,2) NOT NULL DEFAULT 0,
    corrigee         BOOLEAN NOT NULL DEFAULT FALSE,
    correct          BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (id_test_technique) REFERENCES test_technique(id_test_technique) ON DELETE CASCADE,
    UNIQUE (id_test_technique, question_ref)
);

CREATE INDEX idx_reponse_test ON reponse_test_technique(id_test_technique);
