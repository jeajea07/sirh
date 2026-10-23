-- Bloc 3
-- Données de démonstration du Bloc 3.
-- Exécuter après migrations/bloc3_faneva.sql.
-- Les identifiants des blocs 2 et des utilisateurs sont des exemples.

INSERT INTO b3_selection
    (id_candidature_b2, id_offre_b2, id_candidat_b2,
     score_test, score_entretien_rh, score_entretien_tech, score_entretien_dg,
     score_final, rang_classement, id_etat_dossier,
     elements_manquants, id_type_decision, justification, decide_par, decide_le,
     validation_obligatoire, id_etat_validation, valide_par, valide_le)
SELECT exemple.id_candidature_b2,
       exemple.id_offre_b2,
       exemple.id_candidat_b2,
       exemple.score_test,
       exemple.score_entretien_rh,
       exemple.score_entretien_tech,
       exemple.score_entretien_dg,
       exemple.score_final,
       exemple.rang_classement,
       ed.id_etat_dossier,
       exemple.elements_manquants,
       d.id_type_decision,
       exemple.justification,
       exemple.decide_par,
       exemple.decide_le,
       exemple.validation_obligatoire,
       ev.id_etat_validation,
       exemple.valide_par,
       exemple.valide_le
FROM (VALUES
    (92001, 81001, 71001, 18.00, 4.50, 4.30, 4.60, 4.45, 1,
     'COMPLET', NULL::TEXT, 'RETENU',
     'Meilleur classement après étude du dossier et des entretiens.', 102,
     DATE '2026-10-20', TRUE, 'VALIDEE', 103, DATE '2026-10-20'),
    (92002, 81001, 71002, NULL::NUMERIC, NULL::NUMERIC, NULL::NUMERIC,
     NULL::NUMERIC, NULL::NUMERIC, NULL::INT,
     'INCOMPLET', 'Justificatif d’expérience manquant', 'EN_ATTENTE',
     NULL::TEXT, 102, DATE '2026-10-20', FALSE, 'NON_REQUISE', NULL::INT, NULL::DATE)
) AS exemple(id_candidature_b2, id_offre_b2, id_candidat_b2,
             score_test, score_entretien_rh, score_entretien_tech, score_entretien_dg,
             score_final, rang_classement, etat_dossier,
             elements_manquants, decision, justification, decide_par, decide_le,
             validation_obligatoire, etat_validation, valide_par, valide_le)
JOIN b3_etat_dossier ed ON ed.code = exemple.etat_dossier
JOIN b3_etat_validation ev ON ev.code = exemple.etat_validation
LEFT JOIN b3_type_decision d ON d.code = exemple.decision
ON CONFLICT (id_candidature_b2) DO NOTHING;

INSERT INTO b3_proposition
    (id_selection, numero_version, intitule_poste, id_type_contrat,
     salaire_mensuel, date_debut_prevue, id_etat_proposition, cree_par,
     cree_le, envoyee_le, date_limite_reponse)
SELECT s.id_selection,
       1,
       'Développeur web',
       tc.id_type_contrat,
       2500000.00,
       DATE '2026-11-02',
       ep.id_etat_proposition,
       102,
       DATE '2026-10-20',
       DATE '2026-10-20',
       DATE '2026-10-25'
FROM b3_selection s
JOIN b3_type_contrat tc ON tc.code = 'CDI'
JOIN b3_etat_proposition ep ON ep.code = 'ACCEPTEE'
WHERE s.id_candidature_b2 = 92001
ON CONFLICT (id_selection, numero_version) DO NOTHING;

INSERT INTO b3_reponse_candidat
    (id_proposition, id_type_reponse, reponse_le, commentaire)
SELECT p.id_proposition, rt.id_type_reponse, DATE '2026-10-21',
       'Proposition acceptée par le candidat.'
FROM b3_proposition p
JOIN b3_selection s ON s.id_selection = p.id_selection
JOIN b3_type_reponse rt ON rt.code = 'ACCEPTEE'
WHERE s.id_candidature_b2 = 92001
  AND NOT EXISTS (
      SELECT 1 FROM b3_reponse_candidat r
      WHERE r.id_proposition = p.id_proposition
        AND r.id_type_reponse = rt.id_type_reponse
  );

-- La confirmation et le transfert sont des états distincts.
INSERT INTO b3_recrutement
    (id_selection, nombre_postes, id_etat_recrutement, confirme_par,
     confirme_le, id_etat_transfert, transfert_le, reference_bloc4)
SELECT s.id_selection,
       2,
       er.id_etat_recrutement,
       102,
       DATE '2026-10-21',
       et.id_etat_transfert,
       DATE '2026-10-21',
       'B4-TEST-REC-001'
FROM b3_selection s
JOIN b3_etat_recrutement er ON er.code = 'TRANSMIS_BLOC4'
JOIN b3_etat_transfert et ON et.code = 'TRANSMIS'
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

-- Requêtes de consultation avec les libellés des tables de référence.
SELECT * FROM b3_v_suivi_recrutement ORDER BY id_selection, numero_version;
SELECT * FROM b3_v_dossiers_selection ORDER BY id_selection;
