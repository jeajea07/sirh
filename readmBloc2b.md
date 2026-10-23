# Bloc 2B — Synthèse de développement et d’intégration

## 1. Objectif du bloc 2B

Le bloc 2B correspond à la phase de recrutement où le candidat dont le dossier est validé passe un test technique, puis reçoit une décision de l’équipe RH.

Il s’agit de la transition entre :
- le dossier de candidature validé,
- l’accès candidat généré,
- le test technique soumis,
- la validation finale de la candidature.

Le bloc 2B est donc la partie qui gère :
- la création de l’accès candidat,
- la gestion des questions du test,
- le calcul de la note,
- le statut de la candidature,
- la décision RH.

---

## 2. Ce qui a été modifié

### a) Nettoyage du code métier

Les valeurs codées en dur ont été remplacées par des éléments centralisés et explicites :
- [src/main/java/com/gestionrh/sirh/service/Bloc2BQuestion.java](src/main/java/com/gestionrh/sirh/service/Bloc2BQuestion.java)
- [src/main/java/com/gestionrh/sirh/service/Bloc2BStatus.java](src/main/java/com/gestionrh/sirh/service/Bloc2BStatus.java)
- [src/main/java/com/gestionrh/sirh/service/Bloc2BService.java](src/main/java/com/gestionrh/sirh/service/Bloc2BService.java)

Pourquoi ?
- éviter les chaînes magiques du type "Q1", "Q2", "Q3", "ADMIS", "REFUSE" répétées partout,
- rendre le code plus lisible,
- faciliter la maintenance,
- permettre une intégration facile dans le bloc 2 global.

### b) Harmonisation des statuts

Le bloc 2B utilisait plusieurs libellés proches mais incohérents, ce qui créait de la confusion entre l’état du test et l’état de la candidature.

Les statuts sont maintenant regroupés selon deux axes :

1. Candidature
   - RECU
   - DOSSIER_VALIDE
   - TEST_EN_COURS
   - TEST_OK
   - ADMIS
   - REFUSE
   - ABSENT
   - EN_ATTENTE

2. Test technique
   - PROGRAMME
   - EN_COURS
   - RELANCE
   - CORRIGE
   - ADMIS
   - REFUSE
   - ABSENT

Pourquoi ?
- un statut représente un seul sens métier,
- le code est uniforme entre service, contrôleur et base de données,
- les pages JSP et les décisions RH restent cohérentes,
- il devient plus simple d’intégrer le bloc 2B au bloc 2 sans ambiguïté.

### c) Schéma partagé et migration BLOC 2B

Appliquer `migrations/bloc1.sql`, puis `migrations/b2a_recrutement.sql`, puis `migrations/bloc2.sql`. Les tables `candidat`, `offre` et `candidature` sont partagées et définies par le BLOC 2A; le script BLOC 2B ajoute uniquement les colonnes d'accès et les tables `test_technique` et `reponse_test_technique`.

Les jeux d'essai s'exécutent dans l'ordre `migrations/b2a_donnees_test.sql`, puis `migrations/bloc2_teste.sql`.

### d) Contrôle RH et logique du test technique

Une logique de décision a été stabilisée dans :
- [src/main/java/com/gestionrh/sirh/controller/TestTechniqueController.java](src/main/java/com/gestionrh/sirh/controller/TestTechniqueController.java)
- [src/test/java/com/gestionrh/sirh/service/Bloc2BServiceTest.java](src/test/java/com/gestionrh/sirh/service/Bloc2BServiceTest.java)

Pourquoi ?
- le RH peut décider d’admettre ou de refuser la candidature selon le seuil défini,
- le test technique repose sur une règle unique et centralisée,
- les tests de validation restent faciles à lancer et à maintenir.

---

## 3. Fichiers touchés

Voici les fichiers concernés par cette étape de développement :

- [src/main/java/com/gestionrh/sirh/service/Bloc2BService.java](src/main/java/com/gestionrh/sirh/service/Bloc2BService.java) — logique principale du bloc 2B
- [src/main/java/com/gestionrh/sirh/service/Bloc2BQuestion.java](src/main/java/com/gestionrh/sirh/service/Bloc2BQuestion.java) — questions du test et validation des réponses
- [src/main/java/com/gestionrh/sirh/service/Bloc2BStatus.java](src/main/java/com/gestionrh/sirh/service/Bloc2BStatus.java) — statut standardisé
- [src/main/java/com/gestionrh/sirh/controller/TestTechniqueController.java](src/main/java/com/gestionrh/sirh/controller/TestTechniqueController.java) — parcours candidat et RH
- [src/test/java/com/gestionrh/sirh/service/Bloc2BServiceTest.java](src/test/java/com/gestionrh/sirh/service/Bloc2BServiceTest.java) — validation du comportement métier
- [migrations/bloc2.sql](migrations/bloc2.sql) — structure SQL du test technique uniquement; candidat, offre et candidature sont partagés avec le BLOC 2A

---

## 4. Explication métier du bloc 2B

### Étape 1 — validation du dossier
Quand une candidature est jugée conforme, on valide le dossier du candidat. Cela permet de passer à la phase test technique.

### Étape 2 — création de l’accès candidat
À ce moment, le système crée automatiquement :
- un identifiant,
- un mot de passe temporaire,
- la marque de création d’accès,
- le statut `DOSSIER_VALIDE`.

Cela permet au candidat de se connecter et d’accéder à son test technique.

### Étape 3 — soumission du test
Le candidat répond aux questions du test technique. Le système :
- enregistre les réponses,
- calcule la note,
- compare la note au seuil,
- met à jour le statut de la candidature en `TEST_OK`.

### Étape 4 — correction et décision RH
Le RH peut :
- relancer le test,
- corriger les réponses,
- admettre ou refuser la candidature selon le seuil.

Le service centralise cette logique pour éviter les erreurs de duplication.

---

## 5. Pourquoi le bloc 2B est facile à intégrer dans le bloc 2

Le bloc 2B s’intègre facilement parce qu’il suit la logique naturelle du recrutement :

1. Une offre existe.
2. Un candidat postule.
3. Le dossier est étudié.
4. Le dossier valide le passage au test.
5. Le test donne une note.
6. La RH décide de l’admission.

Autrement dit, le bloc 2B ne travaille pas en isolation. Il repose sur les objets déjà présents dans le bloc 2 :
- `candidat`
- `offre`
- `candidature`
- `test_technique`
- `reponse_test_technique`

La clé de l’intégration est la cohérence des données :
- une candidature est liée à un candidat et à une offre,
- un test est lié à une candidature,
- une réponse est liée à un test,
- les statuts sont standardisés,
- la logique métier est centralisée dans le service.

En pratique, pour intégrer le bloc 2B dans le bloc 2, il suffit de :
- conserver les tables déjà créées,
- exécuter le script final de migration,
- appeler `Bloc2BService` au bon moment,
- utiliser les statuts normalisés,
- ne pas dupliquer de logique métier dans les contrôleurs.

---

## 6. Résumé rapide

Le bloc 2B est désormais :
- propre,
- cohérent,
- testable,
- préréparé pour la soutenance,
- facile à intégrer au bloc 2 sans réécriture importante.

Le cœur de la logique est maintenant dans le service dédié, avec des statuts harmonisés et un script SQL final propre.
