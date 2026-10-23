-- Jeu de démonstration du Bloc 4, rejouable sans doublon.
-- À exécuter après migrations/bloc4.sql, sur une base contenant le seed Bloc 1.

INSERT INTO organisme (code, nom, type, site_web) VALUES
  ('CNAPS', 'Caisse nationale de prévoyance sociale', 'SOCIAL', 'https://www.cnaps.mg'),
  ('OSTIE', 'Organisation sanitaire inter-entreprises', 'SANTE', 'https://www.ostie.mg'),
  ('IMPOT', 'Administration fiscale', 'FISCAL', NULL)
ON CONFLICT (code) DO UPDATE
SET nom = EXCLUDED.nom, type = EXCLUDED.type, site_web = EXCLUDED.site_web, actif = TRUE;

DO $$
DECLARE
    v_model INT;
    v_contrat INT;
    v_onboarding INT;
    v_t1 INT; v_t2 INT; v_t3 INT; v_t4 INT;
    v_t5 INT; v_t6 INT; v_t7 INT; v_t8 INT;
    v_t9 INT; v_t10 INT; v_t11 INT; v_t12 INT;
    v_t13 INT; v_t14 INT; v_t15 INT; v_t16 INT;
    v_rh INT;
BEGIN
    SELECT id_employe INTO v_rh FROM employe WHERE matricule = 'EMP002' LIMIT 1;
    IF v_rh IS NULL THEN
        RAISE EXCEPTION 'Seed Bloc 1 absent : EMP002 est introuvable.';
    END IF;

    SELECT id_modele_onboarding INTO v_model
      FROM modele_onboarding WHERE nom = 'Standard' AND version = 1 LIMIT 1;

    IF v_model IS NULL THEN
        INSERT INTO modele_onboarding (nom, description, id_departement, id_poste, version, actif, cree_par_id)
        VALUES ('Standard', 'Parcours d''intégration commun à tous les salariés.', NULL, NULL, 1, TRUE, v_rh)
        RETURNING id_modele_onboarding INTO v_model;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM modele_tache_onboarding WHERE id_modele_onboarding = v_model) THEN
        INSERT INTO modele_tache_onboarding (id_modele_onboarding, titre, description, role_responsable,
            delai_indicatif_jours, obligatoire, bloquante, ordre, condition_activation)
        VALUES (v_model, 'Vérifier les informations administratives', 'Contrôler l''identité et les pièces remises.', 'RH', 0, TRUE, TRUE, 1, 'TOUJOURS')
        RETURNING id_modele_tache_onboarding INTO v_t1;
        INSERT INTO modele_tache_onboarding (id_modele_onboarding, titre, description, role_responsable,
            delai_indicatif_jours, obligatoire, bloquante, ordre, condition_activation, depend_de_tache_id)
        VALUES (v_model, 'Préparer le projet de contrat', 'Préparer le contrat à partir de la proposition validée.', 'RH', 0, TRUE, FALSE, 2, 'TOUJOURS', v_t1)
        RETURNING id_modele_tache_onboarding INTO v_t2;
        INSERT INTO modele_tache_onboarding (id_modele_onboarding, titre, description, role_responsable,
            delai_indicatif_jours, obligatoire, bloquante, ordre, condition_activation, depend_de_tache_id)
        VALUES (v_model, 'Enregistrer les signatures requises', 'Recueillir les signatures du salarié et de l''entreprise.', 'RH', 1, TRUE, FALSE, 3, 'TOUJOURS', v_t2)
        RETURNING id_modele_tache_onboarding INTO v_t3;
        INSERT INTO modele_tache_onboarding (id_modele_onboarding, titre, description, role_responsable,
            delai_indicatif_jours, obligatoire, bloquante, ordre, condition_activation, depend_de_tache_id)
        VALUES (v_model, 'Archiver le contrat signé', 'Déposer le contrat signé dans le dossier documentaire.', 'RH', 1, TRUE, FALSE, 4, 'TOUJOURS', v_t3)
        RETURNING id_modele_tache_onboarding INTO v_t4;
        INSERT INTO modele_tache_onboarding (id_modele_onboarding, titre, description, role_responsable,
            delai_indicatif_jours, obligatoire, bloquante, ordre, condition_activation)
        VALUES (v_model, 'Vérifier les pièces administratives', 'Vérifier CIN, CNaPS, OSTIE et coordonnées bancaires.', 'RH', 2, TRUE, FALSE, 5, 'TOUJOURS')
        RETURNING id_modele_tache_onboarding INTO v_t5;
        INSERT INTO modele_tache_onboarding (id_modele_onboarding, titre, description, role_responsable,
            delai_indicatif_jours, obligatoire, bloquante, ordre, condition_activation, depend_de_tache_id)
        VALUES (v_model, 'Préparer les informations de paie', 'Transmettre les éléments nécessaires à la paie.', 'PAIE', 3, TRUE, FALSE, 6, 'TOUJOURS', v_t5)
        RETURNING id_modele_tache_onboarding INTO v_t6;
        INSERT INTO modele_tache_onboarding (id_modele_onboarding, titre, description, role_responsable,
            delai_indicatif_jours, obligatoire, bloquante, ordre, condition_activation)
        VALUES (v_model, 'Préparer les démarches sociales', 'Préparer les déclarations CNaPS et OSTIE.', 'RH', 3, TRUE, FALSE, 7, 'TOUJOURS')
        RETURNING id_modele_tache_onboarding INTO v_t7;
        INSERT INTO modele_tache_onboarding (id_modele_onboarding, titre, description, role_responsable,
            delai_indicatif_jours, obligatoire, bloquante, ordre, condition_activation)
        VALUES (v_model, 'Organiser le suivi de santé au travail', 'Planifier la visite médicale d''embauche.', 'SANTE', 5, TRUE, FALSE, 8, 'TOUJOURS')
        RETURNING id_modele_tache_onboarding INTO v_t8;
        INSERT INTO modele_tache_onboarding (id_modele_onboarding, titre, description, role_responsable,
            delai_indicatif_jours, obligatoire, bloquante, ordre, condition_activation)
        VALUES (v_model, 'Préparer le poste de travail', 'Préparer le matériel et l''espace de travail.', 'SERVICES_GENERAUX', 3, TRUE, FALSE, 9, 'TOUJOURS')
        RETURNING id_modele_tache_onboarding INTO v_t9;
        INSERT INTO modele_tache_onboarding (id_modele_onboarding, titre, description, role_responsable,
            delai_indicatif_jours, obligatoire, bloquante, ordre, condition_activation)
        VALUES (v_model, 'Créer les comptes professionnels', 'Créer la messagerie et les accès applicatifs.', 'INFORMATIQUE', 3, TRUE, FALSE, 10, 'TOUJOURS')
        RETURNING id_modele_tache_onboarding INTO v_t10;
        INSERT INTO modele_tache_onboarding (id_modele_onboarding, titre, description, role_responsable,
            delai_indicatif_jours, obligatoire, bloquante, ordre, condition_activation)
        VALUES (v_model, 'Présenter le salarié à l''équipe', 'Organiser la présentation au manager et à l''équipe.', 'RESPONSABLE', 1, TRUE, FALSE, 11, 'TOUJOURS')
        RETURNING id_modele_tache_onboarding INTO v_t11;
        INSERT INTO modele_tache_onboarding (id_modele_onboarding, titre, description, role_responsable,
            delai_indicatif_jours, obligatoire, bloquante, ordre, condition_activation)
        VALUES (v_model, 'Présenter les règles internes', 'Présenter le règlement intérieur et les procédures.', 'RH', 1, TRUE, FALSE, 12, 'TOUJOURS')
        RETURNING id_modele_tache_onboarding INTO v_t12;
        INSERT INTO modele_tache_onboarding (id_modele_onboarding, titre, description, role_responsable,
            delai_indicatif_jours, obligatoire, bloquante, ordre, condition_activation, depend_de_tache_id)
        VALUES (v_model, 'Organiser les formations initiales', 'Planifier les formations métier et sécurité.', 'RESPONSABLE', 5, TRUE, FALSE, 13, 'TOUJOURS', v_t11)
        RETURNING id_modele_tache_onboarding INTO v_t13;
        INSERT INTO modele_tache_onboarding (id_modele_onboarding, titre, description, role_responsable,
            delai_indicatif_jours, obligatoire, bloquante, ordre, condition_activation, depend_de_tache_id)
        VALUES (v_model, 'Effectuer un point d''intégration', 'Faire un entretien de suivi avec le salarié.', 'RESPONSABLE', 14, TRUE, FALSE, 14, 'TOUJOURS', v_t13)
        RETURNING id_modele_tache_onboarding INTO v_t14;
        INSERT INTO modele_tache_onboarding (id_modele_onboarding, titre, description, role_responsable,
            delai_indicatif_jours, obligatoire, bloquante, ordre, condition_activation, depend_de_tache_id)
        VALUES (v_model, 'Recueillir les retours du salarié', 'Recueillir les difficultés et suggestions.', 'RH', 21, TRUE, FALSE, 15, 'TOUJOURS', v_t14)
        RETURNING id_modele_tache_onboarding INTO v_t15;
        INSERT INTO modele_tache_onboarding (id_modele_onboarding, titre, description, role_responsable,
            delai_indicatif_jours, obligatoire, bloquante, ordre, condition_activation, depend_de_tache_id)
        VALUES (v_model, 'Vérifier la complétude et clôturer', 'Contrôler les obligations et clôturer le parcours.', 'RH', 22, TRUE, FALSE, 16, 'TOUJOURS', v_t15)
        RETURNING id_modele_tache_onboarding INTO v_t16;
    END IF;

    -- Contrat de démonstration pour Henika Rasoa (employé 4).
    SELECT id_contrat INTO v_contrat FROM contrat WHERE reference = 'CTR-2026-9001' LIMIT 1;
    IF v_contrat IS NULL THEN
        INSERT INTO contrat (id_employe, reference, id_type_contrat, type_operation,
            date_signature, date_debut, fonction, categorie_professionnelle, salaire,
            statut_workflow, statut_contractuel, commentaire, cree_par_id)
        SELECT e.id_employe, 'CTR-2026-9001', tc.id_type_contrat, 'INITIAL',
               CURRENT_DATE - 120, CURRENT_DATE - 120, 'Développeur', 'Master', 2500000.00,
               'EN_VIGUEUR', 'ACTIF', 'Contrat de démonstration du Bloc 4.', v_rh
        FROM employe e, type_contrat tc
        WHERE e.matricule = 'EMP004' AND tc.code = 'CDI'
        RETURNING id_contrat INTO v_contrat;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM affectation WHERE id_employe = 4 AND statut = 'ACTIVE') THEN
        INSERT INTO affectation (id_employe, id_poste, id_departement, id_contrat, id_responsable,
            site, date_debut, motif, statut, cree_par_id)
        SELECT e.id_employe, e.id_poste, e.id_departement, v_contrat, NULL,
               'Siège', CURRENT_DATE - 120, 'Affectation de démonstration', 'ACTIVE', v_rh
        FROM employe e WHERE e.matricule = 'EMP004';
    END IF;

    -- Onboarding de démonstration avec une tâche terminée, une en retard et une bloquée.
    SELECT id_onboarding INTO v_onboarding FROM onboarding WHERE reference = 'ONB-2026-9001' LIMIT 1;
    IF v_onboarding IS NULL THEN
        INSERT INTO onboarding (reference, id_employe, id_contrat, id_modele_onboarding, version_modele,
            date_debut_prevue, date_fin_prevue, statut, id_responsable_rh, cree_par_id)
        SELECT 'ONB-2026-9001', e.id_employe, v_contrat, v_model, 1,
               CURRENT_DATE - 10, CURRENT_DATE + 20, 'EN_COURS', v_rh, v_rh
        FROM employe e WHERE e.matricule = 'EMP004'
        RETURNING id_onboarding INTO v_onboarding;

        INSERT INTO onboarding_tache (id_onboarding, titre, description, role_responsable,
            id_responsable, date_echeance, statut, date_debut, date_realisation, realise_par_id,
            obligatoire, bloquante, ordre, commentaire)
        VALUES (v_onboarding, 'Vérifier les informations administratives', 'Pièces administratives vérifiées.', 'RH',
            v_rh, CURRENT_DATE - 5, 'TERMINEE', NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days', v_rh,
            TRUE, TRUE, 1, 'Dossier administratif complet.');

        INSERT INTO onboarding_tache (id_onboarding, titre, description, role_responsable,
            id_responsable, date_echeance, statut, obligatoire, bloquante, ordre)
        VALUES (v_onboarding, 'Créer les comptes professionnels', 'Créer la messagerie et les accès.', 'INFORMATIQUE',
            v_rh, CURRENT_DATE - 2, 'A_FAIRE', TRUE, FALSE, 2);

        INSERT INTO onboarding_tache (id_onboarding, titre, description, role_responsable,
            id_responsable, date_echeance, statut, motif_blocage, obligatoire, bloquante, ordre)
        VALUES (v_onboarding, 'Préparer le poste de travail', 'Matériel non disponible.', 'SERVICES_GENERAUX',
            v_rh, CURRENT_DATE + 2, 'BLOQUEE', 'Matériel en attente de livraison.', TRUE, FALSE, 3);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM declaration_sociale WHERE id_employe = 4 AND type_declaration = 'Immatriculation CNaPS') THEN
        INSERT INTO declaration_sociale (id_employe, id_organisme, type_declaration, statut, commentaire, cree_par_id)
        SELECT 4, o.id_organisme, 'Immatriculation CNaPS', 'A_PREPARER', 'Déclaration de démonstration.', v_rh
        FROM organisme o WHERE o.code = 'CNAPS';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM document WHERE titre = 'Contrat de démonstration') THEN
        INSERT INTO document (type_document, titre, nom_fichier, chemin_stockage, type_mime, taille_octets,
            version, statut_validation, niveau_confidentialite, id_employe, id_contrat, depose_par_id, commentaire)
        VALUES ('CONTRAT', 'Contrat de démonstration', 'contrat-demo.pdf',
            'seed/contrat-demo.pdf', 'application/pdf', 0, 1, 'VERIFIE', 'CONFIDENTIEL',
            4, v_contrat, v_rh, 'Métadonnées de démonstration.');
    END IF;
END $$;
