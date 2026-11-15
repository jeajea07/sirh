-- =====================================================================
-- BLOC 5 — Dossier administratif du salarié (voir readme/Bloc5.md)
-- À appliquer APRÈS bloc4.sql. Script idempotent (rejouable).
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Informations personnelles et statut administratif (§5, §6 — RG-IP, RG-SP)
-- ---------------------------------------------------------------------
ALTER TABLE employe ADD COLUMN IF NOT EXISTS sexe VARCHAR(1);
ALTER TABLE employe ADD COLUMN IF NOT EXISTS nationalite VARCHAR(80);
ALTER TABLE employe ADD COLUMN IF NOT EXISTS contact_urgence_nom VARCHAR(150);
ALTER TABLE employe ADD COLUMN IF NOT EXISTS contact_urgence_telephone VARCHAR(30);
ALTER TABLE employe ADD COLUMN IF NOT EXISTS statut_administratif VARCHAR(20) NOT NULL DEFAULT 'ACTIF';
ALTER TABLE employe ADD COLUMN IF NOT EXISTS date_notification_sortie DATE;
ALTER TABLE employe ADD COLUMN IF NOT EXISTS sortie_enregistree_par_id INT REFERENCES employe(id_employe);

ALTER TABLE employe DROP CONSTRAINT IF EXISTS ck_employe_sexe;
ALTER TABLE employe ADD CONSTRAINT ck_employe_sexe CHECK (sexe IS NULL OR sexe IN ('M','F'));
ALTER TABLE employe DROP CONSTRAINT IF EXISTS ck_employe_statut_administratif;
ALTER TABLE employe ADD CONSTRAINT ck_employe_statut_administratif
    CHECK (statut_administratif IN ('EN_INTEGRATION','ACTIF','SUSPENDU','EN_PREAVIS','SORTI'));
ALTER TABLE employe DROP CONSTRAINT IF EXISTS ck_employe_nombre_enfants;
ALTER TABLE employe ADD CONSTRAINT ck_employe_nombre_enfants CHECK (nombre_enfants IS NULL OR nombre_enfants >= 0);

-- Reprise de l'existant : un employé inactif est sorti.
UPDATE employe SET statut_administratif = 'SORTI'
 WHERE actif = FALSE AND statut_administratif <> 'SORTI';

-- RG-IP02 : N° CNaPS unique.
CREATE UNIQUE INDEX IF NOT EXISTS ux_employe_cnaps ON employe (numero_cnaps) WHERE numero_cnaps IS NOT NULL AND numero_cnaps <> '';

-- ---------------------------------------------------------------------
-- 2. Historique de salaire (§9 — RG-SA)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS historique_salaire (
    id_historique_salaire     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_employe                INT NOT NULL REFERENCES employe(id_employe),
    id_contrat                INT REFERENCES contrat(id_contrat),
    salaire_base              NUMERIC(14,2) NOT NULL CHECK (salaire_base > 0),
    categorie_professionnelle VARCHAR(100),
    date_effet                DATE NOT NULL,
    date_fin                  DATE,
    motif                     VARCHAR(30) NOT NULL
                               CHECK (motif IN ('EMBAUCHE','RENOUVELLEMENT','AVENANT',
                                                'AUGMENTATION_GENERALE','AUGMENTATION_INDIVIDUELLE',
                                                'PROMOTION','CORRECTION')),
    commentaire               TEXT,
    valide_par_id             INT NOT NULL REFERENCES employe(id_employe),
    date_creation             TIMESTAMP NOT NULL DEFAULT NOW(),
    CHECK (date_fin IS NULL OR date_fin >= date_effet)
);
-- RG-SA01 : une seule ligne en cours par salarié.
CREATE UNIQUE INDEX IF NOT EXISTS ux_historique_salaire_en_cours
    ON historique_salaire (id_employe) WHERE date_fin IS NULL;
CREATE INDEX IF NOT EXISTS idx_historique_salaire_employe ON historique_salaire (id_employe, date_effet);

-- Reprise : salaire des contrats en vigueur.
INSERT INTO historique_salaire (id_employe, id_contrat, salaire_base, categorie_professionnelle,
                                date_effet, motif, commentaire, valide_par_id)
