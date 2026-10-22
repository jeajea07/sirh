-- Jeu de données de test pour le bloc 2B.
-- À exécuter après bloc2.sql.

INSERT INTO candidat (identifiant, nom, prenom, email, telephone, mot_de_passe, actif, acces_cree)
VALUES
('CAND001', 'Rakoto', 'Jean', 'jean.rakoto@example.com', '0341234567', '$2a$10$abcdefghijklmnopqrstuv', TRUE, TRUE),
('CAND002', 'Rabe', 'Marie', 'marie.rabe@example.com', '0347654321', '$2a$10$abcdefghijklmnopqrstuv', TRUE, FALSE)
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
