# Plan de tests manuels — Bloc 2A

Ce guide permet de vérifier le parcours de recrutement dans l'application. Coche les cases pendant l'essai et note le résultat réel ainsi que toute anomalie.

## 1. Préparer l'environnement

### Prérequis

- [ ] PostgreSQL est démarré et accessible à l'adresse configurée dans `src/main/resources/application.properties` (par défaut `localhost:5432/gestionrh`).
- [ ] Les migrations du bloc 1 puis `migrations/b2a_recrutement.sql` ont été appliquées, dans cet ordre.
- [ ] Un compte RH existant permet de se connecter. Le seed B2A crée un employé de test (`matricule B2A-RH-TEST`), mais ne crée pas son compte de connexion.
- [ ] L'application démarre en Java 21 et les JSP s'affichent.
- [ ] Pour le premier passage, désactiver l'envoi réel : `APP_MAIL_ACTIF=false`. Les messages sont simulés dans les journaux.
- [ ] Définir `APP_RECRUTEMENT_RH_EMPLOYE_IDS` avec l'identifiant de l'employé RH connecté, afin de recevoir les notifications internes.
- [ ] Définir `APP_CV_DOSSIER_STOCKAGE` vers un dossier de test privé, hors du répertoire web. Vérifier que l'application peut y créer des fichiers.
- [ ] Utiliser une base locale réservée aux tests. Le script de données modifie la base et ne doit pas être exécuté en production.

Appliquer le jeu d'essai après les migrations :

```bash
psql -h localhost -p 5432 -U postgres -d gestionrh -v ON_ERROR_STOP=1 -f migrations/b2a_donnees_test.sql
```

Le seed peut être rejoué. Il ajoute des offres `OFF-2099-9001` à `OFF-2099-9005` (brouillon, validée, publiée, suspendue, terminée) et des candidatures `CAND-2099-90001` à `CAND-2099-90004`. Les fichiers CV de ces candidatures sont des références fictives : elles ne permettent pas de tester leur téléchargement.

### Données et accès à noter

- URL de l'application : ______________________________
- Identifiant du compte RH de test : ___________________
- Identifiant employé configuré pour les notifications : __________
- Dossier de stockage CV : ____________________________
- Date et heure de l'essai : ___________________________

Pour vérifier le seed :

```sql
SELECT reference, statut, date_limite FROM offre WHERE reference LIKE 'OFF-2099-9%' ORDER BY reference;
SELECT numero, statut, classement_auto, id_offre FROM candidature WHERE numero LIKE 'CAND-2099-9%' ORDER BY numero;
```

## 2. Vitrine publique et offres

### T01 — Vitrine sans connexion

1. Ouvrir `GET /offres` dans une fenêtre privée ou après déconnexion.
2. Vérifier que l'offre publiée `OFF-2099-9003` apparaît.
3. Vérifier que le brouillon, l'offre validée non publiée, l'offre suspendue et l'offre terminée n'apparaissent pas.
4. Rechercher un mot contenu dans un titre, puis un mot inexistant.
5. Ouvrir le détail de l'offre publiée.

**Attendu :** page accessible sans compte, recherche filtrée, détails publics sans données candidat ni identifiants internes superflus, bouton « Postuler » présent.

### T02 — Limite de date

1. Dans une base de test, modifier temporairement la date limite de l'offre publiée en date passée.
2. Recharger la vitrine et le formulaire de candidature associé.
3. Restaurer une date future après le test.

**Attendu :** l'offre expirée n'est plus présentée comme ouverte et le dépôt est refusé avec un message explicite. Le statut peut rester `PUBLIEE` : la clôture par date est calculée, non persistée.

## 3. Gestion RH des offres

Pour chaque POST manuel ou par formulaire, utiliser la session RH et conserver le jeton CSRF affiché par la page.

### T03 — Contrôle d'accès et CSRF

