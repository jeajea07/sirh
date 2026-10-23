-- Bloc 3 — tables de Faneva.
-- Les identifiants *_b2 et les utilisateurs sont transmis par les autres blocs.
-- Ils n'ont pas de clé étrangère : les blocs peuvent utiliser des bases différentes.
-- Exécuter les commandes dans l'ordre, après avoir sélectionné la base voulue.

-- Tables de référence : les tables métier enregistrent des identifiants,
-- pas les libellés des états, des décisions ou des réponses.
CREATE TABLE IF NOT EXISTS b3_etat_dossier (
    id_etat_dossier SERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    libelle VARCHAR(60) NOT NULL
);

CREATE TABLE IF NOT EXISTS b3_type_decision (
    id_type_decision SERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    libelle VARCHAR(60) NOT NULL
);

CREATE TABLE IF NOT EXISTS b3_etat_validation (
    id_etat_validation SERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    libelle VARCHAR(60) NOT NULL
);

CREATE TABLE IF NOT EXISTS b3_etat_proposition (
    id_etat_proposition SERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    libelle VARCHAR(60) NOT NULL
);

CREATE TABLE IF NOT EXISTS b3_type_reponse (
    id_type_reponse SERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    libelle VARCHAR(60) NOT NULL
);

CREATE TABLE IF NOT EXISTS b3_type_contrat (
    id_type_contrat SERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    libelle VARCHAR(60) NOT NULL
);

CREATE TABLE IF NOT EXISTS b3_etat_recrutement (
    id_etat_recrutement SERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    libelle VARCHAR(60) NOT NULL
);

CREATE TABLE IF NOT EXISTS b3_etat_transfert (
    id_etat_transfert SERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    libelle VARCHAR(60) NOT NULL
);

-- Valeurs de référence initiales. Les libellés peuvent être adaptés sans
-- modifier les tables métier ni leur structure.
INSERT INTO b3_etat_dossier (code, libelle) VALUES
    ('A_VERIFIER', 'À vérifier'), ('COMPLET', 'Complet'),
    ('INCOMPLET', 'Incomplet'), ('INCOHERENT', 'Incohérent')
ON CONFLICT (code) DO NOTHING;

INSERT INTO b3_type_decision (code, libelle) VALUES
    ('RETENU', 'Retenu'), ('NON_RETENU', 'Non retenu'),
    ('EN_ATTENTE', 'En attente'), ('LISTE_ATTENTE', 'Liste d''attente'),
    ('CLOTURE', 'Clôturé')
ON CONFLICT (code) DO NOTHING;

INSERT INTO b3_etat_validation (code, libelle) VALUES
    ('NON_REQUISE', 'Non requise'), ('EN_ATTENTE', 'En attente'),
    ('VALIDEE', 'Validée'), ('REFUSEE', 'Refusée')
ON CONFLICT (code) DO NOTHING;

INSERT INTO b3_etat_proposition (code, libelle) VALUES
    ('A_PREPARER', 'À préparer'), ('A_VALIDER', 'À valider'),
    ('ENVOYEE', 'Envoyée'), ('ACCEPTEE', 'Acceptée'),
    ('REFUSEE', 'Refusée'), ('A_REVISER', 'À réviser'), ('EXPIREE', 'Expirée')
ON CONFLICT (code) DO NOTHING;

INSERT INTO b3_type_reponse (code, libelle) VALUES
    ('ACCEPTEE', 'Acceptée'), ('REFUSEE', 'Refusée'),
    ('PRECISION', 'Demande de précision'), ('MODIFICATION', 'Demande de modification')
ON CONFLICT (code) DO NOTHING;

INSERT INTO b3_type_contrat (code, libelle) VALUES
    ('CDI', 'Contrat à durée indéterminée'), ('CDD', 'Contrat à durée déterminée'),
    ('STAGE', 'Stage'), ('AUTRE', 'Autre')
ON CONFLICT (code) DO NOTHING;

INSERT INTO b3_etat_recrutement (code, libelle) VALUES
    ('NON_CONFIRME', 'Non confirmé'), ('CONFIRME', 'Confirmé'),
    ('ANNULE', 'Annulé'), ('TRANSMIS_BLOC4', 'Transmis au Bloc 4')
