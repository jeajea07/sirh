-- Bloc 4 : contrats, affectations, onboarding, documents, formalités et audit.
-- Ce script ne supprime jamais les tables du Bloc 1, 2B ou 3.

DROP TABLE IF EXISTS evenement_audit CASCADE;
DROP TABLE IF EXISTS validation_b4 CASCADE;
DROP TABLE IF EXISTS declaration_sociale CASCADE;
DROP TABLE IF EXISTS onboarding_tache CASCADE;
DROP TABLE IF EXISTS onboarding CASCADE;
DROP TABLE IF EXISTS modele_tache_onboarding CASCADE;
DROP TABLE IF EXISTS modele_onboarding CASCADE;
DROP TABLE IF EXISTS affectation CASCADE;
DROP TABLE IF EXISTS signature_contrat CASCADE;
DROP TABLE IF EXISTS document CASCADE;
DROP TABLE IF EXISTS contrat CASCADE;
DROP TABLE IF EXISTS organisme CASCADE;

CREATE TABLE organisme (
    id_organisme      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code              VARCHAR(30)  NOT NULL UNIQUE,
    nom               VARCHAR(150) NOT NULL,
    type              VARCHAR(30)  NOT NULL CHECK (type IN ('SOCIAL','FISCAL','SANTE','AUTRE')),
    site_web          VARCHAR(255),
    actif             BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE contrat (
    id_contrat              INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_employe              INT NOT NULL REFERENCES employe(id_employe),
    reference               VARCHAR(30) NOT NULL UNIQUE,
    id_type_contrat         INT NOT NULL REFERENCES type_contrat(id_type_contrat),
    type_operation          VARCHAR(20) NOT NULL DEFAULT 'INITIAL'
                             CHECK (type_operation IN ('INITIAL','RENOUVELLEMENT','AVENANT')),
    date_signature          DATE,
    date_debut              DATE NOT NULL,
    date_fin                DATE,
    fonction                VARCHAR(150) NOT NULL,
    categorie_professionnelle VARCHAR(100),
    salaire                 NUMERIC(14,2) NOT NULL CHECK (salaire >= 0),
    statut_workflow         VARCHAR(20) NOT NULL DEFAULT 'BROUILLON'
                             CHECK (statut_workflow IN
                               ('BROUILLON','A_VALIDER','A_SIGNER','SIGNE',
                                'EN_VIGUEUR','TERMINE','ANNULE')),
    statut_contractuel      VARCHAR(20) NOT NULL DEFAULT 'NON_ACTIF'
                             CHECK (statut_contractuel IN
                               ('NON_ACTIF','ACTIF','REMPLACE','SUSPENDU','TERMINE')),
    contrat_precedent_id    INT REFERENCES contrat(id_contrat),
    id_recrutement_externe  INT UNIQUE,
    motif_recours           VARCHAR(255),
    commentaire             TEXT,
    version                 INT NOT NULL DEFAULT 1,
    date_creation           TIMESTAMP NOT NULL DEFAULT NOW(),
    date_maj                TIMESTAMP NOT NULL DEFAULT NOW(),
    cree_par_id             INT NOT NULL REFERENCES employe(id_employe),
    CHECK (date_fin IS NULL OR date_fin > date_debut),
    CHECK (type_operation <> 'RENOUVELLEMENT' OR contrat_precedent_id IS NOT NULL),
    CHECK (type_operation <> 'AVENANT' OR contrat_precedent_id IS NOT NULL)
);

CREATE TABLE document (
    id_document             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type_document           VARCHAR(30) NOT NULL
                             CHECK (type_document IN
                               ('CONTRAT','ADMINISTRATIF','SOCIAL','SANTE',
                                'INTERNE','FORMATION','MATERIEL','CV')),
    titre                   VARCHAR(200) NOT NULL,
    nom_fichier             VARCHAR(255) NOT NULL,
    chemin_stockage         VARCHAR(500) NOT NULL,
    type_mime               VARCHAR(100),
    taille_octets           BIGINT,
    version                 INT NOT NULL DEFAULT 1,
    statut_validation       VARCHAR(20) NOT NULL DEFAULT 'RECU'
                             CHECK (statut_validation IN ('ATTENDU','RECU','VERIFIE','REJETE')),
    niveau_confidentialite  VARCHAR(20) NOT NULL DEFAULT 'INTERNE'
                             CHECK (niveau_confidentialite IN
                               ('PUBLIC','INTERNE','CONFIDENTIEL','SECRET')),
    id_employe              INT REFERENCES employe(id_employe),
    id_contrat              INT,
    id_onboarding_tache     INT,
    date_depot              TIMESTAMP NOT NULL DEFAULT NOW(),
    depose_par_id           INT NOT NULL REFERENCES employe(id_employe),
    date_verification       TIMESTAMP,
    verifie_par_id          INT REFERENCES employe(id_employe),
    motif_rejet             TEXT,
    commentaire             TEXT
);

CREATE TABLE signature_contrat (
    id_signature_contrat  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_contrat            INT NOT NULL REFERENCES contrat(id_contrat) ON DELETE CASCADE,
    partie                VARCHAR(20) NOT NULL CHECK (partie IN ('SALARIE','ENTREPRISE')),
    nom_signataire        VARCHAR(150) NOT NULL,
    date_signature        DATE NOT NULL,
    statut                VARCHAR(20) NOT NULL DEFAULT 'SIGNEE'
                           CHECK (statut IN ('ATTENDUE','SIGNEE','REFUSEE')),
    commentaire           TEXT,
    date_creation         TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (id_contrat, partie)
);

CREATE TABLE affectation (
    id_affectation    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_employe        INT NOT NULL REFERENCES employe(id_employe),
    id_poste          INT NOT NULL REFERENCES poste(id_poste),
    id_departement    INT NOT NULL REFERENCES departement(id_departement),
    id_contrat        INT REFERENCES contrat(id_contrat),
    id_responsable    INT REFERENCES employe(id_employe),
    site              VARCHAR(150),
    date_debut        DATE NOT NULL,
    date_fin          DATE,
    motif             VARCHAR(255),
    statut            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                       CHECK (statut IN ('ACTIVE','TERMINEE')),
    date_creation     TIMESTAMP NOT NULL DEFAULT NOW(),
    date_maj          TIMESTAMP NOT NULL DEFAULT NOW(),
    cree_par_id       INT NOT NULL REFERENCES employe(id_employe),
    CHECK (date_fin IS NULL OR date_fin > date_debut)
);

CREATE TABLE modele_onboarding (
    id_modele_onboarding  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom                   VARCHAR(150) NOT NULL,
    description           TEXT,
    id_departement        INT REFERENCES departement(id_departement),
    id_poste              INT REFERENCES poste(id_poste),
    version               INT NOT NULL DEFAULT 1,
    actif                 BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation         TIMESTAMP NOT NULL DEFAULT NOW(),
    date_maj              TIMESTAMP NOT NULL DEFAULT NOW(),
    cree_par_id           INT NOT NULL REFERENCES employe(id_employe),
    UNIQUE (nom, version)
);

CREATE TABLE modele_tache_onboarding (
    id_modele_tache_onboarding INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_modele_onboarding       INT NOT NULL
                                 REFERENCES modele_onboarding(id_modele_onboarding)
                                 ON DELETE CASCADE,
    titre                      VARCHAR(200) NOT NULL,
    description                TEXT,
    role_responsable           VARCHAR(30) NOT NULL
                                 CHECK (role_responsable IN
                                   ('EMPLOYE','RESPONSABLE','RH','FINANCE','DG',
                                    'INFORMATIQUE','SERVICES_GENERAUX','PAIE',
                                    'JURIDIQUE','SANTE')),
    delai_indicatif_jours      INT NOT NULL DEFAULT 0 CHECK (delai_indicatif_jours >= 0),
    obligatoire                BOOLEAN NOT NULL DEFAULT TRUE,
    bloquante                  BOOLEAN NOT NULL DEFAULT FALSE,
    ordre                      INT NOT NULL DEFAULT 0,
    condition_activation       VARCHAR(255) NOT NULL DEFAULT 'TOUJOURS',
    depend_de_tache_id         INT
                                 REFERENCES modele_tache_onboarding(id_modele_tache_onboarding)
                                 ON DELETE SET NULL,
    date_creation              TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE onboarding (
    id_onboarding         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reference             VARCHAR(30) NOT NULL UNIQUE,
    id_employe            INT NOT NULL REFERENCES employe(id_employe),
    id_contrat            INT NOT NULL REFERENCES contrat(id_contrat),
    id_modele_onboarding  INT NOT NULL REFERENCES modele_onboarding(id_modele_onboarding),
    version_modele        INT NOT NULL,
    date_debut_prevue     DATE NOT NULL,
    date_debut_reelle     DATE,
    date_fin_prevue       DATE,
    date_cloture          TIMESTAMP,
    statut                VARCHAR(20) NOT NULL DEFAULT 'A_PREPARER'
                           CHECK (statut IN
                             ('A_PREPARER','PREPARATION','PRET','EN_COURS',
                              'BLOQUE','A_VERIFIER','CLOTURE','ANNULE')),
    id_responsable_rh     INT NOT NULL REFERENCES employe(id_employe),
    id_cloture_par        INT REFERENCES employe(id_employe),
    motif_annulation      TEXT,
    date_creation         TIMESTAMP NOT NULL DEFAULT NOW(),
    date_maj              TIMESTAMP NOT NULL DEFAULT NOW(),
    cree_par_id           INT NOT NULL REFERENCES employe(id_employe)
);

CREATE TABLE onboarding_tache (
    id_onboarding_tache          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_onboarding                INT NOT NULL REFERENCES onboarding(id_onboarding) ON DELETE CASCADE,
    id_modele_tache_onboarding   INT REFERENCES modele_tache_onboarding(id_modele_tache_onboarding) ON DELETE SET NULL,
    titre                        VARCHAR(200) NOT NULL,
    description                  TEXT,
    role_responsable             VARCHAR(30) NOT NULL
                                   CHECK (role_responsable IN
                                     ('EMPLOYE','RESPONSABLE','RH','FINANCE','DG',
                                      'INFORMATIQUE','SERVICES_GENERAUX','PAIE',
                                      'JURIDIQUE','SANTE')),
    id_responsable               INT REFERENCES employe(id_employe),
    date_echeance                DATE,
    statut                       VARCHAR(20) NOT NULL DEFAULT 'A_FAIRE'
                                   CHECK (statut IN
                                     ('A_FAIRE','EN_COURS','TERMINEE','BLOQUEE','ANNULEE')),
    date_debut                   TIMESTAMP,
    date_realisation             TIMESTAMP,
    realise_par_id               INT REFERENCES employe(id_employe),
    commentaire                  TEXT,
    motif_blocage                TEXT,
    obligatoire                  BOOLEAN NOT NULL DEFAULT TRUE,
    bloquante                    BOOLEAN NOT NULL DEFAULT FALSE,
    ordre                        INT NOT NULL DEFAULT 0,
    depend_de_tache_id           INT REFERENCES onboarding_tache(id_onboarding_tache) ON DELETE SET NULL,
    date_creation                TIMESTAMP NOT NULL DEFAULT NOW(),
    date_maj                     TIMESTAMP NOT NULL DEFAULT NOW(),
    CHECK (statut <> 'BLOQUEE' OR motif_blocage IS NOT NULL),
    CHECK (statut <> 'TERMINEE' OR date_realisation IS NOT NULL)
);

CREATE TABLE declaration_sociale (
    id_declaration_sociale   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_employe               INT NOT NULL REFERENCES employe(id_employe),
    id_organisme             INT NOT NULL REFERENCES organisme(id_organisme),
    type_declaration         VARCHAR(100) NOT NULL,
    date_preparation         DATE,
    date_envoi               DATE,
    reference_externe        VARCHAR(100),
    statut                   VARCHAR(20) NOT NULL DEFAULT 'A_PREPARER'
                               CHECK (statut IN
                                 ('A_PREPARER','PREPAREE','TRANSMISE','CONFIRMEE','REJETEE')),
    id_document_justificatif INT REFERENCES document(id_document),
    commentaire              TEXT,
    date_creation            TIMESTAMP NOT NULL DEFAULT NOW(),
    date_maj                 TIMESTAMP NOT NULL DEFAULT NOW(),
    cree_par_id              INT NOT NULL REFERENCES employe(id_employe)
);

CREATE TABLE validation_b4 (
    id_validation_b4  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type_objet        VARCHAR(30) NOT NULL
                        CHECK (type_objet IN ('CONTRAT','ONBOARDING','DECLARATION','DOCUMENT')),
    id_objet          INT NOT NULL,
    etape             VARCHAR(30) NOT NULL,
    id_validateur     INT REFERENCES employe(id_employe),
    decision          VARCHAR(10) CHECK (decision IN ('VALIDEE','RENVOYEE','REJETEE')),
    commentaire       TEXT,
    date_reception    TIMESTAMP NOT NULL DEFAULT NOW(),
    date_decision     TIMESTAMP
);

CREATE TABLE evenement_audit (
    id_evenement_audit  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type_objet          VARCHAR(30) NOT NULL
                          CHECK (type_objet IN
                            ('CONTRAT','AFFECTATION','ONBOARDING','TACHE',
                             'DOCUMENT','DECLARATION','MODELE','SALARIE')),
    id_objet            INT NOT NULL,
    action              VARCHAR(50) NOT NULL,
    ancienne_valeur     TEXT,
    nouvelle_valeur     TEXT,
    commentaire         TEXT,
    id_auteur           INT NOT NULL REFERENCES employe(id_employe),
    date_evenement      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Les FK de document sont ajoutées après la création des tables cibles.
ALTER TABLE document
    ADD CONSTRAINT fk_document_contrat
    FOREIGN KEY (id_contrat) REFERENCES contrat(id_contrat);

ALTER TABLE document
    ADD CONSTRAINT fk_document_tache
    FOREIGN KEY (id_onboarding_tache) REFERENCES onboarding_tache(id_onboarding_tache);

-- Dossier salarié (colonnes toutes nullables pour préserver le seed Bloc 1).
ALTER TABLE employe ADD COLUMN IF NOT EXISTS cin VARCHAR(20);
ALTER TABLE employe ADD COLUMN IF NOT EXISTS date_naissance DATE;
ALTER TABLE employe ADD COLUMN IF NOT EXISTS lieu_naissance VARCHAR(150);
ALTER TABLE employe ADD COLUMN IF NOT EXISTS telephone VARCHAR(30);
ALTER TABLE employe ADD COLUMN IF NOT EXISTS adresse TEXT;
ALTER TABLE employe ADD COLUMN IF NOT EXISTS situation_familiale VARCHAR(30);
ALTER TABLE employe ADD COLUMN IF NOT EXISTS nombre_enfants INT DEFAULT 0;
ALTER TABLE employe ADD COLUMN IF NOT EXISTS numero_cnaps VARCHAR(30);
ALTER TABLE employe ADD COLUMN IF NOT EXISTS numero_ostie VARCHAR(30);
ALTER TABLE employe ADD COLUMN IF NOT EXISTS rib VARCHAR(50);
ALTER TABLE employe ADD COLUMN IF NOT EXISTS date_sortie DATE;
ALTER TABLE employe ADD COLUMN IF NOT EXISTS motif_sortie TEXT;
CREATE UNIQUE INDEX IF NOT EXISTS ux_employe_cin ON employe (cin) WHERE cin IS NOT NULL;

-- Lien générique des notifications du Bloc 4.
ALTER TABLE notification ADD COLUMN IF NOT EXISTS type_objet VARCHAR(30);
ALTER TABLE notification ADD COLUMN IF NOT EXISTS id_objet INT;

-- Index du Bloc 4.
CREATE INDEX IF NOT EXISTS idx_contrat_employe ON contrat (id_employe);
CREATE INDEX IF NOT EXISTS idx_contrat_statut ON contrat (statut_workflow);
CREATE INDEX IF NOT EXISTS idx_contrat_echeance ON contrat (date_fin);
CREATE INDEX IF NOT EXISTS idx_contrat_precedent ON contrat (contrat_precedent_id);
CREATE INDEX IF NOT EXISTS idx_contrat_recrutement ON contrat (id_recrutement_externe);
CREATE INDEX IF NOT EXISTS idx_document_employe ON document (id_employe);
CREATE INDEX IF NOT EXISTS idx_document_contrat ON document (id_contrat);
CREATE INDEX IF NOT EXISTS idx_document_statut ON document (statut_validation);
CREATE INDEX IF NOT EXISTS idx_document_type ON document (type_document);
CREATE INDEX IF NOT EXISTS idx_affectation_employe ON affectation (id_employe);
CREATE INDEX IF NOT EXISTS idx_affectation_poste ON affectation (id_poste);
CREATE INDEX IF NOT EXISTS idx_affectation_departement ON affectation (id_departement);
CREATE INDEX IF NOT EXISTS idx_affectation_statut ON affectation (statut);
CREATE UNIQUE INDEX IF NOT EXISTS ux_affectation_active ON affectation (id_employe) WHERE statut = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_onboarding_employe ON onboarding (id_employe);
CREATE INDEX IF NOT EXISTS idx_onboarding_statut ON onboarding (statut);
CREATE INDEX IF NOT EXISTS idx_onboarding_contrat ON onboarding (id_contrat);
CREATE INDEX IF NOT EXISTS idx_tache_onboarding ON onboarding_tache (id_onboarding);
CREATE INDEX IF NOT EXISTS idx_tache_responsable ON onboarding_tache (id_responsable);
CREATE INDEX IF NOT EXISTS idx_tache_statut ON onboarding_tache (statut);
CREATE INDEX IF NOT EXISTS idx_tache_echeance ON onboarding_tache (date_echeance);
CREATE INDEX IF NOT EXISTS idx_tache_dependance ON onboarding_tache (depend_de_tache_id);
CREATE INDEX IF NOT EXISTS idx_declaration_employe ON declaration_sociale (id_employe);
CREATE INDEX IF NOT EXISTS idx_declaration_statut ON declaration_sociale (statut);
CREATE INDEX IF NOT EXISTS idx_declaration_organisme ON declaration_sociale (id_organisme);
CREATE INDEX IF NOT EXISTS idx_validation_objet ON validation_b4 (type_objet, id_objet);
CREATE INDEX IF NOT EXISTS idx_validation_validateur ON validation_b4 (id_validateur);
CREATE INDEX IF NOT EXISTS idx_audit_objet ON evenement_audit (type_objet, id_objet, date_evenement DESC);
CREATE INDEX IF NOT EXISTS idx_audit_auteur ON evenement_audit (id_auteur);
CREATE INDEX IF NOT EXISTS idx_notification_objet ON notification (type_objet, id_objet);