SELECT c.id_employe, c.id_contrat, c.salaire, c.categorie_professionnelle, c.date_debut,
       CASE c.type_operation WHEN 'AVENANT' THEN 'AVENANT'
                             WHEN 'RENOUVELLEMENT' THEN 'RENOUVELLEMENT'
                             ELSE 'EMBAUCHE' END,
       'Reprise du contrat ' || c.reference, c.cree_par_id
  FROM contrat c
 WHERE c.statut_workflow = 'EN_VIGUEUR' AND c.salaire > 0
   AND NOT EXISTS (SELECT 1 FROM historique_salaire h WHERE h.id_employe = c.id_employe);

-- ---------------------------------------------------------------------
-- 3. Historique de carrière (§11 — RG-HI)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS evenement_carriere (
    id_evenement_carriere  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_employe             INT NOT NULL REFERENCES employe(id_employe),
    type_evenement         VARCHAR(30) NOT NULL
                            CHECK (type_evenement IN
                              ('EMBAUCHE','REEMBAUCHE','RENOUVELLEMENT_CONTRAT','TRANSFORMATION_CDI',
                               'AVENANT','AFFECTATION','MUTATION','CHANGEMENT_POSTE',
                               'CHANGEMENT_RESPONSABLE','PROMOTION','REVISION_SALAIRE',
                               'SUSPENSION','REPRISE','SORTIE_ANNONCEE','SORTIE_ANNULEE',
                               'SORTIE','CORRECTION')),
    date_effet             DATE NOT NULL,
    date_enregistrement    TIMESTAMP NOT NULL DEFAULT NOW(),
    id_auteur              INT NOT NULL REFERENCES employe(id_employe),
    libelle                VARCHAR(255) NOT NULL,
    ancienne_valeur        TEXT,
    nouvelle_valeur        TEXT,
    objet_source           VARCHAR(30),
    id_objet_source        INT,
    id_evenement_corrige   INT REFERENCES evenement_carriere(id_evenement_carriere)
);
CREATE INDEX IF NOT EXISTS idx_evenement_carriere_employe ON evenement_carriere (id_employe, date_effet);

-- RG-HI02 : un événement de carrière n'est ni modifiable ni supprimable.
CREATE OR REPLACE FUNCTION evenement_carriere_immuable() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'evenement_carriere est en ajout seul : créer un événement CORRECTION (RG-HI02)';
END;
$$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS trg_evenement_carriere_immuable ON evenement_carriere;
CREATE TRIGGER trg_evenement_carriere_immuable
    BEFORE UPDATE OR DELETE ON evenement_carriere
    FOR EACH ROW EXECUTE FUNCTION evenement_carriere_immuable();

-- Reprise : contrats en vigueur ou terminés, puis affectations existantes.
INSERT INTO evenement_carriere (id_employe, type_evenement, date_effet, id_auteur, libelle,
                                nouvelle_valeur, objet_source, id_objet_source)
SELECT c.id_employe,
       CASE c.type_operation WHEN 'AVENANT' THEN 'AVENANT'
                             WHEN 'RENOUVELLEMENT' THEN 'RENOUVELLEMENT_CONTRAT'
                             ELSE 'EMBAUCHE' END,
       c.date_debut, c.cree_par_id,
       'Contrat ' || c.reference || ' (' || t.libelle || ')',
       c.fonction, 'CONTRAT', c.id_contrat
  FROM contrat c JOIN type_contrat t ON t.id_type_contrat = c.id_type_contrat
 WHERE c.statut_workflow IN ('EN_VIGUEUR','TERMINE')
   AND NOT EXISTS (SELECT 1 FROM evenement_carriere e
                    WHERE e.objet_source = 'CONTRAT' AND e.id_objet_source = c.id_contrat);

INSERT INTO evenement_carriere (id_employe, type_evenement, date_effet, id_auteur, libelle,
                                nouvelle_valeur, objet_source, id_objet_source)
SELECT a.id_employe, 'AFFECTATION', a.date_debut, a.cree_par_id,
       'Affectation : ' || p.designation || ' / ' || d.designation,
       p.designation || ' / ' || d.designation, 'AFFECTATION', a.id_affectation
  FROM affectation a
  JOIN poste p ON p.id_poste = a.id_poste
  JOIN departement d ON d.id_departement = a.id_departement
 WHERE NOT EXISTS (SELECT 1 FROM evenement_carriere e
                    WHERE e.objet_source = 'AFFECTATION' AND e.id_objet_source = a.id_affectation);

