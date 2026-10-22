-- Données de démonstration du Bloc 3.
-- Exécuter après migrations/bloc3_faneva.sql.
-- Les identifiants des blocs 2 et les utilisateurs sont des valeurs d'exemple.

INSERT INTO b3_selection
    (id_candidature_b2, id_offre_b2, id_candidat_b2, etat_dossier,
     decision, justification, decide_par, decide_le,
     validation_obligatoire, etat_validation, valide_par, valide_le)
VALUES
    (92001, 81001, 71001, 'COMPLET', 'RETENU',
     'Meilleur classement après étude du dossier et des entretiens.',
     102, DATE '2026-10-20', TRUE, 'VALIDEE', 103, DATE '2026-10-20'),
    (92002, 81001, 71002, 'INCOMPLET', 'EN_ATTENTE', NULL,
     102, DATE '2026-10-20', FALSE, 'NON_REQUISE', NULL, NULL)
ON CONFLICT (id_candidature_b2) DO NOTHING;

INSERT INTO b3_proposition
    (id_selection, numero_version, intitule_poste, type_contrat,
     salaire_mensuel, date_debut_prevue, etat_proposition, cree_par,
     cree_le, envoyee_le, date_limite_reponse, reponse_candidat,
     reponse_le, commentaire_reponse)
SELECT s.id_selection, 1, 'Développeur web', 'CDI', 2500000.00,
       DATE '2026-11-02', 'ACCEPTEE', 102, DATE '2026-10-20',
       DATE '2026-10-20', DATE '2026-10-25', 'ACCEPTEE',
       DATE '2026-10-21', 'Proposition acceptée par le candidat.'
FROM b3_selection s
WHERE s.id_candidature_b2 = 92001
ON CONFLICT (id_selection, numero_version) DO NOTHING;

INSERT INTO b3_recrutement
    (id_selection, nombre_postes, etat_recrutement, confirme_par,
     confirme_le, etat_transfert, transfert_le, reference_bloc4)
SELECT s.id_selection, 2, 'TRANSMIS_BLOC4', 102,
       DATE '2026-10-21', 'TRANSMIS', DATE '2026-10-21', 'B4-TEST-REC-001'
FROM b3_selection s
WHERE s.id_candidature_b2 = 92001
ON CONFLICT (id_selection) DO NOTHING;

INSERT INTO b3_historique
    (type_element, id_element, action, auteur_id, commentaire, date_action)
SELECT 'SELECTION', s.id_selection, 'DECISION_VALIDEE', 103,
       'Décision validée avant l’envoi de la proposition.', TIMESTAMP '2026-10-20 10:30:00'
FROM b3_selection s
WHERE s.id_candidature_b2 = 92001
  AND NOT EXISTS (
      SELECT 1 FROM b3_historique h
      WHERE h.type_element = 'SELECTION'
        AND h.id_element = s.id_selection
        AND h.action = 'DECISION_VALIDEE'
  );

INSERT INTO b3_historique
    (type_element, id_element, action, auteur_id, commentaire, date_action)
SELECT 'RECRUTEMENT', r.id_recrutement, 'TRANSFERT_BLOC4_REUSSI', 102,
       'Dossier confirmé et transmis au Bloc 4.', TIMESTAMP '2026-10-21 09:00:00'
FROM b3_recrutement r
JOIN b3_selection s ON s.id_selection = r.id_selection
WHERE s.id_candidature_b2 = 92001
  AND NOT EXISTS (
      SELECT 1 FROM b3_historique h
      WHERE h.type_element = 'RECRUTEMENT'
        AND h.id_element = r.id_recrutement
        AND h.action = 'TRANSFERT_BLOC4_REUSSI'
  );

-- Requêtes de vérification des exemples :
SELECT * FROM b3_v_suivi_recrutement ORDER BY id_selection, numero_version;
SELECT * FROM b3_v_dossiers_a_traiter ORDER BY id_selection;
