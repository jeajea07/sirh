# Avancement du bloc 2A

## Audit initial (T0.1)

- Présents : les 9 enums, 7 entités, 7 repositories, CRUD des offres, DTO, exceptions et vues JSP provisoires décrits dans le contexte.
- Compilation : `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:$PATH mvn -q -DskipTests compile` passe. `./mvnw` ne démarre pas, car Maven Wrapper tente d'écrire sa distribution dans `/home/aropih/.m2`, hors du répertoire accessible en écriture. Le premier essai avec Maven système utilisait Java 8 et n'est pas représentatif de la compilation requise en Java 21.
- Configuration après T0.2 : JSP/JSTL, mail, validation, limites multipart, stockage des CV, expéditeur/RH/jours fériés et scheduling sont configurés. Le préfixe principal est `/WEB-INF/views/`; un résolveur maintient les JSP existantes du bloc 1 sous `/WEB-INF/jsp/`. `ddl-auto=none` est conservé.
- Écarts découverts après audit : `notification_recrutement` manquait du script B2A ; sa création est ajoutée. Le jeu SQL de test est écrit, mais ne peut être exécuté car PostgreSQL local ne répond pas.
- À créer : candidature, stockage CV, classement, suivi RH, rappels métier, événements du bloc 2B et documentation finale.
- Existant hors BLOC 2A : `LayoutAdvice` (gestion partielle du layout), sécurité Spring et les entités du bloc 1.
- État Git au début du travail : nombreuses modifications déjà staged, plusieurs fichiers en conflit (`migrations/bloc1.sql`, `application.properties`) et changements non staged dans le code BLOC 2A. Aucun changement préexistant n'est inclus dans cet audit ni écrasé.

## Tâches

### Phase 0 — Audit et mise en place
- [x] T0.1 Auditer le dépôt, relever les écarts et vérifier la compilation. Audit consigné ci-dessus ; compilation Java 21 réussie.
- [x] T0.2 Compléter la configuration JSP/JSTL, mail, validation, stockage CV, uploads et scheduling. Ajout des starters mail/validation, options d'envoi par environnement, limites d'upload, répertoire CV, destinataires/jours fériés et activation du scheduling ; les anciennes vues du bloc 1 restent résolubles.
- [ ] T0.3 Ajouter un jeu de données de test rejouable. Le script est créé ; validation PostgreSQL à faire lorsque le serveur sera disponible (`localhost:5432` ne répond pas).

### Phase 1 — Notifications
- [x] T1.1 Service idempotent de notification, envoi/simulation et relance. Création persistée avant envoi, simulation configurable, erreurs d'envoi isolées, relance planifiée plafonnée à cinq tentatives.
- [x] T1.2 Notifications internes RH, résolution des destinataires et compteur. Destinataires via interface/config provisoire ; page RH, lecture et compteur du gabarit recrutement.

### Phase 2 — Offres
- [x] T2.1 Création automatique depuis une demande acceptée. Choix parmi les demandes dont les dernières décisions de chaque étape obligatoire sont VALIDEE ; création idempotente, préremplissage, critères disponibles et notification après commit.
- [x] T2.2 Règle unique d'offre ouverte et maintien des compteurs. Règle utilisée par la vitrine, le dépôt et le rattachement ; le badge de clôture est dérivé et le compteur est recalculé après réception/rattachement.
- [x] T2.3 Notifications lors de l'annulation. Les dossiers actifs passent en NON_RETENUE avec historique ; notification candidat et RH après commit.
- [x] T2.4 Site vitrine public des offres. Liste paginée/recherche et détail en DTO de liste blanche, routes publiques sans compte.

### Phase 3 — Candidature
- [x] T3.1 Stockage sécurisé et validation des CV. Extension, MIME, signature et taille vérifiés ; nom UUID privé ; contrôle du chemin réel et suppression en cas d'échec.
- [x] T3.2 Dépôt de candidature et accusé de réception. Réutilisation candidat par e-mail insensible à la casse, verrou PostgreSQL, détection de doublon, numéro, historique, classement et compteur.
- [x] T3.3 Notifications à la réception. Accusé candidat et notification RH publiés après commit, avec idempotence.
- [x] T3.4 Candidatures spontanées et rattachement à une offre. Réserve filtrée/paginée ; rattachement vérifie l'offre ouverte, classe, historise, recalcule le compteur et notifie le RH.

