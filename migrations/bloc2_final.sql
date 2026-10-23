-- Bloc 2B : Recrutement / test technique
-- Script final propre, prêt pour la soutenance.
-- 1) Structure
-- 2) Données de démonstration

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

INSERT INTO candidat (identifiant, nom, prenom, email, telephone, mot_de_passe, actif, acces_cree)
VALUES
('CAND001', 'Rakoto', 'Jean', 'jean.rakoto@example.com', '0341234567', '$2b$10$dsEOACCB3gGrOJQnvjNV1e5WI1ElhEqQu0.kI1c1120bx7/syfiCO', TRUE, TRUE),
('CAND002', 'Rabe', 'Marie', 'marie.rabe@example.com', '0347654321', '$2b$10$.YV20rVvmJfb7sjgAmn88uiD1YTmQSZqGgGKo2ZvIdSZmLVaJAbj6', TRUE, FALSE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO offre (numero_offre, titre, nombre_postes, date_publication, date_limite, statut)
VALUES
('OFF-2026-001', 'Développeur Java', 2, CURRENT_DATE, CURRENT_DATE + 30, 'OUVERTE'),
('OFF-2026-002', 'Analyste RH', 1, CURRENT_DATE, CURRENT_DATE + 20, 'OUVERTE')
ON CONFLICT (numero_offre) DO NOTHING;

INSERT INTO candidature (id_candidat, id_offre, statut, dossier_valide, acces_cree, note_finale)
VALUES
((SELECT id_candidat FROM candidat WHERE email = 'jean.rakoto@example.com'),
 (SELECT id_offre FROM offre WHERE numero_offre = 'OFF-2026-001'),
 'DOSSIER_VALIDE', TRUE, TRUE, NULL),
((SELECT id_candidat FROM candidat WHERE email = 'marie.rabe@example.com'),
 (SELECT id_offre FROM offre WHERE numero_offre = 'OFF-2026-002'),
 'EN_ATTENTE', FALSE, FALSE, NULL)
ON CONFLICT (id_candidat, id_offre) DO NOTHING;

INSERT INTO test_technique (id_candidature, statut, seuil_note, note_generale, relance)
VALUES
((SELECT id_candidature FROM candidature WHERE id_candidat = (SELECT id_candidat FROM candidat WHERE email = 'jean.rakoto@example.com')),
 'EN_COURS', 10.00, 12.50, FALSE)
ON CONFLICT (id_candidature) DO NOTHING;

INSERT INTO reponse_test_technique (id_test_technique, question_ref, type_question, reponse_textuelle, note, corrigee, correct)
VALUES
((SELECT id_test_technique FROM test_technique WHERE id_candidature = (SELECT id_candidature FROM candidature WHERE id_candidat = (SELECT id_candidat FROM candidat WHERE email = 'jean.rakoto@example.com'))),
 'Q1', 'CHOIX', 'Java', 1.00, TRUE, TRUE),
((SELECT id_test_technique FROM test_technique WHERE id_candidature = (SELECT id_candidature FROM candidature WHERE id_candidat = (SELECT id_candidat FROM candidat WHERE email = 'jean.rakoto@example.com'))),
 'Q2', 'CHOIX', 'BigDecimal', 1.00, TRUE, TRUE),
((SELECT id_test_technique FROM test_technique WHERE id_candidature = (SELECT id_candidature FROM candidature WHERE id_candidat = (SELECT id_candidat FROM candidat WHERE email = 'jean.rakoto@example.com'))),
 'Q3', 'LIBRE', 'J''ai expliqué la logique Java et le protocole de validation.', 0.50, FALSE, FALSE)
ON CONFLICT (id_test_technique, question_ref) DO NOTHING;