-- ---------------------------------------------------------------------
-- 4. Documents administratifs et validité (§10 — RG-DO)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS type_document_administratif (
    id_type_document_administratif INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code                    VARCHAR(40) NOT NULL UNIQUE,
    libelle                 VARCHAR(150) NOT NULL,
    categorie               VARCHAR(30) NOT NULL
                             CHECK (categorie IN ('CONTRAT','ADMINISTRATIF','SOCIAL','SANTE',
                                                  'INTERNE','FORMATION','MATERIEL','CV')),
    obligatoire             BOOLEAN NOT NULL DEFAULT FALSE,
    expiration_requise      BOOLEAN NOT NULL DEFAULT FALSE,
    duree_validite_jours    INT CHECK (duree_validite_jours IS NULL OR duree_validite_jours > 0),
    delai_alerte_jours      INT NOT NULL DEFAULT 30 CHECK (delai_alerte_jours >= 0),
    niveau_confidentialite  VARCHAR(20) NOT NULL DEFAULT 'CONFIDENTIEL'
                             CHECK (niveau_confidentialite IN ('PUBLIC','INTERNE','CONFIDENTIEL','SECRET')),
    actif                   BOOLEAN NOT NULL DEFAULT TRUE
);

-- Valeurs par défaut : durées et délais à valider avec le RH (Bloc5.md §10.1).
INSERT INTO type_document_administratif
    (code, libelle, categorie, obligatoire, expiration_requise, duree_validite_jours, delai_alerte_jours, niveau_confidentialite)
VALUES
    ('CIN',                  'Copie de la CIN',                        'ADMINISTRATIF', TRUE,  FALSE, NULL, 60, 'CONFIDENTIEL'),
    ('CERTIFICAT_RESIDENCE', 'Certificat de résidence',                'ADMINISTRATIF', TRUE,  TRUE,  90,   15, 'CONFIDENTIEL'),
    ('CASIER_JUDICIAIRE',    'Bulletin n°3 du casier judiciaire',      'ADMINISTRATIF', FALSE, TRUE,  90,   0,  'SECRET'),
    ('DIPLOME',              'Diplôme / attestation',                  'FORMATION',     FALSE, FALSE, NULL, 0,  'INTERNE'),
    ('APTITUDE_MEDICALE',    'Certificat d''aptitude médicale',        'SANTE',         TRUE,  TRUE,  365,  30, 'SECRET'),
    ('ATTESTATION_CNAPS',    'Attestation d''affiliation CNaPS',       'SOCIAL',        TRUE,  FALSE, NULL, 0,  'CONFIDENTIEL'),
    ('RIB',                  'Relevé d''identité bancaire',            'ADMINISTRATIF', TRUE,  FALSE, NULL, 0,  'SECRET'),
    ('ACTE_ETAT_CIVIL',      'Acte d''état civil (mariage, naissance)','ADMINISTRATIF', FALSE, FALSE, NULL, 0,  'CONFIDENTIEL'),
    ('PERMIS_CONDUIRE',      'Permis de conduire',                     'ADMINISTRATIF', FALSE, TRUE,  NULL, 60, 'CONFIDENTIEL'),
    ('AUTORISATION_TRAVAIL', 'Autorisation de travail / visa',         'ADMINISTRATIF', FALSE, TRUE,  NULL, 90, 'CONFIDENTIEL')
ON CONFLICT (code) DO NOTHING;

ALTER TABLE document ADD COLUMN IF NOT EXISTS id_type_document_administratif INT
    REFERENCES type_document_administratif(id_type_document_administratif);
ALTER TABLE document ADD COLUMN IF NOT EXISTS date_emission DATE;
ALTER TABLE document ADD COLUMN IF NOT EXISTS date_expiration DATE;
ALTER TABLE document ADD COLUMN IF NOT EXISTS id_document_remplace INT REFERENCES document(id_document);

ALTER TABLE document DROP CONSTRAINT IF EXISTS document_statut_validation_check;
ALTER TABLE document DROP CONSTRAINT IF EXISTS ck_document_statut_validation;
ALTER TABLE document ADD CONSTRAINT ck_document_statut_validation
    CHECK (statut_validation IN ('ATTENDU','RECU','VERIFIE','REJETE','A_RENOUVELER','EXPIRE','REMPLACE'));
ALTER TABLE document DROP CONSTRAINT IF EXISTS ck_document_dates;
ALTER TABLE document ADD CONSTRAINT ck_document_dates
    CHECK (date_expiration IS NULL OR date_emission IS NULL OR date_expiration > date_emission);

CREATE INDEX IF NOT EXISTS idx_document_expiration ON document (date_expiration) WHERE date_expiration IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_document_type_admin ON document (id_employe, id_type_document_administratif);
