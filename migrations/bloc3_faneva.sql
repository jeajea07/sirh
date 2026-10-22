-- Bloc 3 — tables de Faneva.
-- Les identifiants *_b2 et les identifiants des utilisateurs sont transmis
-- par les autres blocs. Ils n'ont pas de clé étrangère car les blocs peuvent
-- utiliser des bases différentes.
-- Exécuter les commandes dans l'ordre, après avoir sélectionné la base voulue.

CREATE TABLE IF NOT EXISTS b3_selection (
    id_selection          SERIAL PRIMARY KEY,
    id_candidature_b2     INT NOT NULL UNIQUE,
    id_offre_b2           INT NOT NULL,
    id_candidat_b2        INT NOT NULL,
    etat_dossier          VARCHAR(20) NOT NULL DEFAULT 'A_VERIFIER'
                          CHECK (etat_dossier IN ('A_VERIFIER', 'COMPLET', 'INCOMPLET', 'INCOHERENT')),
    elements_manquants    TEXT,
    decision              VARCHAR(20)
                          CHECK (decision IN ('RETENU', 'NON_RETENU', 'EN_ATTENTE', 'LISTE_ATTENTE', 'CLOTURE')),
    justification         TEXT,
    rang_liste_attente    INT CHECK (rang_liste_attente IS NULL OR rang_liste_attente > 0),
    decide_par            INT,
    decide_le             DATE,
    validation_obligatoire BOOLEAN NOT NULL DEFAULT FALSE,
    etat_validation       VARCHAR(20) NOT NULL DEFAULT 'NON_REQUISE'
                          CHECK (etat_validation IN ('NON_REQUISE', 'EN_ATTENTE', 'VALIDEE', 'REFUSEE')),
    valide_par            INT,
    valide_le             DATE,
    CONSTRAINT ck_b3_selection_justification
        CHECK (decision NOT IN ('NON_RETENU', 'CLOTURE')
               OR NULLIF(BTRIM(justification), '') IS NOT NULL),
    CONSTRAINT ck_b3_selection_validation
        CHECK ((validation_obligatoire AND etat_validation <> 'NON_REQUISE')
            OR (NOT validation_obligatoire AND etat_validation = 'NON_REQUISE'))
);

CREATE TABLE IF NOT EXISTS b3_proposition (
    id_proposition        SERIAL PRIMARY KEY,
    id_selection          INT NOT NULL REFERENCES b3_selection(id_selection),
    numero_version        INT NOT NULL CHECK (numero_version > 0),
    intitule_poste        VARCHAR(150) NOT NULL,
    type_contrat          VARCHAR(20) NOT NULL
                          CHECK (type_contrat IN ('CDI', 'CDD', 'STAGE', 'AUTRE')),
    salaire_mensuel       NUMERIC(12,2) NOT NULL CHECK (salaire_mensuel >= 0),
    date_debut_prevue     DATE NOT NULL,
    duree_mois            INT CHECK (duree_mois IS NULL OR duree_mois > 0),
    etat_proposition      VARCHAR(20) NOT NULL DEFAULT 'A_PREPARER'
                          CHECK (etat_proposition IN ('A_PREPARER', 'A_VALIDER', 'ENVOYEE', 'ACCEPTEE', 'REFUSEE', 'A_REVISER', 'EXPIREE')),
    cree_par              INT NOT NULL,
    cree_le               DATE NOT NULL DEFAULT CURRENT_DATE,
    envoyee_le            DATE,
    date_limite_reponse   DATE,
    reponse_candidat      VARCHAR(20)
                          CHECK (reponse_candidat IN ('ACCEPTEE', 'REFUSEE', 'PRECISION', 'MODIFICATION')),
    reponse_le            DATE,
    commentaire_reponse   TEXT,
    CONSTRAINT uq_b3_proposition_version UNIQUE (id_selection, numero_version),
    CONSTRAINT ck_b3_proposition_cdd
        CHECK (type_contrat <> 'CDD' OR duree_mois IS NOT NULL),
    CONSTRAINT ck_b3_proposition_dates
        CHECK (date_limite_reponse IS NULL OR envoyee_le IS NULL OR date_limite_reponse > envoyee_le)
);

CREATE TABLE IF NOT EXISTS b3_recrutement (
    id_recrutement        SERIAL PRIMARY KEY,
    id_selection          INT NOT NULL UNIQUE REFERENCES b3_selection(id_selection),
    nombre_postes         INT NOT NULL CHECK (nombre_postes > 0),
    etat_recrutement      VARCHAR(20) NOT NULL DEFAULT 'NON_CONFIRME'
                          CHECK (etat_recrutement IN ('NON_CONFIRME', 'CONFIRME', 'ANNULE', 'TRANSMIS_BLOC4')),
    confirme_par          INT,
    confirme_le           DATE,
    motif_annulation      TEXT,
    etat_transfert        VARCHAR(20) NOT NULL DEFAULT 'A_TRANSMETTRE'
                          CHECK (etat_transfert IN ('A_TRANSMETTRE', 'TRANSMIS', 'ECHEC')),
    transfert_le          DATE,
    reference_bloc4       VARCHAR(50),
    erreur_transfert      TEXT,
    CONSTRAINT ck_b3_recrutement_confirmation
        CHECK (etat_recrutement NOT IN ('CONFIRME', 'TRANSMIS_BLOC4')
               OR (confirme_par IS NOT NULL AND confirme_le IS NOT NULL)),
    CONSTRAINT ck_b3_recrutement_annulation
        CHECK (etat_recrutement <> 'ANNULE'
               OR NULLIF(BTRIM(motif_annulation), '') IS NOT NULL)
);

-- Historique distinct pour conserver les actions sans alourdir les tables métier.
CREATE TABLE IF NOT EXISTS b3_historique (
    id_historique         SERIAL PRIMARY KEY,
    type_element          VARCHAR(20) NOT NULL
                          CHECK (type_element IN ('SELECTION', 'PROPOSITION', 'RECRUTEMENT')),
    id_element            INT NOT NULL,
    action                VARCHAR(50) NOT NULL,
    auteur_id             INT,
    commentaire           TEXT,
    date_action           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Vue de suivi : une ligne par version de proposition, même sans recrutement.
CREATE OR REPLACE VIEW b3_v_suivi_recrutement AS
SELECT s.id_selection,
       s.id_candidature_b2,
       s.id_offre_b2,
       s.id_candidat_b2,
       s.etat_dossier,
       s.decision,
       s.etat_validation,
       p.id_proposition,
       p.numero_version,
       p.etat_proposition,
       p.reponse_candidat,
       r.etat_recrutement,
       r.etat_transfert,
       r.reference_bloc4
FROM b3_selection s
LEFT JOIN b3_proposition p ON p.id_selection = s.id_selection
LEFT JOIN b3_recrutement r ON r.id_selection = s.id_selection;

-- Vue pour repérer les dossiers qui attendent une action du RH.
CREATE OR REPLACE VIEW b3_v_dossiers_a_traiter AS
SELECT id_selection,
       id_candidature_b2,
       id_offre_b2,
       id_candidat_b2,
       etat_dossier,
       elements_manquants,
       decision,
       etat_validation
FROM b3_selection
WHERE etat_dossier IN ('A_VERIFIER', 'INCOMPLET', 'INCOHERENT')
   OR decision = 'EN_ATTENTE'
   OR etat_validation = 'EN_ATTENTE';

-- Exemples de consultation avec SELECT :
-- SELECT * FROM b3_v_suivi_recrutement ORDER BY id_selection, numero_version;
-- SELECT * FROM b3_v_dossiers_a_traiter ORDER BY id_selection;
