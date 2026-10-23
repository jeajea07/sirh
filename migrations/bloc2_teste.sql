-- Jeu de données BLOC 2B, à exécuter après b2a_donnees_test.sql et bloc2.sql.
-- Utilise une candidature existante du seed BLOC 2A; n'ajoute pas de candidat,
-- offre ou candidature concurrente.

INSERT INTO test_technique (id_candidature, statut, seuil_note, note_generale)
SELECT id_candidature, 'EN_COURS', 2.00, 0.00
FROM candidature
WHERE numero = 'CAND-2099-90001'
ON CONFLICT (id_candidature) DO NOTHING;

INSERT INTO reponse_test_technique
    (id_test_technique, question_ref, type_question, reponse_textuelle, note, corrigee, correct)
SELECT t.id_test_technique, q.question_ref, q.type_question, q.reponse_textuelle, q.note, q.corrigee, q.correct
FROM test_technique t
JOIN candidature c ON c.id_candidature = t.id_candidature
CROSS JOIN (VALUES
    ('Q1', 'CHOIX', 'Java', 1.00::NUMERIC, TRUE, TRUE),
    ('Q2', 'CHOIX', 'BigDecimal', 1.00::NUMERIC, TRUE, TRUE),
    ('Q3', 'LIBRE', 'Validation des entrées et résultats.', 0.00::NUMERIC, FALSE, FALSE)
) AS q(question_ref, type_question, reponse_textuelle, note, corrigee, correct)
WHERE c.numero = 'CAND-2099-90001'
ON CONFLICT (id_test_technique, question_ref) DO NOTHING;
