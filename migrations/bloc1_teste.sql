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
('EMP001', 'Rakoto', 'Jean', INSERT INTO poste (designation) VALUES
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

INSERT INTO grille_salariale (id_poste, id_departement, id_type_contrat, niveau, salaire) VALUES
-- Développeur (poste 4), département Développement (4)
(4, 4, 1, 'Licence',  1800000),   -- CDI
(4, 4, 1, 'Master',   2500000),
(4, 4, 1, 'Doctorat', 3500000),
(4, 4, 2, 'Licence',  1600000),   -- CDD
(4, 4, 2, 'Master',   2200000),
(4, 4, 3, NULL,       1000000),   -- Contrat d'essai, tous niveaux
(4, 4, 4, NULL,        500000),   -- Stage, tous niveaux

-- Développeur, règle générale (tous départements)
(4, NULL, 1, NULL,    2000000),
(4, NULL, 2, NULL,    1700000),

-- Comptable (poste 3), département Comptabilité (3)
(3, 3, 1, 'Licence',  1500000),
(3, 3, 1, 'Master',   2200000),
(3, 3, 2, 'Licence',  1300000),
(3, 3, 3, NULL,        900000),
(3, 3, 4, NULL,        450000),

-- Responsable RH (poste 2), département Ressources humaines (2)
(2, 2, 1, 'Master',   3000000),
(2, 2, 1, 'Doctorat', 4000000),
(2, 2, 2, 'Master',   2600000),
(2, 2, 3, NULL,       1200000),

-- Directeur général (poste 1), Direction générale (1)
(1, 1, 1, NULL,       8000000),

-- Comptable embauché dans un autre département (règle générale, sans niveau)
(3, NULL, 1, NULL,    1800000);'jean.rakoto@entreprise.mg', 1, 1, '2020-01-15', TRUE),
('EMP002', 'Rabe', 'Marie', 'marie.rabe@entreprise.mg', 2, 2, '2021-03-10', TRUE),
('EMP003', 'Andria', 'Paul', 'paul.andria@entreprise.mg', 3, 3, '2022-06-01', TRUE),
('EMP004', 'Rasoa', 'Henika', 'henika.rasoa@entreprise.mg', 4, 4, '2022-06-01', TRUE);

INSERT INTO utilisateur (id_employe, mot_de_passe, role, actif) VALUES
(1, '$2b$10$RVta7ZfNJENwLJPeizevc.3rX./KiTaHnTK3rgO3FhDSVbsLz3wLq', 'DG', TRUE), --DGpassword123
(2, '$2b$10$r2gKtwlCj2JE67xRt6WKTeZ4JRA.jF9qFgilolQGu6noQOFFsFMnW', 'RH', TRUE), --RHpassword123
(3, '$2b$10$UQW8eEqoX.M9QD/842YA8OmpIiL.DeemDtXrzbBD9sBVMUJIgaagm', 'FINANCE', TRUE), --FINANCEpassword123
(4, '$2b$10$B.f2aXSQXRhEyeX8Mc8w/O5Xq8lOPM0YIq7Gfy9GpZrS65nMsA12a', 'EMPLOYE', TRUE); --EMPLOYEpassword123

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

INSERT INTO grille_salariale (id_poste, id_departement, id_type_contrat, niveau, salaire) VALUES
-- Développeur (poste 4), département Développement (4)
(4, 4, 1, 'Licence',  1800000),   -- CDI
(4, 4, 1, 'Master',   2500000),
(4, 4, 1, 'Doctorat', 3500000),
(4, 4, 2, 'Licence',  1600000),   -- CDD
(4, 4, 2, 'Master',   2200000),
(4, 4, 3, NULL,       1000000),   -- Contrat d'essai, tous niveaux
(4, 4, 4, NULL,        500000),   -- Stage, tous niveaux

-- Développeur, règle générale (tous départements)
(4, NULL, 1, NULL,    2000000),
(4, NULL, 2, NULL,    1700000),

-- Comptable (poste 3), département Comptabilité (3)
(3, 3, 1, 'Licence',  1500000),
(3, 3, 1, 'Master',   2200000),
(3, 3, 2, 'Licence',  1300000),
(3, 3, 3, NULL,        900000),
(3, 3, 4, NULL,        450000),

-- Responsable RH (poste 2), département Ressources humaines (2)
(2, 2, 1, 'Master',   3000000),
(2, 2, 1, 'Doctorat', 4000000),
(2, 2, 2, 'Master',   2600000),
(2, 2, 3, NULL,       1200000),

-- Directeur général (poste 1), Direction générale (1)
(1, 1, 1, NULL,       8000000),

-- Comptable embauché dans un autre département (règle générale, sans niveau)
(3, NULL, 1, NULL,    1800000);

-- Si vous avez déjà exécuté budget_teste.sql, videz d'abord la table pour éviter les doublons :
-- DELETE FROM budget_departement;

INSERT INTO budget_departement (id_departement, periode, montant_alloue, montant_max) VALUES
-- 2026
(1, '2026', 99000000, 99999999),   -- Direction générale
(2, '2026', 40000000, 45000000),   -- Ressources humaines
(3, '2026', 30000000, 35000000),   -- Comptabilité
(4, '2026', 60000000, 70000000),   -- Développement

-- 2027
(1, '2027', 99000000, 99999999),
(2, '2027', 50000000, 55000000),
(3, '2027', 20000000, 25000000),   -- budget serré pour tester le dépassement
(4, '2027', 80000000, 90000000);