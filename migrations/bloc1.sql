DROP TABLE IF EXISTS relance                 CASCADE;
DROP TABLE IF EXISTS controle_automatique    CASCADE;
DROP TABLE IF EXISTS notification             CASCADE;
DROP TABLE IF EXISTS historique_modification CASCADE;
DROP TABLE IF EXISTS validation_etape        CASCADE;
DROP TABLE IF EXISTS circuit_validation      CASCADE;
DROP TABLE IF EXISTS critere_recrutement     CASCADE;
DROP TABLE IF EXISTS demande_besoin          CASCADE;
DROP TABLE IF EXISTS promotion               CASCADE;
DROP TABLE IF EXISTS budget_departement      CASCADE;
DROP TABLE IF EXISTS grille_salariale        CASCADE;
DROP TABLE IF EXISTS type_contrat            CASCADE;
DROP TABLE IF EXISTS utilisateur              CASCADE;
DROP TABLE IF EXISTS employe                 CASCADE;
DROP TABLE IF EXISTS poste                   CASCADE;
DROP TABLE IF EXISTS departement             CASCADE;
DROP TABLE IF EXISTS unite_organisationnelle CASCADE;

CREATE TABLE poste (
    id_poste    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    designation VARCHAR(150) NOT NULL
);

CREATE TABLE unite_organisationnelle (
    id_unite_organisationnelle INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    designation                VARCHAR(150) NOT NULL CHECK (designation IN ('DIRECTION', 'DEPARTEMENT', 'SERVICE', 'SECTION'))
);

CREATE TABLE departement (
    id_departement              INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    designation                 VARCHAR(150) NOT NULL,
    id_unite_organisationnelle  INT,
    effectif                    INT,
    FOREIGN KEY(id_unite_organisationnelle) REFERENCES unite_organisationnelle(id_unite_organisationnelle)
);

CREATE TABLE employe (
    id_employe     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    matricule      VARCHAR(30)  NOT NULL UNIQUE,
    nom            VARCHAR(100) NOT NULL,
    prenom         VARCHAR(100) NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,
    id_departement INT NOT NULL,
    id_poste       INT,
    date_embauche  DATE,
    actif          BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (id_departement) REFERENCES departement(id_departement),
    FOREIGN KEY (id_poste)       REFERENCES poste(id_poste)
);

CREATE TABLE utilisateur (
    id_utilisateur INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_employe     INT NOT NULL UNIQUE,
    mot_de_passe   VARCHAR(100) NOT NULL,   -- hash BCrypt, jamais en clair
    role           VARCHAR(30)  NOT NULL
                   CHECK (role IN ('EMPLOYE','RESPONSABLE','RH','FINANCE','DG')),
    actif          BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (id_employe) REFERENCES employe(id_employe)
);

CREATE TABLE type_contrat (
    id_type_contrat INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code            VARCHAR(20)  NOT NULL UNIQUE,
    libelle         VARCHAR(100) NOT NULL
);

CREATE TABLE grille_salariale (
    id_grille_salariale INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_poste            INT NOT NULL,
    id_departement      INT,
    niveau              VARCHAR(150),
    salaire_min         DECIMAL(10,2) NOT NULL,
    salaire_max         DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_poste)       REFERENCES poste(id_poste),
    FOREIGN KEY (id_departement) REFERENCES departement(id_departement)
);

CREATE TABLE budget_departement (
    id_budget_departement INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_departement        INT NOT NULL,
    periode               VARCHAR(20) NOT NULL,
    montant_alloue        DECIMAL(10,2) NOT NULL,
    montant_max           DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_departement) REFERENCES departement(id_departement)
);

CREATE TABLE promotion (
    id_promotion     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_employe       INT NOT NULL,
    id_ancien_poste  INT NOT NULL,
    id_nouveau_poste INT NOT NULL,
    date_effet       DATE NOT NULL,
    FOREIGN KEY (id_employe)       REFERENCES employe(id_employe),
    FOREIGN KEY (id_ancien_poste)  REFERENCES poste(id_poste),
    FOREIGN KEY (id_nouveau_poste) REFERENCES poste(id_poste)
);

