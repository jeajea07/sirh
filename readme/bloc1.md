# Application RH — Manifestation de besoin en personnel (version finale)

**Principe :** l'application fait le maximum toute seule. Une personne n'intervient que pour **valider ou décider**, en un clic autant que possible.

## Vue d'ensemble

**Demande → Validation du responsable de département → Vérification RH → Vérification Finance → Décision de la DG → Bloc 2**

---

## Étape 1 — Rédaction et envoi de la demande

**Résumé : Formulaire de demande → Brouillon (facultatif) → Contrôle automatique de complétude → « Soumettre » → Notification au validateur suivant**

**Ce qui se passe**
1. Un manager ou un employé d'un département remplit le **formulaire de demande de personnel**.
2. Il peut **enregistrer un brouillon** et le terminer plus tard.
3. À la soumission, l'application vérifie que tout est rempli, puis envoie la demande au validateur suivant.

**Contenu du formulaire**
- Département du demandeur et son poste.
- Poste à pourvoir et nombre de postes.
- **Motif** : *remplacement* (avec l'employé sortant), *poste existant (renfort)* ou *création de poste* (avec une justification plus détaillée).
- **Source de pourvoi** : *interne*, *externe* ou *mixte* (voir « Promotion et mobilité interne »).
- Type de contrat souhaité (CDI, CDD…) et, pour un CDD, la durée.
- Critères du poste : obligatoires et souhaités (diplôme, expérience, compétences).
- Salaire proposé.
- Date souhaitée de prise de poste.
- Justification.

**Règles**
- Tout employé peut créer une demande.
- Une demande incomplète ne peut pas être soumise.
- Une demande de **remplacement** doit indiquer l'employé sortant.
- La date souhaitée ne peut pas être dans le passé.
- Une demande soumise ne se modifie plus directement : seul le circuit de correction (voir plus bas) la rouvre.
- Le demandeur peut **retirer** sa demande tant qu'elle n'est pas validée par la DG.

**Fait automatiquement**
- Numéro et date pour chaque demande.
- Blocage de la soumission si une information manque (avec la liste).
- Détection des **demandes en double** (même département, même poste, demande encore en cours).
- Vérification que le poste existe dans l'organigramme.
- Notification au validateur suivant.

---

## Étape 2 — Validation du responsable de département

**Résumé : Demande d'un employé → Avis du responsable hiérarchique → Envoi au RH**

**Ce qui se passe**
1. Si le demandeur **n'est pas lui-même manager**, la demande va d'abord à son responsable de département.
2. Le responsable **valide**, **renvoie pour correction** ou **rejette**.
3. Si validée, la demande passe au RH.

**Règles**
- Si le demandeur est manager, cette étape est sautée.
- Un rejet ou un renvoi demande toujours un commentaire.

**Fait automatiquement**
- Choix du bon responsable selon le département.
- Étape sautée si le demandeur est manager.
- Rappel si la demande attend trop longtemps.

---

## Étape 3 — Vérification par le RH

**Résumé : Demande reçue → Contrôles automatiques → Consultation et correction par le RH → Valider / Renvoyer / Rejeter**

**Ce qui se passe**
1. L'application effectue les **contrôles automatiques** dès l'arrivée de la demande.
2. Le RH consulte la demande avec le résultat des contrôles.
3. Il peut **corriger** certains champs (par exemple le salaire ou les critères).
4. Il **valide**, **renvoie au demandeur pour correction** (avec commentaire) ou **rejette** (avec motif).
5. Si validée, la demande passe à la Finance.

**Contrôles automatiques**

| Contrôle | Résultat |
|---|---|
| **Salaire et grille salariale** | Dans la grille : conforme. Hors grille : **écart signalé** au RH (pas de blocage). |
| **Effectif du département** | Effectif autorisé déjà atteint : alerte au RH. |
| **Complétude des critères** | Critères obligatoires présents : conforme. |
| **Demande en double** | Demande similaire en cours : alerte au RH. |
| **Cohérence motif / contrat** | Exemple : renfort sans CDD, avertissement. |

- Les alertes **aident** le RH, elles ne décident pas à sa place.
- Un salaire hors grille n'est jamais refusé automatiquement : le RH tranche.

**Règles**
- Toute correction du RH est gardée avec : valeur saisie, valeur corrigée, auteur, date.
- Un rejet ou un renvoi demande toujours un commentaire.
- Le RH ne peut pas valider une demande dont le salaire est hors grille sans indiquer un motif.

**Délai pour le RH**
- Délai maximum de **5 jours ouvrables** (paramétrable).
- Passé ce délai, un **rappel automatique** est envoyé au RH, puis une **escalade** vers son supérieur ou son suppléant.

**Fait automatiquement**
- Contrôles dès la réception.
- Comparaison du salaire avec la grille.
- Historique des corrections.
- Notification au demandeur en cas de renvoi ou de rejet.
- Rappel et escalade en cas de retard.

---

## Étape 4 — Vérification par la Finance

**Résumé : Demande validée par le RH → Contrôle automatique du budget → Consultation et correction par la Finance → Valider / Renvoyer / Rejeter**

**Ce qui se passe**
1. L'application calcule le **coût de la demande** et le compare au **budget disponible** du département.
2. La Finance consulte la demande avec le résultat du contrôle.
3. Elle peut **corriger** ce qui relève de son domaine (par exemple le salaire proposé ou la période d'engagement).
4. Elle **valide**, **renvoie** (avec commentaire) ou **rejette** (avec motif).
5. Si validée, la demande passe à la DG.

**Contrôle du budget**

| Situation | Résultat |
|---|---|
| Budget disponible suffisant | **Conforme** |
| Budget insuffisant | **Dépassement signalé** à la Finance avec le montant manquant |
| Demande de remplacement | **Étape allégée** : le poste est déjà budgété, validation en un clic |

- Le budget disponible est le montant alloué moins le montant déjà engagé.
- Un dépassement n'est pas bloqué automatiquement : la Finance décide et indique un motif.

**Règles**
- Toute correction de la Finance est gardée avec ses valeurs avant et après, son auteur et sa date.
- Un rejet ou un renvoi demande toujours un commentaire.
- Si la Finance modifie le salaire, le RH en est informé (le salaire doit rester dans la grille).

**Fait automatiquement**
- Calcul du coût et comparaison avec le budget.
- Allègement pour les remplacements.
- Mise à jour du montant engagé quand la DG valide.
- Rappel et escalade en cas de retard.

---

## Étape 5 — Décision de la Direction Générale

**Résumé : Demande validée par la Finance → Résumé complet pour la DG → Valider / Rejeter → Notification à tous → Passage au bloc 2**

**Ce qui se passe**
1. La DG reçoit un **résumé complet** : demande, résultats des contrôles RH et Finance, corrections faites, commentaires.
2. Elle **valide** ou **rejette** (motif obligatoire), ou renvoie en arrière avec commentaire.
3. Le demandeur, le RH et la Finance sont **prévenus automatiquement**.
4. Si la demande est validée, elle est **transmise au bloc 2**.

**Règles**
- La décision de la DG est finale.
- Une demande rejetée est conservée dans l'historique et ne se supprime pas.
- Chaque décision indique son auteur et sa date.

**Fait automatiquement**
- Résumé prêt à décider.
- Notifications après la décision.
- Réservation du montant dans le budget quand la demande est validée.
- Transmission au bloc 2.

---

## Promotion et mobilité interne

**Résumé : Demande validée → Si source interne ou mixte : publication interne d'abord → Si personne n'est retenu : passage en externe (bloc 2)**

Un poste validé n'est pas forcément pourvu par un recrutement externe. Le choix se fait avec le champ **source de pourvoi**.

| Source | Comportement après validation de la DG |
|---|---|
| **Interne** | L'offre est ouverte d'abord aux employés. Si personne n'est retenu, le RH peut la passer en externe. |
| **Mixte** | L'offre est ouverte en interne pendant un délai, puis passe automatiquement en externe. |
| **Externe** | L'offre va directement en recrutement externe. |

**Règles**
- Le délai d'ouverture en interne est **paramétrable** (par exemple de 7 à 15 jours).
- Les employés éligibles peuvent postuler, et le RH peut proposer des candidats internes.
- Si un employé est promu ou muté, son **ancien poste devient vacant** : une **demande de remplacement** est créée automatiquement, liée à la promotion d'origine.
- Cette demande de remplacement suit le circuit normal, avec la Finance allégée (poste déjà budgété).
- La sélection des candidats internes suit le processus du bloc 2.

**Fait automatiquement**
- Information donnée au bloc 2 sur la source de pourvoi et le délai interne.
- Création de la demande de remplacement après une promotion.
- Lien entre la demande de remplacement et son origine.
- Passage en externe à la fin du délai si aucun candidat interne n'est retenu (source mixte).

---

## Corrections, renvois et rejets

À chaque étape, le validateur a **trois choix** :

| Choix | Effet |
|---|---|
| **Valider** | La demande passe à l'étape suivante. |
| **Renvoyer pour correction** | Retour au demandeur avec un commentaire. Il corrige et **resoumet sans tout ressaisir**. La demande repart de l'étape qui l'a renvoyée. |
| **Rejeter** | La demande est fermée, avec un motif obligatoire. |

**Règles**
- Après une correction, seules les étapes concernées sont refaites (le salaire modifié repasse par le contrôle de la grille, par exemple).
- Chaque renvoi et chaque rejet est gardé dans l'historique.
- Un demandeur peut refaire une nouvelle demande à partir d'une demande rejetée (les informations sont reprises).

---

## Notifications et e-mails

| Moment | Qui reçoit | Contenu |
|---|---|---|
| Demande soumise | Responsable de département (ou RH si manager) | Nouvelle demande à traiter |
| Demande validée par le responsable | **RH** | Nouvelle demande à vérifier |
| Contrôle RH avec écart ou alerte | **RH** | Liste des alertes |
| Demande validée par le RH | **Finance** | Nouvelle demande à vérifier |
| Dépassement de budget | **Finance** | Montant manquant |
| Demande validée par la Finance | **DG** | Résumé à décider |
| Demande en attente trop longtemps | Validateur concerné | Rappel, puis escalade |
| Demande renvoyée | Demandeur | Commentaire et lien pour corriger |
| Demande rejetée | Demandeur | Motif du rejet |
| Décision finale de la DG | Demandeur, RH, Finance | Demande validée ou rejetée |
| Demande validée | **RH** (bloc 2) | Offre créée automatiquement |

---

## Règles les plus importantes

| Sujet | Règle |
|---|---|
| Qui peut demander | Tout employé ; validation d'abord par son responsable si ce n'est pas un manager |
| Ordre du circuit | Responsable, RH, Finance, DG, sans sauter d'étape |
| Salaire | Contrôlé avec la grille ; hors grille : écart signalé, le RH tranche avec un motif |
| Budget | Contrôlé automatiquement ; dépassement signalé, la Finance tranche avec un motif |
| Remplacement | Finance allégée (poste déjà budgété) |
| Corrections | Toujours gardées : valeur avant, valeur après, auteur, date |
| Rejet et renvoi | Toujours avec commentaire |
| Délais | 5 jours ouvrables par étape, rappel puis escalade |
| Décision finale | La DG, avec le nom de l'auteur et la date |
| Après validation | Transmission automatique au bloc 2 |
| Historique | Toutes les décisions et corrections sont gardées |

---

## Situations possibles

**Une demande** : brouillon → soumise → validation du responsable → vérification RH → vérification Finance → attente DG → validée → transmise au bloc 2

**Cas particuliers** : à corriger (renvoyée au demandeur), rejetée (à n'importe quelle étape), retirée par le demandeur

**Une étape de validation** : en attente → traitée (validée, renvoyée ou rejetée) ; ou en retard (rappel puis escalade)

> Une demande ne peut pas sauter d'étape.

---

## Informations à gérer

- **Demande de besoin** (département, poste, motif, source de pourvoi, type de contrat, salaire proposé, date souhaitée, critères, statut, demandeur, demande d'origine)
- **Validation d'étape** (étape, validateur, décision, commentaire, date)
- **Historique des modifications** (champ, ancienne valeur, nouvelle valeur, auteur, date)
- **Grille salariale** (poste ou niveau, minimum, maximum)
- **Budget du département** (période, montant alloué, montant engagé)
- **Organigramme** (départements, postes, effectif autorisé)

---

## Décisions retenues

| Sujet | Décision | Origine |
|---|---|---|
| Circuit de validation | Demandeur, RH, Finance, DG | Équipe |
| Contrôle du salaire | Automatique, avec la grille salariale du RH | Équipe |
| Contrôle du budget | Automatique, par la Finance | Équipe |
| Décision finale | La Direction Générale | Équipe |
| Promotion | Moyen de pourvoir un poste validé : interne d'abord, puis externe | Équipe |
| Remplacement après promotion | Demande créée automatiquement, Finance allégée | Proposition |
| Trois issues à chaque étape | Valider, renvoyer pour correction, rejeter (commentaire obligatoire) | Proposition |
| Historique des corrections | Valeur avant et après, auteur, date | Proposition |
| Motifs de la demande | Remplacement, poste existant (renfort), création de poste | Proposition |
| Qui peut demander | Tout employé, avec validation du responsable | Proposition |
| Salaire hors grille | Écart signalé, jamais bloqué | Proposition |
| Délais et relances | 5 jours ouvrables, rappel puis escalade | Proposition |
| Statuts | Brouillon, soumise, en vérification, à corriger, validée, rejetée, retirée | Proposition |
| Circuit configurable par entreprise | Prévu comme extension ; version de base avec circuit fixe | Proposition |

**Qui voit quoi**

| Personne | Peut voir |
|---|---|
| Demandeur | Ses propres demandes et leur état d'avancement |
| Responsable de département | Les demandes de son département |
| RH | Toutes les demandes |
| Finance | Les demandes arrivées à son étape, et le budget |
| DG | Les demandes arrivées à son étape, avec résumé complet |

---

## Points encore à voir

1. **Circuit configurable** : pour qu'une startup sans service Finance puisse utiliser l'application, l'ordre et les rôles des étapes doivent pouvoir se paramétrer. À présenter comme extension.
2. **Seuil de la DG** : faut-il que la DG valide toutes les demandes, ou seulement celles au-dessus d'un certain coût ?
3. **Motifs de la demande** : confirmer la liste (remplacement, renfort, création de poste).
4. **Délai interne** : durée par défaut de l'ouverture interne pour les promotions et la mobilité (7 ou 15 jours).