1. Sans connexion, ouvrir `GET /recrutement/offres` et `GET /rh/recrutement/candidatures`.
2. Se connecter avec un compte non-RH et refaire les essais si un tel compte existe.
3. Depuis un formulaire RH, tenter une action POST sans jeton CSRF (par exemple via l'outil réseau du navigateur).

**Attendu :** seules les routes publiques sont accessibles sans rôle RH; une session non-RH ne peut pas gérer les offres/dossiers; les POST sans CSRF sont refusés.

### T04 — Création depuis une demande acceptée

1. Ouvrir `GET /recrutement/offres/nouveau`.
2. Choisir une demande acceptée et créer son offre.
3. Refaire l'action sur la même demande.

**Attendu :** offre créée en `BROUILLON`, champs disponibles préremplis, référence générée, critères récupérés s'ils sont disponibles. La seconde création ne doit pas créer une seconde offre active. Une demande non acceptée doit être refusée.

### T05 — Offre incomplète et cycle de vie

1. Sur une offre brouillon incomplète, tenter de valider.
2. Ajouter le titre, description, missions, salaire, au moins un poste, date limite future et au moins un critère obligatoire.
3. Valider, puis publier.
4. Depuis l'offre publiée, suspendre; tenter de postuler; reprendre; postuler de nouveau.
5. Terminer l'offre et vérifier la vitrine.

**Attendu :** les champs manquants sont listés; aucune transition invalide n'est acceptée; une publication renseigne la date de publication; l'offre suspendue/terminée n'accepte pas de candidature.

### T06 — Modification et suppression

1. Modifier un brouillon puis enregistrer.
2. Supprimer un brouillon sans candidature.
3. Tenter de modifier/supprimer une offre publiée ou une offre ayant des candidatures.

**Attendu :** la modification et la suppression simple ne sont autorisées que selon les règles du brouillon; une offre publiée est annulée, pas supprimée.

### T07 — Recherche, filtre et pagination RH

1. Ouvrir `/recrutement/offres`, filtrer par statut et rechercher un titre.
2. Cliquer sur les pages suivantes si plus de dix résultats sont présents.

**Attendu :** recherche et filtre se combinent, les critères restent dans les liens de pagination.

## 4. Dépôt public de candidature

Préparer deux fichiers de test non sensibles : un PDF valide (signature `%PDF`) et un DOCX valide (archive ZIP/OOXML). Ne pas utiliser de vrais CV.

### T08 — Candidature conforme

1. Depuis une offre publiée ouverte, cliquer « Postuler ».
2. Remplir tous les champs, ajouter les compétences, sélectionner le PDF valide et envoyer.
3. Noter le numéro affiché sur la confirmation.

**Attendu :** confirmation avec un numéro `CAND-AAAA-NNNNN`; statut `A_ETUDIER`; classement affiché en RH; accusé candidat simulé et notification RH créés; compteur de l'offre incrémenté.

### T09 — Champs obligatoires et fichier invalide

Répéter le dépôt avec les cas suivants :

- [ ] Champ obligatoire vide.
- [ ] Aucun fichier.
- [ ] Extension non admise (par exemple `.txt`).
- [ ] Fichier renommé `.pdf` dont le contenu n'est pas un PDF.
- [ ] Fichier supérieur à 5 Mio.
- [ ] DOCX valide.

**Attendu :** les cas invalides restent sur le formulaire avec message; aucun dossier n'est créé. Le DOCX valide est accepté. Vérifier que les messages ne révèlent pas de trace technique.

### T10 — Doublon et réutilisation du candidat

1. Déposer une candidature pour une offre avec une adresse e-mail donnée.
2. Refaire une candidature sur cette même offre avec la même adresse, puis avec la même adresse en majuscules/minuscules différentes.
3. Déposer avec cette adresse sur une autre offre ouverte.

**Attendu :** doublon refusé pour la même offre, y compris en variant la casse; pas de seconde candidature active créée. Pour une autre offre, le candidat existant est réutilisé.

### T11 — Offre fermée

Tenter de poster en appelant directement `/candidatures/nouvelle?offreId=<id>` sur une offre suspendue, terminée ou expirée.

**Attendu :** dépôt refusé avec explication; aucun fichier conservé dans le dossier privé, aucune ligne candidature ajoutée.

### T12 — Candidature spontanée

1. Ouvrir `/candidatures/nouvelle` sans paramètre `offreId`.
2. Soumettre le formulaire complet avec CV valide.

**Attendu :** candidature avec `id_offre` nul, indicateur spontanée vrai, statut `RECUE`, pas de classement automatique; accusé candidat et notification RH.

### T13 — Données et fichier privés

1. Vérifier le répertoire de stockage après un dépôt réussi.
2. Comparer le nom disque avec le nom transmis par le navigateur.
3. Essayer d'ouvrir un chemin direct vers le CV depuis le navigateur.

**Attendu :** nom disque non prédictible, fichier hors webroot, aucune URL publique de fichier; accès direct impossible.

## 5. Réserve RH et rattachement

### T14 — Recherche et rattachement d'une candidature spontanée

1. Ouvrir `/rh/recrutement/reserve` en RH.
2. Rechercher par nom, e-mail puis numéro.
3. Choisir une offre publiée et ouverte et cliquer « Rattacher ».
4. Essayer ensuite de rattacher une candidature déjà liée ou vers une offre fermée.

**Attendu :** le premier rattachement renseigne offre et date de rattachement, classe le dossier, passe à `A_ETUDIER`, crée l'historique `RATTACHEMENT` et met à jour le compteur. Les rattachements invalides/doublons sont refusés.

## 6. Classement automatique

Consulter `CAND-2099-90001` à `CAND-2099-90003` dans `/rh/recrutement/candidatures` et leurs détails.

### T15 — Cas du seed

- [ ] `CAND-2099-90001` : profil conforme; vérifier chaque critère obligatoire.
- [ ] `CAND-2099-90002` : profil non conforme; au moins un critère obligatoire est KO.
- [ ] `CAND-2099-90003` : profil à regarder; information manquante/proche du seuil.
- [ ] Le critère souhaité PostgreSQL apparaît au détail sans changer le classement.

**Attendu :** critère par critère, le détail expose valeur candidate et état OK/douteux/KO; un seul KO donne `NON_CONFORME`; sans KO mais avec doute donne `A_REGARDER`; tous OK donnent `CONFORME`.

### T16 — Frontières de critères

Créer des candidatures de test séparées couvrant :

- [ ] Niveau requis exact, un niveau en dessous, deux niveaux en dessous, niveau absent.
- [ ] Expérience au minimum, une année en dessous, deux années en dessous, expérience absente.
- [ ] Compétence présente avec casse/accents/espaces différents.
- [ ] Compétence absente des compétences mais mentionnée dans formation/expérience.
- [ ] Compétence absente avec d'autres compétences déclarées; aucune compétence déclarée.
- [ ] Langue présente, absente, champ langue vide.
- [ ] Critère souhaité manquant.

**Attendu avec tolérances par défaut :** un niveau ou une année en dessous est douteux; au-delà est KO. Les critères souhaités ne modifient jamais le classement.

## 7. Étude RH et décisions

### T17 — Recherche et détails de suivi

1. Ouvrir `/rh/recrutement/candidatures`.
2. Filtrer par offre, classement, statut, nom/e-mail/numéro; essayer plusieurs filtres ensemble.
3. Trier par date, numéro, statut et classement; fournir un nom de propriété de tri invalide dans l'URL.
4. Ouvrir le détail d'une vraie candidature créée à T08.
5. Télécharger son CV via le bouton.

**Attendu :** liste filtrée/paginée, tri invalide remplacé par un tri autorisé; détail contient le CV numérique, critères et historique; téléchargement contrôlé restitue le fichier avec nom/type correct.

### T18 — Décisions RH

1. Pour un dossier `A_REGARDER`, essayer de confirmer directement le classement automatique.
2. Décider `CONFORME` ou `NON_CONFORME` avec un commentaire si le classement diffère de l'automatique.
3. Tenter la même décision une seconde fois.
4. Pour un dossier conforme, vérifier l'événement `DossierValideEvent` dans les journaux.
5. Pour un dossier non conforme, vérifier l'e-mail simulé.

**Attendu :** `A_REGARDER` doit être tranché; commentaire obligatoire lors d'un changement; conforme devient `DOSSIER_VALIDE`; non conforme devient `NON_RETENUE`; auteur, date, décision et commentaire sont historisés; aucune seconde décision n'est permise. Pas de compte candidat ni de convocation créée ici : le bloc 2B doit écouter l'événement.

## 8. Notifications et rappels

### T19 — Notifications RH

1. Après T08, ouvrir `/recrutement/notifications` avec le RH destinataire configuré.
2. Marquer une notification comme lue, puis recharger.
3. Tenter d'ouvrir la page avec un autre RH.

**Attendu :** liste personnelle, compteur de non-lues décrémenté, une notification ne peut être marquée lue que par son destinataire.

### T20 — Mode e-mail

Avec `APP_MAIL_ACTIF=false`, effectuer réception, non-rétention et notification RH.

**Attendu :** mode simulation sans tentative SMTP. Vérifier les journaux sans y chercher de CV ou de données sensibles. N'activer `APP_MAIL_ACTIF=true` que si un serveur SMTP de test est configuré.

### T21 — Rappel après cinq jours ouvrables

Le seed contient des candidatures anciennes (`CAND-2099-90002` et `...90003`). Vérifier qu'elles sont toujours `A_ETUDIER`. Pour éviter d'attendre 8 h, configurer temporairement le cron de test à la prochaine minute, redémarrer l'application et attendre son déclenchement.

**Attendu :** notification groupée par RH avec numéro, candidat, offre et retard; jours fériés de `APP_RECRUTEMENT_JOURS_FERIES` et week-ends exclus; pas de doublon pour le même RH le même jour. Remettre la configuration normale ensuite.

## 9. Annulation d'offre

### T22 — Annulation et clôture des candidatures

1. Utiliser une offre publiée de test avec au moins une candidature active.
2. L'annuler depuis la page RH.
3. Vérifier statut de l'offre, statuts des candidatures, historique et notifications.

**Attendu :** offre `ANNULEE`; candidatures actives concernées `NON_RETENUE`; historique d'annulation/transition créé; notifications candidat et RH émises. Les données des dossiers restent présentes.

## 10. Contrôles en base après les essais

```sql
SELECT numero, id_offre, spontanee, statut, classement_auto, classement_final, date_validation
FROM candidature ORDER BY id_candidature DESC LIMIT 20;

SELECT c.numero, h.action, h.ancien_statut, h.nouveau_statut,
       h.ancien_classement, h.nouveau_classement, h.commentaire, h.date_action
FROM historique_candidature h JOIN candidature c USING (id_candidature)
ORDER BY h.date_action DESC LIMIT 40;

SELECT o.reference, o.nb_candidatures,
       count(c.id_candidature) FILTER (WHERE c.statut <> 'RETIREE') AS compte_reel
FROM offre o LEFT JOIN candidature c USING (id_offre)
GROUP BY o.id_offre ORDER BY o.reference;

SELECT canal, statut_envoi, lu, count(*)
FROM notification_recrutement GROUP BY canal, statut_envoi, lu;
```

**Attendu :** compteur stocké égal au nombre réel hors `RETIREE`; historique cohérent; destinataire de notification unique conformément au canal; aucune candidature dupliquée pour la même personne et offre.

## 11. Rapport de test à compléter

| ID | Résultat (OK/KO/Non testé) | Numéro offre/candidature | Étapes pour reproduire / anomalie |
|---|---|---|---|
| T01–T07 | | | |
| T08–T13 | | | |
| T14–T16 | | | |
| T17–T18 | | | |
| T19–T21 | | | |
| T22 | | | |

Ne pas corriger les lignes de seed directement en production. Pour refaire un scénario, utiliser une base locale de test ou créer de nouvelles données par l'interface.