CREATE TABLE critere_recrutement (
    id_critere_recrutement INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    annee_experience       INT,
    niveau_etude           VARCHAR(150) CHECK (niveau_etude IN ('LICENCE', 'MASTER', 'PHD')),
    missions               VARCHAR(500),
    competence             VARCHAR(500)
);

CREATE TABLE demande_besoin (
    id_demande_besoin INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero            VARCHAR(30) NOT NULL UNIQUE,
    id_demandeur      INT NOT NULL,
    id_departement    INT NOT NULL,
    id_poste          INT NOT NULL,
    id_critere_recrutement INT NOT NULL,
    nombre_postes     INT NOT NULL DEFAULT 1 CHECK (nombre_postes >= 1),
    motif             VARCHAR(20) NOT NULL CHECK (motif IN ('REMPLACEMENT', 'RENFORT', 'CREATION')),
    niveau_urgence    VARCHAR(20) NOT NULL CHECK (niveau_urgence IN ('CRITIQUE', 'URGENT')),
    id_type_contrat   INT NOT NULL,
    duree_mois        INT,
    salaire_propose   NUMERIC(14,2) NOT NULL CHECK (salaire_propose >= 0),
    date_souhaitee    DATE NOT NULL,
    justification     TEXT,
    statut            VARCHAR(30) NOT NULL DEFAULT 'BROUILLON',
    FOREIGN KEY (id_demandeur)    REFERENCES employe(id_employe),
    FOREIGN KEY (id_departement)  REFERENCES departement(id_departement),
    FOREIGN KEY (id_poste)        REFERENCES poste(id_poste),
    FOREIGN KEY (id_type_contrat) REFERENCES type_contrat(id_type_contrat),
    FOREIGN KEY (id_critere_recrutement) REFERENCES critere_recrutement(id_critere_recrutement)
);

CREATE TABLE circuit_validation (
    id_circuit_validation INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    etape                 VARCHAR(20) NOT NULL UNIQUE,
    role                  VARCHAR(30) NOT NULL CHECK (role IN ('RESPONSABLE', 'RH', 'FINANCE', 'DG')),
    obligatoire           BOOLEAN NOT NULL DEFAULT TRUE,
    delai_jours           INT NOT NULL DEFAULT 5
);

CREATE TABLE validation_etape (
    id_validation_etape INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_demande_besoin   INT NOT NULL,
    etape               VARCHAR(20) NOT NULL,
    id_validateur       INT,
    decision            VARCHAR(10) CHECK (decision IN ('VALIDEE', 'RENVOYEE', 'REJETEE')),
    commentaire         TEXT,
    date_reception      TIMESTAMP NOT NULL DEFAULT NOW(),
    date_decision       TIMESTAMP,
    FOREIGN KEY (id_demande_besoin) REFERENCES demande_besoin(id_demande_besoin),
    FOREIGN KEY (id_validateur)     REFERENCES employe(id_employe)
);

CREATE TABLE historique_modification (
    id_historique_modification INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_demande_besoin          INT NOT NULL,
    id_auteur                  INT NOT NULL,
    remarque                   VARCHAR(150),
    date_modification          TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (id_demande_besoin) REFERENCES demande_besoin(id_demande_besoin),
    FOREIGN KEY (id_auteur)         REFERENCES employe(id_employe)
);

CREATE TABLE notification (
    id_notification   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_destinataire   INT NOT NULL,
    id_demande_besoin INT,
    messages           TEXT NOT NULL,
    date_envoi        TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (id_destinataire)   REFERENCES employe(id_employe),
    FOREIGN KEY (id_demande_besoin) REFERENCES demande_besoin(id_demande_besoin)
);