### Phase 4 — Étude et sélection
- [x] T4.1 Classement automatique et détail des critères. Évaluateur pur avec tolérances configurables, critères souhaités séparés et motif lisible ; tests unitaires de frontières ajoutés.
- [x] T4.2 Suivi RH des candidatures et téléchargement contrôlé du CV. Filtres, tri sur liste blanche, pagination, indicateurs, historique et endpoint protégé de téléchargement.
- [x] T4.3 Décision RH, historique et événement `DossierValideEvent`. Décision verrouillée/idempotente, commentaire requis en cas de changement, réponse non retenue par e-mail et journal du point d'intégration BLOC 2B.
- [x] T4.4 Rappel quotidien des dossiers en retard. Calcul lundi-vendredi avec jours fériés configurés, message groupé unique par RH et par jour ; test unitaire du calendrier.

### Phase 5 — Transverse
- [x] T5.1 Gestion globale des erreurs. Advice BLOC 2A pour règles métier, 404, validation, upload trop volumineux, conflit de données et erreurs inattendues.
- [x] T5.2 Recherche, tri et pagination uniformes. Filtres conservés dans les liens ; tri utilisateur de candidatures contrôlé par liste blanche.
- [x] T5.3 Sécurité et robustesse. Routes RH protégées, dépôt public limité aux routes prévues, CSRF sur les formulaires POST, affichage des données dynamiques échappé avec JSTL, CV privés avec contrôle de chemin et erreurs sans détails internes.
- [ ] T5.4 Tests automatisés sur PostgreSQL et contrôleurs. Tests unitaires métier ciblés présents et réussis ; tests d'intégration PostgreSQL/contrôleurs restent à écrire et exécuter avec une base disponible.
- [ ] T5.5 Intégration du gabarit commun lorsqu'il est disponible. Le dépôt ne contient pas encore de gabarit fourni par Faneva ; les vues utilisent le gabarit provisoire BLOC 2A.

### Phase 6 — Finalisation
- [ ] T6.1 Scénario d'intégration de bout en bout. Il dépend de PostgreSQL réel et de l'intégration active du bloc 1 et du listener BLOC 2B.
- [ ] T6.2 Nettoyage du code. Recherche des `System.out`, `printStackTrace`, `TODO` et `FIXME` dans le périmètre BLOC 2A effectuée ; revue complète des imports et formatage à finaliser.
- [x] T6.3 Documentation BLOC 2A, flux utilisateur et maquettes. Guide détaillé, diagrammes Mermaid des parcours et des états/données, exemples de classement et maquettes statiques ajoutés.
- [x] T6.4 Mise à jour de la documentation technique globale. Section BLOC 2A ajoutée à `readme/bloc2.md` avec renvoi vers le guide technique.
- [ ] T6.5 Correction des anomalies et compte rendu final. À reprendre après tests d'intégration et déroulé manuel sur une base PostgreSQL.

## Vérifications récentes

- Compilation réussie avec Java 21 via Maven système : `mvn -q -DskipTests compile`.
- Tests unitaires ciblés réussis : classement, jours ouvrables et stockage/validation CV.
- Tests PostgreSQL, MockMvc et exécution du jeu SQL non lancés : PostgreSQL local n'écoute pas sur `localhost:5432`.
- Dernière compilation Java 21 et suite unitaire ciblée (classement, jours ouvrables, stockage CV) réussies après les changements de sécurité.
- Aucun commit n'a pu être créé, car `.git/index` est en lecture seule dans l'environnement ; l'index avait déjà des chemins en conflit avant ces changements.
- Plan détaillé de tests manuels généré dans [`tests-manuels.md`](tests-manuels.md), avec prérequis, seed, parcours RH/public, contrôles PostgreSQL et fiche de résultats.

## Points ouverts relevés

- Le BLOC 1 n'expose pas de statut final de demande ni d'événement de fin de circuit. Le BLOC 2A déduit provisoirement l'acceptation lorsque la dernière décision de chaque étape obligatoire est `VALIDEE`.
- Le lien de `critere_recrutement` à une demande existe dans les entités et schémas actuellement lisibles ; le conflit non résolu dans `migrations/bloc1.sql` empêche de confirmer le script final.
- La date limite sera traitée comme un indicateur dérivé jusqu'à décision contraire.
- Les destinataires RH proviennent provisoirement de `APP_RECRUTEMENT_RH_EMPLOYE_IDS`. Les tolérances de classement par défaut (un an, un niveau) restent à confirmer avec l'équipe.
- Le bloc 2A publie `DossierValideEvent` avec id candidature, candidat, offre et numéro ; Edinah doit confirmer ce contrat. L'annulation clôture les candidatures en NON_RETENUE ; motif d'annulation et durée de conservation restent à décider.
- Le compte RH connecté est résolu par l'email ou matricule via le repository du bloc 1 ; les routes RH du recrutement exigent le rôle RH.
- Le jeu SQL de test est préparé, mais dépend d'une base PostgreSQL et du conflit à résoudre dans `migrations/bloc1.sql`.
