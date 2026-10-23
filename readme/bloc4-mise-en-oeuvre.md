# BLOC 4 — MISE EN ŒUVRE TECHNIQUE

Ce document décrit l'implémentation réelle du Bloc 4 dans l'application.
Le référentiel fonctionnel reste `readme/bloc4.md`.

## 1. Périmètre livré

- contrats : création, modification, soumission, validation, signatures, activation,
  terminaison, annulation, avenants et renouvellements ;
- salariés : fiche consolidée, contrats, affectations, onboardings, documents et
  formalités ;
- affectations : création, clôture et mise à jour du poste/département du salarié ;
- onboarding : modèles versionnés, génération automatique, tâches, dépendances,
  blocage/déblocage, progression, clôture et réouverture ;
- documents : upload borné, types et confidentialité contrôlés, vérification,
  rejet et téléchargement protégé ;
- formalités : préparation, transmission, confirmation et échec ;
- tableau de bord RH, notifications et journal d'audit.

## 2. Organisation du code

| Couche | Emplacement |
|---|---|
| Entités Bloc 4 | `src/main/java/com/gestionrh/sirh/model/` |
| Dépôts | `src/main/java/com/gestionrh/sirh/repository/` |
| Services métier | `src/main/java/com/gestionrh/sirh/service/` |
| DTO de lecture/écriture | `src/main/java/com/gestionrh/sirh/service/dto/` |
| Contrôleurs | `src/main/java/com/gestionrh/sirh/controller/` |
| Vues | `src/main/webapp/WEB-INF/jsp/rh/` et `mes-taches.jsp` |
| Styles et scripts | `src/main/resources/static/` |
| Schéma et données de test | `migrations/bloc4.sql`, `migrations/bloc4_teste.sql` |

Le Bloc 4 n'importe aucune entité des Blocs 1, 2 ou 3. La seule entrée
inter-blocs est `EmbaucheDTO`, appelée par l'adaptateur côté Bloc 3
(voir `readme/bloc4-integration.md`).

## 3. Données et contraintes

Les tables utilisent des clés `id_*`, des timestamps `date_creation` /
`date_maj` et des contraintes `CHECK` sur les états métier.

Points structurants :

- `contrat.id_recrutement_externe` est unique : un recrutement Bloc 3 ne peut
  produire qu'un seul contrat initial ;
- un salarié peut avoir plusieurs contrats et plusieurs affectations ;
- une seule affectation active par salarié ;
- une tâche d'onboarding ne peut être terminée que si sa dépendance est terminée ;
- une tâche bloquée exige un motif ;
- les documents `SANTE` et `SECRET` sont réservés aux rôles RH et DG.

Les énumérations métier sont stockées en texte et validées côté service avant
toute écriture, afin d'éviter les erreurs de contrainte SQL peu lisibles.

## 4. Règles de gestion appliquées

### Contrats

- CDI : la date de fin est forcée à `null` ;
- CDD/CE/STAGE : date de fin obligatoire et postérieure à la date de début ;
- un salarié ne peut avoir qu'un contrat actif à la fois ;
- un avenant crée un nouveau contrat lié au contrat initial sans modifier
  l'historique ;
- un renouvellement crée un nouveau contrat et termine l'ancien à l'activation ;
- l'activation est refusée avant la date d'effet ;
- les signatures requises doivent être présentes avant l'activation.

### Onboarding

- les tâches sont générées depuis un modèle applicable au poste/département ;
- un modèle est versionné ; l'activation d'une version désactive les autres
  versions du même nom ;
- la duplication d'un modèle conserve les dépendances entre tâches ;
- les conditions d'activation non remplies ne génèrent pas la tâche ;
- le statut passe à `BLOQUE` si une tâche bloquante est bloquée ;
- le déblocage restaure `A_FAIRE` ou `EN_COURS` selon que la tâche a démarré ;
- la clôture exige que les tâches obligatoires soient terminées ;
- la réouverture est tracée dans l'audit.

### Documents

- taille maximale : 10 Mo ;
- extensions acceptées : `pdf`, `docx`, `doc`, `png`, `jpg`, `jpeg` ;
- nom de stockage généré côté serveur, jamais le nom fourni par l'utilisateur ;
- vérification du chemin avant lecture pour bloquer toute traversée de répertoire ;
- un document `SANTE`/`SECRET` n'est téléchargeable que par RH ou DG ;
- un salarié ne peut télécharger que ses propres documents, sauf responsable
  de son département.

## 5. Routes principales

| Méthode | URL | Usage |
|---|---|---|
| GET | `/rh/tableau-de-bord` | tableau de bord RH |
| GET/POST | `/rh/contrats` | liste et filtres |
| GET/POST | `/rh/contrats/nouveau` | création |
| GET/POST | `/rh/contrats/{id}/modifier` | modification |
| POST | `/rh/contrats/{id}/soumettre` | soumission |
| POST | `/rh/contrats/{id}/valider` | validation |
| POST | `/rh/contrats/{id}/signer` | signature |
| POST | `/rh/contrats/{id}/activer` | activation |
| POST | `/rh/contrats/{id}/terminer` | terminaison |
| POST | `/rh/contrats/{id}/annuler` | annulation |
| GET/POST | `/rh/contrats/{id}/avenant` | avenant |
| GET/POST | `/rh/contrats/{id}/renouveler` | renouvellement |
| GET/POST | `/rh/affectations` | affectations |
| GET/POST | `/rh/onboardings` | onboardings |
| GET/POST | `/rh/onboardings/modeles` | modèles |
| GET/POST | `/rh/documents` | documents |
| GET/POST | `/rh/formalites` | formalités |
| GET/POST | `/mes-taches` | tâches du salarié connecté |

Tous les `POST` sont protégés par CSRF. Les pages RH sont réservées aux rôles
`RH` et `DG` ; `/mes-taches` est accessible à tout utilisateur authentifié.

## 6. Interface

- layout commun avec barre latérale, badge de tâches et navigation Bloc 4 ;
- composant de sélection personnalisé (`select.js`) pour tous les `<select>` ;
- éditeur dynamique des modèles d'onboarding (`modele-onboarding.js`) ;
- états de chargement, vides, erreur et succès explicites ;
- responsive vérifié à 375, 768 et 1024 px ;
- libellés, focus visible et contrastes conformes aux règles d'accessibilité.

## 7. Vérifications

```bash
./mvnw -q test
./mvnw -q -DskipTests compile
./mvnw clean package
```

Tests ajoutés :

- `Bloc4ContratServiceTest` : règles CDI/CDD ;
- `Bloc4DocumentServiceTest` : extension, type et stockage ;
- `Bloc4ModeleOnboardingServiceTest` : dépendances lors de la duplication ;
- `RecrutementAdapterTest` : construction du DTO, idempotence et échec Bloc 4.

Parcours vérifiés manuellement : contrat complet jusqu'à l'activation, avenant,
renouvellement, affectation, onboarding avec blocage/déblocage, document
uploadé/vérifié/téléchargé, formalité jusqu'à confirmation, rôles EMPLOYE/RH
et responsive.
