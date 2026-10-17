# BLOC 3 --- SÉLECTION ET EMBAUCHE

## 1. Objectif du bloc

Le Bloc 3 prend en charge la décision finale concernant le candidat à
recruter, la proposition d'embauche, la réponse du candidat et la
confirmation du recrutement. Une fois le recrutement confirmé, le
dossier est transmis au Bloc 4 --- Contrat et onboarding.


## 2. Périmètre fonctionnel

Le Bloc 3 doit permettre de :

-   récupérer les résultats et les dossiers issus du Bloc 2 ;
-   vérifier que le dossier du candidat est complet ;
-   enregistrer et faire valider la décision finale de sélection ;
-   préparer et envoyer une proposition d'embauche ;
-   enregistrer la réponse du candidat ;
-   gérer les refus, les absences de réponse et les candidats en attente
    ;
-   confirmer le recrutement après acceptation et validation des étapes
    requises ;
-   transmettre le dossier du candidat retenu au Bloc 4 ;
-   conserver un historique des décisions et des actions.

### Répartition indicative entre les blocs

-   **Bloc 1 --- Besoin :** expression et validation du besoin de
    recrutement.
-   **Bloc 2 --- Recrutement :** publication de l'offre, réception des
    candidatures, étude des dossiers, tests et entretiens, résultats et
    classement.
-   **Bloc 3 --- Sélection et embauche :** décision finale, proposition
    d'embauche, réponse du candidat, confirmation du recrutement et
    transfert du dossier.
-   **Bloc 4 --- Contrat et onboarding :** préparation du contrat,
    formalités administratives, affectation et intégration.


## 3. Acteurs concernés

  -----------------------------------------------------------------------
  Acteur                              Responsabilités
  ----------------------------------- -----------------------------------
  Responsable RH                      Vérifie le dossier, coordonne le
                                      processus et assure le suivi de la
                                      proposition d'embauche.

  Responsable du recrutement /        Donne son avis sur l'adéquation du
  responsable métier                  candidat au poste.

  Direction ou personne habilitée     Valide la décision finale lorsque
                                      la procédure l'exige.

  Candidat                            Accepte ou refuse la proposition
                                      d'embauche, ou demande des
                                      précisions.

  SIRH                                Enregistre les décisions, met à
                                      jour les statuts, déclenche les
                                      notifications et conserve
                                      l'historique.

  Responsable du Bloc 4               Reçoit le dossier du candidat
                                      retenu pour poursuivre le
                                      processus.
  -----------------------------------------------------------------------


## 4. Workflow --- déroulement du processus

1.  **Réception du dossier depuis le Bloc 2**
    -   Le SIRH récupère le candidat, sa candidature et les résultats
        disponibles.
    -   Le dossier doit rester lié à l'offre et au processus de
        recrutement concernés.
2.  **Vérification du dossier**
    -   Le RH vérifie que les informations et les résultats nécessaires
        sont présents.
    -   Si le dossier est incomplet ou incohérent, il est mis en attente
        de correction ou de vérification.
    -   Le processus ne continue pas tant que le problème bloquant n'est
        pas résolu.
3.  **Examen de la candidature et décision finale**
    -   Les personnes habilitées consultent les résultats des tests et
        entretiens ainsi que les informations pertinentes.
    -   Une décision est prise : retenu, non retenu ou en attente.
    -   La décision est enregistrée avec son auteur, sa date et sa
        justification lorsque celle-ci est requise.
4.  **Validation de la décision**
    -   Si une validation hiérarchique est obligatoire, la proposition
        d'embauche ne peut être envoyée avant cette validation.
    -   En cas de refus de validation, le dossier revient à l'étape de
        décision ou est clôturé selon le motif.
5.  **Préparation et envoi de la proposition d'embauche**
    -   Le RH prépare la proposition conformément au poste et aux
        conditions autorisées.
    -   Le SIRH enregistre la date d'envoi et le délai de réponse
        éventuel.
    -   Le candidat reçoit une notification.
6.  **Enregistrement de la réponse du candidat**
    -   **Acceptation :** poursuivre vers la confirmation du
        recrutement.
    -   **Refus :** enregistrer le refus et examiner, si nécessaire, le
        candidat suivant.
    -   **Absence de réponse :** envoyer les rappels prévus, puis faire
        examiner la situation par le RH.
    -   **Demande de précision ou de modification :** transmettre la
        demande au RH ; toute modification doit être validée avant
        l'envoi d'une nouvelle proposition.
7.  **Confirmation du recrutement**
    -   Après acceptation et réalisation des validations obligatoires,
        le RH confirme le recrutement dans le SIRH.
    -   Le statut du candidat et celui du processus sont mis à jour.
8.  **Transmission au Bloc 4**
    -   Le SIRH transmet le dossier du candidat retenu et les
        informations nécessaires au Bloc 4.
    -   Le transfert doit être tracé et son résultat confirmé.
    -   Le Bloc 4 prend ensuite en charge le contrat et l'onboarding.

