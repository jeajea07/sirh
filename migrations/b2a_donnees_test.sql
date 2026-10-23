-- Jeu d'essai BLOC 2A rejouable.
-- Prérequis : migrations du bloc 1, puis b2a_recrutement.sql.
-- Les lignes sont retrouvées par leurs références de test avant insertion.
-- Ne pas utiliser sur une base contenant des données de production.

DO $$
DECLARE
    v_unite INT;
    v_departement INT;
    v_poste INT;
    v_type INT;
    v_employe INT;
    v_critere INT;
    v_demande INT;
    v_offre_brouillon INT;
    v_offre_validee INT;
    v_offre_publiee INT;
    v_offre_suspendue INT;
    v_offre_terminee INT;
    v_candidat_conforme INT;
    v_candidat_non_conforme INT;
    v_candidat_a_regarder INT;
    v_candidat_spontane INT;
    v_candidature INT;
    v_index INT;
BEGIN
    SELECT id_unite_organisationnelle INTO v_unite
      FROM unite_organisationnelle WHERE designation = 'DEPARTEMENT' LIMIT 1;
    IF v_unite IS NULL THEN
        INSERT INTO unite_organisationnelle (designation) VALUES ('DEPARTEMENT') RETURNING id_unite_organisationnelle INTO v_unite;
    END IF;

    SELECT id_departement INTO v_departement FROM departement WHERE designation = 'B2A - Informatique' LIMIT 1;
    IF v_departement IS NULL THEN
        INSERT INTO departement (designation, id_unite_organisationnelle, effectif)
        VALUES ('B2A - Informatique', v_unite, 5) RETURNING id_departement INTO v_departement;
    END IF;

    SELECT id_poste INTO v_poste FROM poste WHERE designation = 'B2A - Développeur Java' LIMIT 1;
    IF v_poste IS NULL THEN
        INSERT INTO poste (designation) VALUES ('B2A - Développeur Java') RETURNING id_poste INTO v_poste;
    END IF;

    SELECT id_type_contrat INTO v_type FROM type_contrat WHERE code = 'B2A-CDI' LIMIT 1;
    IF v_type IS NULL THEN
        INSERT INTO type_contrat (code, libelle) VALUES ('B2A-CDI', 'CDI - données de test B2A') RETURNING id_type_contrat INTO v_type;
    END IF;

    SELECT id_employe INTO v_employe FROM employe WHERE matricule = 'B2A-RH-TEST' LIMIT 1;
    IF v_employe IS NULL THEN
        INSERT INTO employe (matricule, nom, prenom, email, id_departement, id_poste, date_embauche, actif)
        VALUES ('B2A-RH-TEST', 'Test', 'Responsable', 'rh-b2a-test@example.invalid', v_departement, v_poste, CURRENT_DATE, TRUE)
        RETURNING id_employe INTO v_employe;
    END IF;

    FOR v_index IN 1..5 LOOP
        SELECT id_critere_recrutement INTO v_critere
          FROM critere_recrutement WHERE missions = 'Données de test B2A ' || v_index LIMIT 1;
        IF v_critere IS NULL THEN
            INSERT INTO critere_recrutement (annee_experience, niveau_etude, missions, competence)
            VALUES (3, 'LICENCE', 'Données de test B2A ' || v_index, 'Java, SQL')
            RETURNING id_critere_recrutement INTO v_critere;
        END IF;
        SELECT id_demande_besoin INTO v_demande
          FROM demande_besoin WHERE numero = 'B2A-TEST-DEM-' || v_index LIMIT 1;
        IF v_demande IS NULL THEN
            INSERT INTO demande_besoin (numero, id_demandeur, id_departement, id_poste, id_critere_recrutement,
                nombre_postes, motif, niveau_urgence, id_type_contrat, salaire_propose, date_souhaitee,
                justification, statut)
            VALUES ('B2A-TEST-DEM-' || v_index, v_employe, v_departement, v_poste, v_critere,
                1, 'RENFORT', 'URGENT', v_type, 2500000, CURRENT_DATE + 30,
                'Demande acceptée pour le scénario de test du bloc 2A.', 'VALIDEE')
            RETURNING id_demande_besoin INTO v_demande;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM offre WHERE reference = 'OFF-2099-' || lpad((9000 + v_index)::text, 4, '0')) THEN
            INSERT INTO offre (reference, id_demande_besoin, id_poste, id_departement, id_type_contrat,
                titre, description, missions, salaire_propose, nombre_postes, statut, id_validateur,
                date_validation, date_publication, date_limite)
            VALUES ('OFF-2099-' || lpad((9000 + v_index)::text, 4, '0'), v_demande, v_poste, v_departement, v_type,
                'Développeur Java — test ' || v_index,
                'Offre de démonstration du bloc 2A.', 'Développer et maintenir des applications.',
                2500000, 1,
                CASE v_index WHEN 1 THEN 'BROUILLON' WHEN 2 THEN 'VALIDEE' WHEN 3 THEN 'PUBLIEE'
                    WHEN 4 THEN 'SUSPENDUE' ELSE 'TERMINEE' END,
                v_employe, CURRENT_TIMESTAMP, CASE WHEN v_index >= 3 THEN CURRENT_TIMESTAMP - INTERVAL '2 days' END,
                CURRENT_DATE + 30);
        END IF;
    END LOOP;

    SELECT id_offre INTO v_offre_brouillon FROM offre WHERE reference = 'OFF-2099-9001';
    SELECT id_offre INTO v_offre_validee FROM offre WHERE reference = 'OFF-2099-9002';
    SELECT id_offre INTO v_offre_publiee FROM offre WHERE reference = 'OFF-2099-9003';
    SELECT id_offre INTO v_offre_suspendue FROM offre WHERE reference = 'OFF-2099-9004';
    SELECT id_offre INTO v_offre_terminee FROM offre WHERE reference = 'OFF-2099-9005';

    INSERT INTO critere_offre (id_offre, type_critere, nature, libelle, valeur_min)
    SELECT o.id_offre, c.type_critere, c.nature, c.libelle, c.valeur_min
      FROM offre o CROSS JOIN (VALUES
        ('OBLIGATOIRE','NIVEAU_ETUDE','LICENCE',NULL::INT),
        ('OBLIGATOIRE','EXPERIENCE',NULL,3),
        ('OBLIGATOIRE','COMPETENCE','Java',NULL::INT),
        ('OBLIGATOIRE','LANGUE','Français',NULL::INT),
        ('SOUHAITE','COMPETENCE','PostgreSQL',NULL::INT)
      ) AS c(type_critere,nature,libelle,valeur_min)
     WHERE o.id_offre IN (v_offre_brouillon, v_offre_validee, v_offre_publiee, v_offre_suspendue, v_offre_terminee)
       AND NOT EXISTS (SELECT 1 FROM critere_offre x WHERE x.id_offre=o.id_offre AND x.type_critere=c.type_critere
           AND x.nature=c.nature AND x.libelle IS NOT DISTINCT FROM c.libelle);

    INSERT INTO candidat (nom, prenom, email, telephone, adresse)
    VALUES ('Conforme', 'Camille', 'candidat-conforme-b2a@example.invalid', '000000001', 'Adresse de test')
    ON CONFLICT (email) DO NOTHING;
    INSERT INTO candidat (nom, prenom, email, telephone, adresse)
    VALUES ('Refus', 'Noa', 'candidat-non-conforme-b2a@example.invalid', '000000002', 'Adresse de test')
    ON CONFLICT (email) DO NOTHING;
    INSERT INTO candidat (nom, prenom, email, telephone, adresse)
    VALUES ('Examen', 'Alex', 'candidat-a-regarder-b2a@example.invalid', '000000003', 'Adresse de test')
    ON CONFLICT (email) DO NOTHING;
    INSERT INTO candidat (nom, prenom, email, telephone, adresse)
    VALUES ('Réserve', 'Sam', 'candidat-spontane-b2a@example.invalid', '000000004', 'Adresse de test')
    ON CONFLICT (email) DO NOTHING;

    SELECT id_candidat INTO v_candidat_conforme FROM candidat WHERE email='candidat-conforme-b2a@example.invalid';
    SELECT id_candidat INTO v_candidat_non_conforme FROM candidat WHERE email='candidat-non-conforme-b2a@example.invalid';
    SELECT id_candidat INTO v_candidat_a_regarder FROM candidat WHERE email='candidat-a-regarder-b2a@example.invalid';
    SELECT id_candidat INTO v_candidat_spontane FROM candidat WHERE email='candidat-spontane-b2a@example.invalid';

    INSERT INTO candidature (numero,id_candidat,id_offre,spontanee,date_reception,niveau_etude,annees_experience,
        formations,experiences,langues,disponibilite,cv_nom_fichier,cv_chemin,cv_type,cv_taille_octets,statut,classement_auto,motif_classement)
    VALUES
      ('CAND-2099-90001',v_candidat_conforme,v_offre_publiee,FALSE,CURRENT_TIMESTAMP - INTERVAL '1 day','MASTER',5,
       'Master informatique','Développement Java depuis 5 ans','Français, Anglais',CURRENT_DATE,'cv-test-conforme.pdf','/test/cv-1','PDF',128,'A_ETUDIER','CONFORME','Données de test conformes'),
      ('CAND-2099-90002',v_candidat_non_conforme,v_offre_publiee,FALSE,CURRENT_TIMESTAMP - INTERVAL '8 days','BAC',0,
       'Baccalauréat','Aucune expérience','Français',CURRENT_DATE,'cv-test-non-conforme.docx','/test/cv-2','DOCX',256,'A_ETUDIER','NON_CONFORME','Données de test non conformes'),
      ('CAND-2099-90003',v_candidat_a_regarder,v_offre_publiee,FALSE,CURRENT_TIMESTAMP - INTERVAL '6 days','BAC',2,
       'Formation informatique','Expérience Java de 2 ans','',CURRENT_DATE,'cv-test-a-regarder.pdf','/test/cv-3','PDF',192,'A_ETUDIER','A_REGARDER','Données de test à examiner'),
      ('CAND-2099-90004',v_candidat_spontane,NULL,TRUE,CURRENT_TIMESTAMP - INTERVAL '2 days','LICENCE',2,
       'Licence informatique','Développement Java','Français',CURRENT_DATE,'cv-test-spontane.pdf','/test/cv-4','PDF',160,'RECUE',NULL,NULL)
    ON CONFLICT (numero) DO NOTHING;

    INSERT INTO cv_competence (id_candidature,libelle)
    SELECT c.id_candidature,x.libelle FROM candidature c
      JOIN (VALUES ('CAND-2099-90001','Java'),('CAND-2099-90001','PostgreSQL'),
                   ('CAND-2099-90002','HTML'),('CAND-2099-90003','Java')) x(numero,libelle) USING (numero)
     WHERE NOT EXISTS (SELECT 1 FROM cv_competence v WHERE v.id_candidature=c.id_candidature AND lower(v.libelle)=lower(x.libelle));

    INSERT INTO historique_candidature (id_candidature,action,nouveau_statut,nouveau_classement,commentaire)
    SELECT c.id_candidature,'JEU_TEST',c.statut,c.classement_auto,'Ligne de démonstration, rejouable'
      FROM candidature c WHERE c.numero LIKE 'CAND-2099-9%'
        AND NOT EXISTS (SELECT 1 FROM historique_candidature h WHERE h.id_candidature=c.id_candidature AND h.action='JEU_TEST');
END $$;
