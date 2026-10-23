# Application RH — Recrutement (version finale)

**Principe :** l'application fait le maximum toute seule. Une personne n'intervient que pour **valider ou décider**, en un clic autant que possible.

## Vue d'ensemble

**Offre → Candidature → Étude du dossier → Test technique → Entretien 1 (RH) → Entretien 2 (Technicien) → Entretien 3 (DG) → Confirmation finale → Bloc 3**

---

## Étape 1 — Création de l'offre

**Résumé : Demande acceptée → Offre créée automatiquement → Validation du RH → Publication sur le site vitrine**

**Ce qui se passe**
1. L'application récupère les informations de la demande de recrutement acceptée (bloc 1) et **crée l'offre automatiquement**.
2. Le RH vérifie l'offre, la complète si besoin et la **valide**.
3. Après validation, l'offre est **publiée** sur le site vitrine.

**Règles**
- Une offre vient toujours d'une demande acceptée.
- Une offre incomplète ne peut pas être validée ni publiée.
- Il faut au moins 1 poste à pourvoir.
- La date limite doit être après la date de publication.
- Une demande ne donne pas plusieurs offres en même temps (sauf exception).
- Une offre publiée ne se supprime pas simplement : il faut suivre une procédure.

**Fait automatiquement**
- Création de l'offre depuis la demande.
- Blocage de la publication si une information manque (avec la liste).
- Enregistrement de la date de publication.
- Arrêt des candidatures à la date limite.

---

## Étape 2 — Dépôt de candidature

**Résumé : Consultation des offres sur le site vitrine → « Postuler » → Formulaire (CV numérique) → Téléchargement du CV → « Envoyer ma candidature » → E-mail au candidat + notification au RH**

**Ce qui se passe**
1. Le candidat consulte les offres publiées sur le site vitrine.
2. Il clique sur **« Postuler »**.
3. Un **formulaire** s'affiche pour obtenir un **CV numérique**.
4. À la fin, il **télécharge son CV** (fichier **PDF** ou **DOCX**, 5 Mo maximum).
5. Il clique sur **« Envoyer ma candidature »**.
6. L'application envoie automatiquement :
   - un **e-mail au candidat** : « Votre dossier est en cours d'étude » ;
   - une **notification au RH**.

**Candidature spontanée (possible)**
- Le candidat peut aussi envoyer une candidature sans offre précise, avec le même formulaire.
- Elle est gardée dans une **réserve de candidatures**.
- Quand une offre correspond, le RH la **rattache à l'offre en un clic**. Elle suit alors le parcours normal, à partir de l'étude du dossier.

**Règles**
- On ne postule que sur une offre ouverte (sauf candidature spontanée).
- Une candidature est liée à un candidat et à une offre (sauf candidature spontanée, tant qu'elle n'est pas rattachée).
- Les champs obligatoires et le CV doivent être fournis avant l'envoi.
- Seuls les formats PDF et DOCX sont acceptés.
- Pas de double candidature pour la même offre (sauf exception).
- Recevoir une candidature ne veut pas dire que le candidat est choisi.
- Les conditions du poste (salaire, etc.) ne sont **pas négociables** dans le formulaire.

**Fait automatiquement**
- Vérification des champs obligatoires, du fichier et de son format.
- Numéro et date de réception pour chaque candidature.
- Détection des doubles candidatures.
- Refus avec explication si l'offre est fermée.
- E-mail au candidat et notification au RH.
- Mise à jour du nombre de candidatures de l'offre.

---

## Étape 3 — Étude et sélection de dossier

**Résumé : Classement automatique → Consultation par le RH → Validation → E-mail au candidat (convocation ou réponse)**

**Ce qui se passe**
1. Chaque candidature est **classée automatiquement** selon les règles ci-dessous.
2. Le RH suit facilement les candidatures grâce à ce classement.
3. Le RH consulte chaque candidature et la **valide** (ou change son classement).
4. Après validation, le candidat reçoit **automatiquement un e-mail** :
   - **Conforme** : convocation au test technique ;
   - **Non conforme** : message de réponse.

