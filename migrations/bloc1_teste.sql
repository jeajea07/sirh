INSERT INTO poste (designation) VALUES
('Directeur général'),
('Responsable RH'),
('Comptable'),
('Développeur');

INSERT INTO unite_organisationnelle (designation) VALUES
('DIRECTION'),
('DEPARTEMENT'),
('SERVICE');

INSERT INTO departement (designation, id_unite_organisationnelle, effectif) VALUES
('Direction générale', 1, 5),
('Ressources humaines', 2, 8),
('Comptabilité', 2, 4),
('Développement', 2, 4);

INSERT INTO employe (matricule, nom, prenom, email, id_departement, id_poste, date_embauche, actif) VALUES
('EMP001', 'Rakoto', 'Jean', 'jean.rakoto@entreprise.mg', 1, 1, '2020-01-15', TRUE),
('EMP002', 'Rabe', 'Marie', 'marie.rabe@entreprise.mg', 2, 2, '2021-03-10', TRUE),
('EMP003', 'Andria', 'Paul', 'paul.andria@entreprise.mg', 3, 3, '2022-06-01', TRUE),
('EMP004', 'Rasoa', 'Henika', 'henika.rasoa@entreprise.mg', 4, 4, '2022-06-01', TRUE);

INSERT INTO utilisateur (id_employe, mot_de_passe, role, actif) VALUES
(1, '$2b$10$RVta7ZfNJENwLJPeizevc.3rX./KiTaHnTK3rgO3FhDSVbsLz3wLq', 'DG', TRUE),
(2, '$2b$10$r2gKtwlCj2JE67xRt6WKTeZ4JRA.jF9qFgilolQGu6noQOFFsFMnW', 'RH', TRUE),
(3, '$2b$10$UQW8eEqoX.M9QD/842YA8OmpIiL.DeemDtXrzbBD9sBVMUJIgaagm', 'FINANCE', TRUE),
(4, '$2b$10$B.f2aXSQXRhEyeX8Mc8w/O5Xq8lOPM0YIq7Gfy9GpZrS65nMsA12a', 'EMPLOYE', TRUE);

INSERT INTO type_contrat (code, libelle) VALUES
('CDI', 'Contrat à durée indéterminée'),
('CDD', 'Contrat à durée déterminée'),
('CE', 'Contrat d''essai'),
('STAGE', 'Stage');

INSERT INTO circuit_validation (etape, role, obligatoire, delai_jours) VALUES
('RESPONSABLE', 'RESPONSABLE', TRUE, 3),
('RH', 'RH', TRUE, 5),
('FINANCE', 'FINANCE', TRUE, 5),
('DG', 'DG', TRUE, 5);