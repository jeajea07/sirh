-- =====================================================================
-- BLOC 2A : Offre, candidature, étude de dossier  (responsable : Mpiaro)
-- A exécuter APRES le script du bloc 1 (tables poste, departement,
-- employe, type_contrat, demande_besoin...).
-- =====================================================================

DROP TABLE IF EXISTS notification_recrutement CASCADE;
DROP TABLE IF EXISTS historique_candidature CASCADE;
DROP TABLE IF EXISTS cv_competence          CASCADE;
DROP TABLE IF EXISTS candidature            CASCADE;
DROP TABLE IF EXISTS candidat               CASCADE;
DROP TABLE IF EXISTS critere_offre          CASCADE;
DROP TABLE IF EXISTS offre                  CASCADE;

-- ---------------------------------------------------------------------
-- OFFRE : toujours créée depuis une demande acceptée (bloc 1)
-- Cycle : BROUILLON -> VALIDEE -> PUBLIEE -> TERMINEE (SUSPENDUE / ANNULEE)
-- ---------------------------------------------------------------------
CREATE TABLE offre (
    id_offre          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reference         VARCHAR(30)  NOT NULL UNIQUE,            -- ex : OFF-2026-0001
    id_demande_besoin INT          NOT NULL,                   -- une offre vient toujours d'une demande
    id_poste          INT          NOT NULL,
    id_departement    INT          NOT NULL,
    id_type_contrat   INT          NOT NULL,
    duree_mois        INT,
    titre             VARCHAR(200) NOT NULL,
    description       TEXT,
    missions          TEXT,
    salaire_propose   NUMERIC(14,2) CHECK (salaire_propose >= 0), -- affiché, non négociable
    nombre_postes     INT          NOT NULL DEFAULT 1 CHECK (nombre_postes >= 1),
    nb_candidatures   INT          NOT NULL DEFAULT 0,            -- mis à jour automatiquement
    statut            VARCHAR(20)  NOT NULL DEFAULT 'BROUILLON'
                      CHECK (statut IN ('BROUILLON','VALIDEE','PUBLIEE','TERMINEE','SUSPENDUE','ANNULEE')),
    date_creation     TIMESTAMP    NOT NULL DEFAULT NOW(),
    id_validateur     INT,                                        -- RH qui valide
    date_validation   TIMESTAMP,
    date_publication  TIMESTAMP,
    date_limite       DATE,
    FOREIGN KEY (id_demande_besoin) REFERENCES demande_besoin(id_demande_besoin),
    FOREIGN KEY (id_poste)          REFERENCES poste(id_poste),
    FOREIGN KEY (id_departement)    REFERENCES departement(id_departement),
    FOREIGN KEY (id_type_contrat)   REFERENCES type_contrat(id_type_contrat),
    FOREIGN KEY (id_validateur)     REFERENCES employe(id_employe),
    -- la date limite doit etre après la date de publication
    CHECK (date_limite IS NULL OR date_publication IS NULL
           OR date_limite > date_publication::date)
);

-- Une demande ne donne pas plusieurs offres en meme temps
CREATE UNIQUE INDEX uq_offre_demande_active
    ON offre(id_demande_besoin)
    WHERE statut NOT IN ('ANNULEE','TERMINEE');

CREATE INDEX idx_offre_statut_limite ON offre(statut, date_limite);

-- ---------------------------------------------------------------------
-- CRITERES de l'offre (obligatoires = classement ; souhaités = affichage)
--   NIVEAU_ETUDE : libelle = niveau requis (ex : 'LICENCE')
--   EXPERIENCE   : valeur_min = années minimum
--   COMPETENCE   : libelle = compétence (ex : 'JavaScript')
--   LANGUE       : libelle = langue (ex : 'Anglais')
-- ---------------------------------------------------------------------
CREATE TABLE critere_offre (
    id_critere_offre INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_offre         INT          NOT NULL,
    type_critere     VARCHAR(12)  NOT NULL CHECK (type_critere IN ('OBLIGATOIRE','SOUHAITE')),
    nature           VARCHAR(20)  NOT NULL CHECK (nature IN ('NIVEAU_ETUDE','EXPERIENCE','COMPETENCE','LANGUE')),
    libelle          VARCHAR(150),
    valeur_min       INT CHECK (valeur_min >= 0),
    FOREIGN KEY (id_offre) REFERENCES offre(id_offre) ON DELETE CASCADE,
    CHECK (nature <> 'EXPERIENCE' OR valeur_min IS NOT NULL),
    CHECK (nature =  'EXPERIENCE' OR libelle IS NOT NULL)
);