**Comment le classement fonctionne**

Pour chaque offre, le RH indique des **critères obligatoires** (exemples pour un développeur web : diplôme requis, 2 ans d'expérience minimum, compétence « JavaScript »). L'application vérifie chacun dans le CV numérique.

| Classement | Quand ? | Exemple |
|---|---|---|
| **Conforme** | **Tous** les critères obligatoires sont remplis. | Diplôme requis, 3 ans d'expérience, JavaScript indiqué. |
| **Non conforme** | **Au moins un** critère obligatoire n'est clairement pas rempli. | Pas de diplôme demandé, ou 0 an d'expérience. |
| **À regarder** | Rien ne manque clairement, mais **un critère est douteux ou incomplet**. | Expérience juste en dessous du minimum, diplôme proche, information manquante. |

- Les critères « souhaités » (bonus) ne changent pas le classement : ils sont seulement affichés au RH pour l'aider.
- Les critères sont les mêmes pour tous les candidats d'une offre et sont fixés avant l'étude des dossiers.

**Délai pour le RH**
- Chaque candidature doit être traitée dans un délai maximum de **5 jours ouvrables**.
- Passé ce délai, un **rappel automatique** est envoyé au RH, avec la liste des dossiers en retard.

**Règles**
- Le classement automatique est une aide : la validation est faite par le RH.
- Chaque décision est gardée dans l'historique.
- « Non conforme » ne veut pas dire « supprimé » : le dossier est conservé.

**Fait automatiquement**
- Classement des candidatures dès leur réception.
- Alerte au RH quand des candidatures attendent.
- Rappel quand le délai est dépassé.
- E-mail au candidat après validation.
- Mise à jour de la situation de chaque candidature.

---

## Étape 4 — Test technique

**Résumé : Convocation + accès → Connexion → Test sur place (une seule séance) → Notes automatiques + correction des réponses libres → Résultats au RH → Confirmation des admis → E-mail pour l'étape suivante**

**Ce qui se passe**
1. À la validation du dossier, l'application **crée automatiquement l'accès du candidat** (identifiant et mot de passe provisoire, à changer à la première connexion). Il les reçoit dans l'e-mail de convocation.
2. Le jour du test, le candidat vient dans l'entreprise et **se connecte**. Cela permet de suivre qui répond à quelle offre.
3. Il passe le test en **une seule séance** : un grand nombre de questions, surtout à cocher, très peu de réponses libres. Ses réponses sont **sauvegardées automatiquement**, et le test se termine à la fin du temps prévu.
4. Les questions à cocher sont **corrigées automatiquement**.
5. Les **réponses libres** sont envoyées à la **direction responsable** (celle du service qui recrute). Elle les corrige et **saisit les notes** pour compléter le total.
6. Quand tout est corrigé, le RH reçoit les **résultats**, avec la **note minimale** (seuil) qui indique qui est admis ou non.
7. Le RH **confirme** les candidats acceptés.
8. Les candidats reçoivent **automatiquement un e-mail** (convocation à l'entretien 1 pour les admis, réponse pour les autres).

**Règles**
- Seuls les candidats dont le dossier est validé sont convoqués.
- Le seuil est une **note minimale générale**, fixée avant le test.
- Un candidat absent est noté « absent » et n'est pas retenu. Le RH peut accorder une **exception** en indiquant le motif (il n'y a normalement qu'une seule séance).
- En cas de panne, le RH peut relancer le test du candidat : ses réponses déjà données sont conservées.
- Les notes sont gardées avec la candidature.

**Fait automatiquement**
- Création et envoi de l'accès du candidat.
- Rappel avant la date du test.
- Enregistrement des présences et des absences.
- Correction des questions à cocher.
- Envoi des réponses libres à la direction responsable, avec rappel si la correction tarde.
- Calcul de la note totale et comparaison avec le seuil.
- Envoi des résultats au RH quand tout est corrigé.
- E-mails après la confirmation du RH.

---

## Étapes 5, 6 et 7 — Les trois niveaux d'entretien

**Résumé : Entretien 1 (RH) → Entretien 2 (Technicien) → Entretien 3 (DG)**

| Niveau | Avec qui |
|---|---|
| **1** | RH |
| **2** | Technicien |
| **3** | DG |

Les trois niveaux suivent la **même procédure**, l'un après l'autre. On ne peut pas passer à un niveau sans avoir réussi le précédent.

**Procédure commune**

**Résumé : Convocation + choix du créneau → Entretien → Notes saisies sur ordinateur → Calcul automatique → Confirmation du RH → E-mail au candidat**

1. Le candidat reçoit un e-mail avec un lien : il **choisit lui-même son créneau** parmi les horaires libres du jury (délai de 3 jours, avec un rappel automatique).
2. Le rendez-vous est confirmé et il reçoit un rappel avant la date.
3. Pendant l'entretien, chaque membre du jury remplit ses **notes sur ordinateur**, de **1 à 5**, **par question** ou **par critère**.
4. Les **questions sont connues à l'avance** : elles sont préparées dans une **grille d'évaluation**. Le RH la prépare ; pour l'entretien 2, le technicien complète les questions techniques. La grille ne peut plus être modifiée une fois le niveau commencé.
5. S'il y a plusieurs jurés, l'application fait la **moyenne** de leurs notes pour chaque question.
6. L'application calcule la note générale et la compare aux seuils.
7. Le RH confirme les candidats qui passent au niveau suivant.
8. Chaque candidat reçoit **automatiquement un e-mail** avec la suite ou la réponse.

**Notes et seuils**
- Chaque question a une **importance** : **1** (normale), **2** (importante) ou **3** (très importante). Par défaut, c'est 1. La note générale en tient compte.
- Deux seuils sont possibles, ensemble :
  - une **note générale minimale** (exemple : au moins 3,5/5) ;
  - pour certaines questions marquées **éliminatoires**, une **note minimale** (exemple : au moins 3/5 sur la question 4).
- Un candidat est **proposé comme réussi** s'il atteint la note générale minimale **et** respecte les minimums des questions éliminatoires.
- Si une question éliminatoire est échouée alors que la moyenne est bonne, l'application propose « non admis » et le RH tranche avec un motif.

**Règles**
- Seuls les candidats ayant réussi le niveau précédent sont convoqués.
- Chaque entretien est lié à une candidature et indique son niveau.
- Les notes sont saisies par les membres du jury, de 1 à 5.
- Une absence ou un changement de date est noté, sans effacer le rendez-vous d'origine.
- Le jury du niveau suivant peut consulter les notes des niveaux précédents.

**Fait automatiquement**
- Proposition des créneaux libres du jury.
- Confirmation, invitation et rappel avant la date.
- Grille d'évaluation prête à remplir sur ordinateur.
- Calcul des moyennes, de la note générale et comparaison avec les seuils.
- Envoi des résultats au RH.
- E-mail au candidat après confirmation.
- Rappel au jury si les notes ne sont pas saisies après l'entretien.

---

## Confirmation finale et passage au bloc 3

**Résumé : Entretien 3 réussi → Dossier complet et classement → Confirmation finale du RH → E-mail au candidat → Envoi au bloc 3**

**Ce qui se passe**
1. Après l'entretien 3, l'application prépare le **résumé complet** de chaque candidat : dossier, note du test, notes des trois entretiens.
2. Elle calcule une **note finale** (moyenne du test et des trois entretiens, ramenés sur la même échelle) et **classe** les candidats.
3. Le **DG** donne son avis à l'entretien 3. Le **RH** fait la **confirmation finale**.
4. Les candidats recrutés sont prévenus automatiquement. Leur dossier est **envoyé au bloc 3**.

**Plusieurs postes**
- Les candidats sont recrutés dans l'ordre du classement, jusqu'au nombre de postes.
- Les autres candidats qui ont réussi vont sur une **liste d'attente**, dans l'ordre.
- Si un candidat recruté refuse, l'application propose automatiquement le suivant au RH.

**Ce qui est envoyé au bloc 3**
Identité et contacts du candidat, poste et offre concernés, date de recrutement, CV, résumé des notes.

**Règles**
- Pas de confirmation finale s'il manque une étape obligatoire (sauf autorisation spéciale).
- Chaque décision indique son auteur et sa date.
- Pas plus de candidats recrutés que de postes.
- Toutes les décisions sont gardées dans un historique.

**Fait automatiquement**
- Résumé, note finale et classement.
- Alerte « prêt pour confirmation ».
- Mise à jour de la situation du candidat et e-mail.
- Envoi du dossier au bloc 3.
- Gestion de la liste d'attente.
- Quand tous les postes sont pourvus : fin de l'offre, message aux candidats encore en cours, conservation de l'historique.

---

## Notifications et e-mails

| Moment | Qui reçoit | Contenu |
|---|---|---|
| Candidature envoyée | Candidat | « Votre dossier est en cours d'étude » |
| Candidature envoyée | **RH** | Nouvelle candidature reçue |
| Dossiers en attente depuis plus de 5 jours | **RH** | Rappel avec la liste |
| Dossier validé (conforme) | Candidat | Convocation au test + accès personnel |
| Dossier validé (non conforme) | Candidat | Message de réponse |
| Avant le test ou un entretien | Candidat | Rappel |
| Test terminé avec réponses libres | **Direction responsable** | Réponses à corriger |
| Test entièrement corrigé | **RH** | Résultats et comparaison avec le seuil |
| Admis confirmés par le RH | Candidat | Lien pour choisir le créneau de l'entretien 1 |
| Résultats d'un entretien prêts | **RH** | Notes du jury et comparaison avec les seuils |
| Entretien 1 ou 2 confirmé par le RH | Candidat | Lien pour choisir le créneau du niveau suivant |
| Échec à une étape | Candidat | Message de réponse |
| Confirmation finale | Candidat | Réponse définitive |

---

## Règles les plus importantes

| Sujet | Règle |
|---|---|
| Offre | Vient d'une demande acceptée, validée par le RH avant publication |
| Candidature | Liée à un candidat et à une offre (sauf spontanée), CV en PDF ou DOCX |
| Double candidature | Une seule par personne et par offre |
| Classement des dossiers | Critères obligatoires fixés d'avance ; conforme, non conforme, à regarder ; validation par le RH |
| Test technique | Connexion obligatoire, une seule séance, seuil fixé d'avance |
| Entretiens | Dans l'ordre : RH, Technicien, DG, sans sauter de niveau |
| Notes | De 1 à 5, grille connue à l'avance |
| Seuils | Note générale minimale + minimum sur les questions éliminatoires |
| Passage à l'étape suivante | Toujours confirmé par le RH |
| Résultats | Conservés et protégés |
| Décision finale | Avec le nom de l'auteur et la date |
| Historique | Toutes les décisions importantes sont gardées |

---

## Situations possibles

**Une offre** : brouillon → validée → publiée → terminée (ou suspendue / annulée)

**Une candidature** : reçue → à étudier → dossier validé → test technique → entretien RH → entretien technicien → entretien DG → confirmation finale

**Cas particuliers** : spontanée (dans la réserve), en liste d'attente, retirée par le candidat

**Résultat** : recruté, non retenu (à n'importe quelle étape), en liste d'attente

**Test ou entretien** : convoqué → réalisé (ou reporté / annulé / candidat absent)

> Une candidature ne peut pas sauter d'étape.

---

## Informations à gérer

- **Offre d'emploi** (avec ses critères obligatoires et souhaités)
- **Candidat** (avec son accès pour le test)
- **Candidature** (avec le CV numérique et le fichier de CV)
- **Test technique** (questions, réponses, correction, note, seuil)
- **Entretien** (avec son niveau : 1, 2 ou 3)
- **Grille d'évaluation** (questions, importance, éliminatoire ou non, seuils)
- **Notes du jury** (de 1 à 5 par question ou critère)

---

## Décisions retenues

| Sujet | Décision | Origine |
|---|---|---|
| Négociation des conditions par le candidat | Non | Équipe |
| Formats du CV | PDF et DOCX (5 Mo max) | Équipe + proposition |
| Candidature spontanée | Possible, via une réserve de candidatures | Équipe + proposition |
| Critères de classement | Voir tableau de l'étape 3 | Proposition |
| Délai du RH pour les dossiers | Gardé : 5 jours ouvrables, avec rappel | Équipe + proposition |
| Dossier non conforme | Message de réponse après validation du RH | Proposition |
| Accès du candidat au test | Compte créé automatiquement, envoyé avec la convocation | Équipe |
| Réponses libres du test | Corrigées par la direction responsable | Équipe |
| Séance de test | Une seule, avec beaucoup de questions | Équipe |
| Absence ou panne au test | Absent = non retenu (exception possible du RH) ; test relancé en cas de panne | Proposition |
| Seuil du test | Note minimale générale | Proposition |
| Passage après un entretien | Confirmé par le RH | Proposition |
| Plusieurs jurés | Moyenne des notes | Proposition |
| Importance des questions | 1, 2 ou 3 (défaut : 1) | Proposition |
| Seuils des entretiens | Note générale + questions éliminatoires | Équipe + proposition |
| Dates des entretiens | Le candidat choisit parmi les créneaux libres | Proposition |
| Grille d'évaluation | Préparée par le RH (technicien pour le niveau 2), figée au début du niveau | Proposition |
| Décision du DG | Avis à l'entretien 3 ; confirmation finale par le RH | Proposition |
| Plusieurs postes | Classement par note finale + liste d'attente | Proposition |
| Données envoyées au bloc 3 | Identité, contacts, poste, offre, date, CV, résumé des notes | Proposition |
| Fin de l'offre | Candidatures arrêtées à la date limite ; offre terminée quand les postes sont pourvus | Proposition |
| Conservation des dossiers non retenus | 2 ans, puis suppression automatique (ou plus tôt à la demande du candidat) | Proposition |

**Qui voit quoi**

| Personne | Peut voir |
|---|---|
| Candidat | Ses propres informations et son état d'avancement |
| RH | Tout |
| Technicien | Dossiers et notes des candidats de son entretien et des niveaux précédents |
| DG | Résumés et notes des candidats de l'entretien 3 et des niveaux précédents |
| Direction responsable | Seulement les réponses libres à corriger |

---

## Documentation technique du bloc 2A

La documentation détaillée, les diagrammes et les maquettes sont dans [`b2a/bloc2a.md`](b2a/bloc2a.md), [`b2a/user-flow.md`](b2a/user-flow.md) et [`b2a/maquettes/`](b2a/maquettes/). Le bloc 2A ajoute les packages `*.recrutement`, les migrations `b2a_recrutement.sql` et `b2a_donnees_test.sql`, les routes RH/publiques décrites dans le guide, les paramètres `app.mail.*`, `app.cv.*` et `app.recrutement.*`, ainsi que les starters mail/validation. L'événement `DossierValideEvent` constitue le point d'intégration candidat vers le bloc 2B; aucun compte candidat n'est créé ici.

## Point encore à voir

1. **Champs du CV numérique** : quels champs exactement ? Proposition de départ : identité et contacts, études, expériences, compétences, langues, disponibilité.
2. **Durée de conservation (2 ans)** : à confirmer avec les règles légales sur les données personnelles.