ON CONFLICT (code) DO NOTHING;

INSERT INTO b3_etat_transfert (code, libelle) VALUES
    ('A_TRANSMETTRE', 'À transmettre'), ('TRANSMIS', 'Transmis'), ('ECHEC', 'Échec')
ON CONFLICT (code) DO NOTHING;

-- Dossier reçu et décision finale. Les résultats du Bloc 2 restent dans sa base.
CREATE TABLE IF NOT EXISTS b3_selection (
    id_selection           SERIAL PRIMARY KEY,
    id_candidature_b2      INT NOT NULL UNIQUE,
    id_offre_b2            INT NOT NULL,
    id_candidat_b2         INT NOT NULL,
    score_test             NUMERIC(5,2),
    score_entretien_rh     NUMERIC(5,2),
    score_entretien_tech   NUMERIC(5,2),
    score_entretien_dg     NUMERIC(5,2),
    score_final            NUMERIC(5,2),
    rang_classement        INT CHECK (rang_classement IS NULL OR rang_classement > 0),
    id_etat_dossier        INT NOT NULL REFERENCES b3_etat_dossier(id_etat_dossier),
    elements_manquants    TEXT,
    id_type_decision       INT REFERENCES b3_type_decision(id_type_decision),
    justification          TEXT,
    rang_liste_attente     INT CHECK (rang_liste_attente IS NULL OR rang_liste_attente > 0),
    decide_par             INT,
    decide_le              DATE,
    validation_obligatoire BOOLEAN NOT NULL DEFAULT FALSE,
    id_etat_validation     INT NOT NULL REFERENCES b3_etat_validation(id_etat_validation),
    valide_par             INT,
    valide_le              DATE,
    CONSTRAINT ck_b3_score_test CHECK (score_test IS NULL OR score_test BETWEEN 0 AND 20),
    CONSTRAINT ck_b3_score_entretien_rh CHECK (score_entretien_rh IS NULL OR score_entretien_rh BETWEEN 0 AND 5),
    CONSTRAINT ck_b3_score_entretien_tech CHECK (score_entretien_tech IS NULL OR score_entretien_tech BETWEEN 0 AND 5),
    CONSTRAINT ck_b3_score_entretien_dg CHECK (score_entretien_dg IS NULL OR score_entretien_dg BETWEEN 0 AND 5),
    CONSTRAINT ck_b3_score_final CHECK (score_final IS NULL OR score_final BETWEEN 0 AND 5)
);

-- Une proposition par version. La réponse est conservée à part ci-dessous.
CREATE TABLE IF NOT EXISTS b3_proposition (
    id_proposition       SERIAL PRIMARY KEY,
    id_selection         INT NOT NULL REFERENCES b3_selection(id_selection),
    numero_version       INT NOT NULL CHECK (numero_version > 0),
    intitule_poste       VARCHAR(150) NOT NULL,
    id_type_contrat      INT NOT NULL REFERENCES b3_type_contrat(id_type_contrat),
    salaire_mensuel      NUMERIC(12,2) NOT NULL CHECK (salaire_mensuel >= 0),
    date_debut_prevue    DATE NOT NULL,
    duree_mois           INT CHECK (duree_mois IS NULL OR duree_mois > 0),
    id_etat_proposition  INT NOT NULL REFERENCES b3_etat_proposition(id_etat_proposition),
    cree_par             INT NOT NULL,
    cree_le              DATE NOT NULL DEFAULT CURRENT_DATE,
    envoyee_le           DATE,
    date_limite_reponse  DATE,
    CONSTRAINT uq_b3_proposition_version UNIQUE (id_selection, numero_version),
    CONSTRAINT ck_b3_proposition_dates
        CHECK (date_limite_reponse IS NULL OR envoyee_le IS NULL OR date_limite_reponse > envoyee_le)
);

CREATE TABLE IF NOT EXISTS b3_reponse_candidat (
    id_reponse       SERIAL PRIMARY KEY,
    id_proposition   INT NOT NULL REFERENCES b3_proposition(id_proposition),
    id_type_reponse  INT NOT NULL REFERENCES b3_type_reponse(id_type_reponse),
    reponse_le       DATE NOT NULL DEFAULT CURRENT_DATE,
    commentaire      TEXT
);