CREATE INDEX idx_critere_offre ON critere_offre(id_offre);

-- ---------------------------------------------------------------------
-- CANDIDAT (identité). Le compte de connexion pour le test technique
-- est créé par le bloc 2B (Edinah) dans sa propre table.
-- ---------------------------------------------------------------------
CREATE TABLE candidat (
    id_candidat   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nom           VARCHAR(100) NOT NULL,
    prenom        VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    telephone     VARCHAR(30),
    adresse       VARCHAR(255),
    date_creation TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------
-- CANDIDATURE (CV numérique + fichier du CV)
-- id_offre NULL + spontanee = TRUE  -> candidature dans la réserve
-- Les statuts après DOSSIER_VALIDE sont mis à jour par le bloc 2B / bloc 3.
-- ---------------------------------------------------------------------
CREATE TABLE candidature (
    id_candidature    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero            VARCHAR(30) NOT NULL UNIQUE,              -- ex : CAND-2026-00001
    id_candidat       INT NOT NULL,
    id_offre          INT,
    spontanee         BOOLEAN NOT NULL DEFAULT FALSE,
    date_rattachement TIMESTAMP,                                -- rattachement d'une spontanée
    date_reception    TIMESTAMP NOT NULL DEFAULT NOW(),

    -- CV numérique (champs de départ, à confirmer par l'équipe)
    niveau_etude      VARCHAR(20) CHECK (niveau_etude IN ('AUCUN','BAC','LICENCE','MASTER','PHD')),
    annees_experience INT CHECK (annees_experience >= 0),
    formations        TEXT,
    experiences       TEXT,
    langues           VARCHAR(300),
    disponibilite     DATE,

    -- Fichier du CV : PDF ou DOCX, 5 Mo maximum
    cv_nom_fichier    VARCHAR(255) NOT NULL,
    cv_chemin         VARCHAR(500) NOT NULL,
    cv_type           VARCHAR(5)   NOT NULL CHECK (cv_type IN ('PDF','DOCX')),
    cv_taille_octets  BIGINT       NOT NULL CHECK (cv_taille_octets > 0 AND cv_taille_octets <= 5242880),

    -- Situation
    statut            VARCHAR(30) NOT NULL DEFAULT 'RECUE'
                      CHECK (statut IN ('RECUE','A_ETUDIER','DOSSIER_VALIDE','TEST_TECHNIQUE',
                                        'ENTRETIEN_RH','ENTRETIEN_TECHNICIEN','ENTRETIEN_DG',
                                        'CONFIRMATION_FINALE','RECRUTE','NON_RETENUE',
                                        'LISTE_ATTENTE','RETIREE')),

    -- Classement automatique puis décision du RH
    classement_auto   VARCHAR(15) CHECK (classement_auto  IN ('CONFORME','NON_CONFORME','A_REGARDER')),
    motif_classement  TEXT,                                     -- critères remplis / manquants
    classement_final  VARCHAR(15) CHECK (classement_final IN ('CONFORME','NON_CONFORME','A_REGARDER')),
    id_validateur     INT,                                      -- RH
    date_validation   TIMESTAMP,

    FOREIGN KEY (id_candidat)   REFERENCES candidat(id_candidat),
    FOREIGN KEY (id_offre)      REFERENCES offre(id_offre),
    FOREIGN KEY (id_validateur) REFERENCES employe(id_employe),
    -- une candidature a une offre, sauf spontanée non rattachée
    CHECK (spontanee OR id_offre IS NOT NULL)
);

-- Pas de double candidature pour la meme offre (une candidature retirée ne compte pas)
CREATE UNIQUE INDEX uq_candidature_candidat_offre
    ON candidature(id_candidat, id_offre)
    WHERE id_offre IS NOT NULL AND statut <> 'RETIREE';

CREATE INDEX idx_candidature_offre_statut ON candidature(id_offre, statut);
CREATE INDEX idx_candidature_classement   ON candidature(classement_auto);

-- Compétences du CV numérique (comparées aux critères COMPETENCE de l'offre)
CREATE TABLE cv_competence (
    id_cv_competence INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_candidature   INT NOT NULL,
    libelle          VARCHAR(100) NOT NULL,
    FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature) ON DELETE CASCADE
);

CREATE INDEX idx_cv_competence ON cv_competence(id_candidature);

-- ---------------------------------------------------------------------
-- HISTORIQUE des décisions et changements de situation
-- id_auteur NULL = action automatique du système
-- ---------------------------------------------------------------------
CREATE TABLE historique_candidature (
    id_historique_candidature INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_candidature            INT NOT NULL,
    action                    VARCHAR(40) NOT NULL,   -- ex : RECEPTION, CLASSEMENT_AUTO, VALIDATION_RH, RATTACHEMENT
    ancien_statut             VARCHAR(30),
    nouveau_statut            VARCHAR(30),
    ancien_classement         VARCHAR(15),
    nouveau_classement        VARCHAR(15),
    id_auteur                 INT,
    commentaire               TEXT,
    date_action               TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature),
    FOREIGN KEY (id_auteur)      REFERENCES employe(id_employe)
);

CREATE INDEX idx_hist_candidature ON historique_candidature(id_candidature, date_action);

-- ---------------------------------------------------------------------
-- NOTIFICATION_RECRUTEMENT : notifications et e-mails du recrutement
-- (la table notification du bloc 1 reste réservée aux demandes de besoin)
--   * destinataire INTERNE (RH, direction...) : id_destinataire_employe
--   * destinataire CANDIDAT                   : id_candidat (canal EMAIL)
-- Exactement un des deux est renseigné.
-- Utilisable aussi par le bloc 2B : type_notification est libre (pas de CHECK).
-- Exemples de type : ACCUSE_RECEPTION, NOUVELLE_CANDIDATURE, RAPPEL_DOSSIERS,
--   OFFRE_FERMEE, CONVOCATION_TEST, REPONSE_NON_CONFORME
-- ---------------------------------------------------------------------
CREATE TABLE notification_recrutement (
    id_notification_recrutement INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_destinataire_employe     INT,
    id_candidat                 INT,
    id_offre                    INT,
    id_candidature              INT,
    type_notification           VARCHAR(40)  NOT NULL,
    canal                       VARCHAR(10)  NOT NULL DEFAULT 'INTERNE'
                                CHECK (canal IN ('INTERNE','EMAIL')),
    objet                       VARCHAR(200),
    message                     TEXT         NOT NULL,
    statut_envoi                VARCHAR(12)  NOT NULL DEFAULT 'EN_ATTENTE'
                                CHECK (statut_envoi IN ('EN_ATTENTE','ENVOYE','ECHEC')),
    erreur                      TEXT,
    lu                          BOOLEAN      NOT NULL DEFAULT FALSE,
    date_creation               TIMESTAMP    NOT NULL DEFAULT NOW(),
    date_envoi                  TIMESTAMP,
    FOREIGN KEY (id_destinataire_employe) REFERENCES employe(id_employe),
    FOREIGN KEY (id_candidat)             REFERENCES candidat(id_candidat),
    FOREIGN KEY (id_offre)                REFERENCES offre(id_offre),
    FOREIGN KEY (id_candidature)          REFERENCES candidature(id_candidature),
    -- un seul type de destinataire
    CHECK ((id_destinataire_employe IS NULL) <> (id_candidat IS NULL)),
    -- un candidat est toujours prévenu par e-mail
    CHECK (id_candidat IS NULL OR canal = 'EMAIL')
);

CREATE INDEX idx_notif_rec_employe ON notification_recrutement(id_destinataire_employe, lu);
CREATE INDEX idx_notif_rec_candidat ON notification_recrutement(id_candidat);
CREATE INDEX idx_notif_rec_envoi    ON notification_recrutement(statut_envoi);