### Diagramme du workflow

``` mermaid
flowchart TD
    A[Réception du dossier depuis le Bloc 2] --> B{Dossier complet et cohérent ?}
    B -- Non --> C[Demander une correction ou une vérification]
    C --> B
    B -- Oui --> D[Examen des résultats et décision finale]
    D --> E{Décision validée ?}
    E -- Non --> F[Compléter ou réexaminer la décision]
    F --> D
    E -- Oui --> G[Préparer et envoyer la proposition d'embauche]
    G --> H{Réponse du candidat ?}
    H -- Acceptation --> I{Étapes obligatoires terminées ?}
    I -- Non --> J[Compléter les validations requises]
    J --> I
    I -- Oui --> K[Confirmer le recrutement]
    K --> L[Transmettre le dossier au Bloc 4]
    H -- Refus --> M[Enregistrer le refus]
    M --> N{Un candidat suivant est disponible ?}
    N -- Oui --> D
    N -- Non --> O[Clôturer ou relancer le recrutement]
    H -- Aucune réponse --> P[Envoyer les rappels prévus]
    P --> Q[Décision du RH selon la procédure]
    Q --> H
    H -- Demande de modification --> R[Étudier et valider la demande]
    R --> G
```

## 5. Règles de gestion


  -----------------------------------------------------------------------
  Code                                Règle de gestion
  ----------------------------------- -----------------------------------
  RG-SE01                             Chaque décision du Bloc 3 doit être
                                      liée à une candidature et à une
                                      offre existantes dans le SIRH.

  RG-SE02                             Le Bloc 3 utilise les résultats
                                      transmis par le Bloc 2 ; il ne doit
                                      pas recréer ni modifier
                                      silencieusement les résultats des
                                      tests ou des entretiens.

  RG-SE03                             Un dossier incomplet ou incohérent
                                      est mis en attente tant que les
                                      éléments bloquants ne sont pas
                                      vérifiés.

  RG-SE04                             Les résultats requis doivent être
                                      disponibles avant la décision
                                      finale, sauf dérogation
                                      explicitement autorisée et tracée.

  RG-SE05                             Un classement automatique peut
                                      aider à l'analyse, mais ne remplace
                                      pas la décision humaine finale.

  RG-SE06                             Toute décision finale doit être
                                      enregistrée avec son statut, son
                                      auteur et sa date. Une
                                      justification est ajoutée lorsque
                                      la procédure l'exige.

  RG-SE07                             Seules les personnes habilitées
                                      peuvent valider la décision finale
                                      ou modifier son statut.

  RG-SE08                             La proposition d'embauche ne peut
                                      être envoyée qu'après les
                                      validations obligatoires.

  RG-SE09                             La proposition envoyée doit être
                                      enregistrée avec sa date d'envoi,
                                      sa version et, si applicable, son
                                      délai de réponse.

  RG-SE10                             L'absence de réponse du candidat ne
                                      vaut pas acceptation.

  RG-SE11                             Une réponse positive ou négative du
                                      candidat doit être enregistrée et
                                      datée.

  RG-SE12                             En cas de refus, le SIRH conserve
                                      l'historique et permet au RH
                                      d'examiner le candidat suivant si
                                      la procédure le prévoit.

  RG-SE13                             Une demande de modification des
                                      conditions doit être examinée et
                                      validée avant l'envoi d'une
                                      proposition révisée.

  RG-SE14                             Le recrutement ne peut être
                                      confirmé qu'après acceptation de la
                                      proposition et accomplissement des
                                      validations requises.

  RG-SE15                             Le nombre de recrutements confirmés
                                      ne doit pas dépasser le nombre de
                                      postes autorisés, sauf validation
                                      d'une modification du besoin.

  RG-SE16                             Si une liste d'attente est
                                      utilisée, l'ordre de traitement
                                      doit suivre les critères validés
                                      par l'organisation et être
                                      traçable.

  RG-SE17                             Si aucun candidat disponible n'est
                                      retenu, le RH doit pouvoir clôturer
                                      le processus ou demander sa relance
                                      selon la procédure.

  RG-SE18                             Le transfert au Bloc 4 ne peut être
                                      considéré comme terminé que si les
                                      données requises ont été transmises
                                      et que le résultat du transfert est
                                      enregistré.

  RG-SE19                             Les décisions, changements de
                                      statut, envois de proposition et
                                      transferts doivent être historisés
                                      pour permettre un suivi.

  RG-SE20                             L'accès aux dossiers candidats doit
                                      être limité aux personnes
                                      autorisées et les données doivent
                                      être traitées de manière
                                      confidentielle.
  -----------------------------------------------------------------------

## 6. Statuts proposés

### 6.1 Statut de sélection

-   `À vérifier`
-   `En cours d’examen`
-   `En attente de validation`
-   `Retenu`
-   `Non retenu`
-   `Liste d’attente`
-   `Clôturé`

