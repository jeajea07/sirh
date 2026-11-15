-- =====================================================================
-- BLOC 2 — Étapes 5, 6 et 7 : les trois niveaux d'entretien
--   1 = RH, 2 = Technicien, 3 = DG (dans cet ordre, sans sauter de niveau).
-- Version simplifiée retenue : la RH planifie l'entretien, saisit la grille
-- (importance 1 à 3, questions éliminatoires) et les notes du jury de 1 à 5 ;
-- l'application calcule la note générale, la compare aux seuils et propose
-- un résultat ; la RH confirme le passage au niveau suivant.
--
-- À exécuter après bloc2_test_technique.sql. Idempotent.
-- =====================================================================

CREATE TABLE IF NOT EXISTS b2_entretien (
    id_entretien      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_candidature    INT          NOT NULL REFERENCES candidature(id_candidature) ON DELETE CASCADE,
    niveau            SMALLINT     NOT NULL CHECK (niveau BETWEEN 1 AND 3),
    date_prevue       TIMESTAMP    NOT NULL,
    date_origine      TIMESTAMP    NOT NULL,              -- rendez-vous d'origine, conservé en cas de report
    lieu              VARCHAR(150),
    jury              VARCHAR(255),
    statut            VARCHAR(20)  NOT NULL DEFAULT 'PLANIFIE'
                      CHECK (statut IN ('PLANIFIE','REPORTE','REALISE','ABSENT','ANNULE')),
    motif_report      TEXT,
    seuil_general     NUMERIC(3,2) NOT NULL DEFAULT 3 CHECK (seuil_general BETWEEN 1 AND 5),
    note_generale     NUMERIC(4,2) CHECK (note_generale IS NULL OR note_generale BETWEEN 1 AND 5),
    resultat_propose  VARCHAR(20)  CHECK (resultat_propose IN ('ADMIS','NON_ADMIS')),
    motif_proposition TEXT,
    decision          VARCHAR(20)  CHECK (decision IN ('ADMIS','NON_ADMIS')),
    motif_decision    TEXT,
    id_decideur       INT REFERENCES employe(id_employe),
    date_decision     TIMESTAMP,
    id_planificateur  INT REFERENCES employe(id_employe),
    date_creation     TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (id_candidature, niveau)
);

CREATE INDEX IF NOT EXISTS idx_b2_entretien_candidature ON b2_entretien(id_candidature);

-- Grille d'évaluation et notes du jury (une ligne par question)
CREATE TABLE IF NOT EXISTS b2_entretien_note (
    id_note        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_entretien   INT          NOT NULL REFERENCES b2_entretien(id_entretien) ON DELETE CASCADE,
    ordre          SMALLINT     NOT NULL,
    question       VARCHAR(255) NOT NULL,
    importance     SMALLINT     NOT NULL DEFAULT 1 CHECK (importance BETWEEN 1 AND 3),
    eliminatoire   BOOLEAN      NOT NULL DEFAULT FALSE,
    note_minimale  NUMERIC(3,2) CHECK (note_minimale IS NULL OR note_minimale BETWEEN 1 AND 5),
    note           NUMERIC(3,2) CHECK (note IS NULL OR note BETWEEN 1 AND 5),
    UNIQUE (id_entretien, ordre)
);
