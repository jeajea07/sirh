-- Bloc 2 : Recrutement / étape 2B (test technique)
-- Script autonome, sans dépendre des scripts du bloc 1 cassés.
-- Conserve les tables minimales nécessaires au parcours : candidat, offre, candidature, test technique, réponses.

CREATE TABLE IF NOT EXISTS candidat (
    id_candidat BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    identifiant VARCHAR(100) UNIQUE,
    nom VARCHAR(150) NOT NULL,
    prenom VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    telephone VARCHAR(40),
    mot_de_passe VARCHAR(255),
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acces_cree BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS offre (
    id_offre BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_offre VARCHAR(50) NOT NULL UNIQUE,
    titre VARCHAR(200) NOT NULL,
    nombre_postes INTEGER NOT NULL DEFAULT 1 CHECK (nombre_postes >= 1),
    date_publication DATE,
    date_limite DATE,
    statut VARCHAR(30) NOT NULL DEFAULT 'OUVERTE'
        CHECK (statut IN ('OUVERTE', 'FERMEE', 'ARCHIVEE')),
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS candidature (
    id_candidature BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_candidat BIGINT NOT NULL,
    id_offre BIGINT,
    date_depot TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    statut VARCHAR(40) NOT NULL DEFAULT 'RECU'
        CHECK (statut IN (
            'RECU',
            'DOSSIER_VALIDE',
            'TEST_EN_COURS',
            'TEST_OK',
            'ADMIS',
            'REFUSE',
            'ABSENT',
            'EN_ATTENTE'
        )),
    dossier_valide BOOLEAN NOT NULL DEFAULT FALSE,
    acces_cree BOOLEAN NOT NULL DEFAULT FALSE,
    note_finale NUMERIC(5,2),
    CONSTRAINT fk_candidature_candidat
        FOREIGN KEY (id_candidat) REFERENCES candidat(id_candidat),
    CONSTRAINT fk_candidature_offre
        FOREIGN KEY (id_offre) REFERENCES offre(id_offre),
    CONSTRAINT uk_candidature_candidat_offre UNIQUE (id_candidat, id_offre)
);

CREATE TABLE IF NOT EXISTS test_technique (
    id_test_technique BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_candidature BIGINT NOT NULL UNIQUE,
    date_debut TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_fin TIMESTAMP,
    statut VARCHAR(30) NOT NULL DEFAULT 'PROGRAMME'
        CHECK (statut IN (
            'PROGRAMME',
            'EN_COURS',
            'RELANCE',
            'CORRIGE',
            'ADMIS',
            'REFUSE',
            'ABSENT'
        )),
    seuil_note NUMERIC(5,2) NOT NULL DEFAULT 0,
    note_generale NUMERIC(5,2) NOT NULL DEFAULT 0,
    relance BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_test_candidature
        FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature)
);

CREATE TABLE IF NOT EXISTS reponse_test_technique (
    id_reponse BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_test_technique BIGINT NOT NULL,
    question_ref VARCHAR(100) NOT NULL,
    type_question VARCHAR(30) NOT NULL DEFAULT 'CHOIX'
        CHECK (type_question IN ('CHOIX', 'QCM', 'LIBRE')),
    reponse_textuelle TEXT,
    note NUMERIC(5,2) NOT NULL DEFAULT 0,
    corrigee BOOLEAN NOT NULL DEFAULT FALSE,
    correct BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_reponse_test
        FOREIGN KEY (id_test_technique) REFERENCES test_technique(id_test_technique),
    CONSTRAINT uk_reponse_test_question UNIQUE (id_test_technique, question_ref)
);

CREATE INDEX IF NOT EXISTS idx_candidature_candidat
    ON candidature(id_candidat);

CREATE INDEX IF NOT EXISTS idx_candidature_offre
    ON candidature(id_offre);

CREATE INDEX IF NOT EXISTS idx_candidature_statut
    ON candidature(statut);

CREATE INDEX IF NOT EXISTS idx_test_candidature
    ON test_technique(id_candidature);

CREATE INDEX IF NOT EXISTS idx_reponse_test
    ON reponse_test_technique(id_test_technique);

-- Script de structure uniquement.
-- Les données de test sont dans le fichier bloc2_teste.sql.