### 6.2 Statut de la proposition d'embauche

-   `À préparer`
-   `En attente de validation`
-   `Envoyée`
-   `Acceptée`
-   `Refusée`
-   `En attente de réponse`
-   `À réviser`
-   `Expirée` --- uniquement si une date limite est prévue et validée
    dans la procédure.

### 6.3 Statut du recrutement

-   `Non confirmé`
-   `En cours de confirmation`
-   `Confirmé`
-   `Annulé`
-   `Transmis au Bloc 4`

Les noms et transitions de statuts sont à valider avec l'équipe afin
d'éviter les doublons avec le Bloc 2.

## 7. Notifications et événements

  -----------------------------------------------------------------------
  Événement               Destinataire            Action du SIRH
  ----------------------- ----------------------- -----------------------
  Dossier incomplet       RH ou responsable       Signaler les éléments
                          concerné                manquants et bloquer la
                                                  suite si nécessaire.

  Décision en attente de  Validateur habilité     Envoyer une demande de
  validation                                      validation.

  Proposition envoyée     Candidat et RH          Enregistrer l'envoi et
                                                  notifier les personnes
                                                  concernées.

  Délai de réponse proche Candidat et/ou RH       Envoyer un rappel
  ou dépassé                                      conformément à la
                                                  procédure.

  Acceptation du candidat RH                      Signaler que la
                                                  confirmation peut être
                                                  poursuivie.

  Refus du candidat       RH                      Enregistrer le refus et
                                                  signaler la possibilité
                                                  d'examiner un autre
                                                  candidat.

  Recrutement confirmé    RH et responsable du    Informer que le dossier
                          Bloc 4                  est prêt à être
                                                  transmis.

  Échec du transfert au   RH et support concerné  Signaler l'échec et
  Bloc 4                                          permettre une nouvelle
                                                  tentative contrôlée.
  -----------------------------------------------------------------------

Les canaux de notification et les délais exacts doivent être définis par
l'équipe.

## 8. Cas particuliers et exceptions

1.  **Dossier incomplet :** mettre le dossier en attente, indiquer les
    éléments manquants et notifier le responsable.
2.  **Résultats incohérents :** suspendre la décision et demander une
    vérification au responsable du Bloc 2.
3.  **Décision non validée :** empêcher l'envoi de la proposition tant
    que la validation obligatoire n'est pas obtenue.
4.  **Refus du candidat :** enregistrer la réponse et permettre l'examen
    d'un autre candidat si la procédure le prévoit.
5.  **Absence de réponse :** envoyer les rappels prévus ; le RH décide
    de la suite selon les règles établies.
6.  **Candidat en liste d'attente :** conserver son statut et son ordre
    de priorité conformément aux critères validés.
7.  **Aucun candidat approprié :** clôturer le recrutement ou demander
    une nouvelle campagne.
8.  **Recrutement annulé :** enregistrer le motif, l'auteur et la date,
    puis informer les acteurs concernés.
9.  **Erreur dans la décision :** corriger selon les droits d'accès et
    conserver la trace de la modification.
10. **Échec du transfert au Bloc 4 :** conserver le recrutement
    confirmé, signaler l'échec et permettre une reprise sans créer de
    doublon.

## 9. Données principales à gérer

  -----------------------------------------------------------------------
  Donnée / objet                      Informations principales
  ----------------------------------- -----------------------------------
  Candidat                            Identifiant, coordonnées et
                                      informations nécessaires au
                                      recrutement.

  Candidature                         Candidat, offre concernée, date et
                                      statut.

  Résultats du Bloc 2                 Résultats des tests et entretiens,
                                      classement et références aux
                                      évaluations.

  Décision de sélection               Décision, auteur, date, validation
                                      et justification si nécessaire.

  Proposition d'embauche              Version, date d'envoi, conditions
                                      autorisées, délai éventuel et
                                      statut.

  Réponse du candidat                 Type de réponse, date et éventuel
                                      commentaire.

  Recrutement                         Statut de confirmation, poste
                                      concerné et dates importantes.

  Liste d'attente                     Position ou ordre de priorité et
                                      statut du candidat.

  Transfert au Bloc 4                 Données transmises, date, résultat
                                      et éventuelles erreurs.

  Historique                          Actions effectuées, changements de
                                      statut, auteur et horodatage.
  -----------------------------------------------------------------------

Les données exactes à stocker doivent respecter le périmètre du projet
et les règles de confidentialité applicables.

## 10. Conditions de sortie du Bloc 3

Le Bloc 3 est terminé lorsque :

-   la décision finale a été enregistrée et validée ;
-   la proposition d'embauche a été acceptée ;
-   les validations obligatoires sont terminées ;
-   le recrutement est marqué comme confirmé ;
-   le dossier nécessaire a été transmis au Bloc 4 ;
-   le transfert et les principales actions sont traçables.

Si le candidat refuse ou si aucun candidat n'est retenu, le processus
doit être clôturé ou réorienté selon les règles validées.