-- Confirmation du recrutement et résultat du transfert vers le Bloc 4.
CREATE TABLE IF NOT EXISTS b3_recrutement (
    id_recrutement       SERIAL PRIMARY KEY,
    id_selection         INT NOT NULL UNIQUE REFERENCES b3_selection(id_selection),
    nombre_postes        INT NOT NULL CHECK (nombre_postes > 0),
    id_etat_recrutement  INT NOT NULL REFERENCES b3_etat_recrutement(id_etat_recrutement),
    confirme_par         INT,
    confirme_le          DATE,
    motif_annulation     TEXT,
    id_etat_transfert    INT NOT NULL REFERENCES b3_etat_transfert(id_etat_transfert),
    transfert_le         DATE,
    reference_bloc4      VARCHAR(50),
    erreur_transfert     TEXT
);

-- Historique simple des principales actions effectuées dans le Bloc 3.
CREATE TABLE IF NOT EXISTS b3_historique (
    id_historique   SERIAL PRIMARY KEY,
    type_element    VARCHAR(20) NOT NULL,
    id_element      INT NOT NULL,
    action          VARCHAR(50) NOT NULL,
    auteur_id       INT,
    commentaire     TEXT,
    date_action     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Vue de suivi : elle affiche les libellés des tables de référence.
CREATE OR REPLACE VIEW b3_v_suivi_recrutement AS
SELECT s.id_selection,
       s.id_candidature_b2,
       s.id_offre_b2,
       s.id_candidat_b2,
       s.score_test,
       s.score_entretien_rh,
       s.score_entretien_tech,
       s.score_entretien_dg,
       s.score_final,
       s.rang_classement,
       ed.libelle AS etat_dossier,
       d.libelle AS decision,
       ev.libelle AS etat_validation,
       p.id_proposition,
       p.numero_version,
       ep.libelle AS etat_proposition,
       tr.libelle AS reponse_candidat,
       er.libelle AS etat_recrutement,
       et.libelle AS etat_transfert,
       r.reference_bloc4
FROM b3_selection s
JOIN b3_etat_dossier ed ON ed.id_etat_dossier = s.id_etat_dossier
LEFT JOIN b3_type_decision d ON d.id_type_decision = s.id_type_decision
JOIN b3_etat_validation ev ON ev.id_etat_validation = s.id_etat_validation
LEFT JOIN b3_proposition p ON p.id_selection = s.id_selection
LEFT JOIN b3_etat_proposition ep ON ep.id_etat_proposition = p.id_etat_proposition
LEFT JOIN b3_reponse_candidat rc ON rc.id_proposition = p.id_proposition
LEFT JOIN b3_type_reponse tr ON tr.id_type_reponse = rc.id_type_reponse
LEFT JOIN b3_recrutement r ON r.id_selection = s.id_selection
LEFT JOIN b3_etat_recrutement er ON er.id_etat_recrutement = r.id_etat_recrutement
LEFT JOIN b3_etat_transfert et ON et.id_etat_transfert = r.id_etat_transfert;

-- Vue de consultation des dossiers et des informations manquantes.
CREATE OR REPLACE VIEW b3_v_dossiers_selection AS
SELECT s.id_selection,
       s.id_candidature_b2,
       s.id_offre_b2,
       s.id_candidat_b2,
       ed.code AS code_etat_dossier,
       ed.libelle AS etat_dossier,
       s.elements_manquants,
       d.code AS code_decision,
       d.libelle AS decision,
       ev.code AS code_etat_validation,
       ev.libelle AS etat_validation
FROM b3_selection s
JOIN b3_etat_dossier ed ON ed.id_etat_dossier = s.id_etat_dossier
LEFT JOIN b3_type_decision d ON d.id_type_decision = s.id_type_decision
JOIN b3_etat_validation ev ON ev.id_etat_validation = s.id_etat_validation;

-- Exemples de consultation avec SELECT :
-- SELECT * FROM b3_v_suivi_recrutement ORDER BY id_selection, numero_version;
-- SELECT * FROM b3_v_dossiers_selection ORDER BY id_selection;
