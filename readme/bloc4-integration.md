# BLOC 4 — INTÉGRATION AVEC LE BLOC 3

## 1. Principe

Le Bloc 3 confirme le recrutement, puis transmet le dossier au Bloc 4. La
frontière est matérialisée par un seul DTO scalaire :

`com.gestionrh.sirh.service.dto.EmbaucheDTO`

Le Bloc 4 ne connaît ni `SelectionModel`, ni `PropositionModel`, ni
`CandidatModel`. L'adaptateur `RecrutementAdapter` vit côté Bloc 3 et porte
toute la logique de transformation.

## 2. Contrat d'interface

| Champ | Obligatoire | Source Bloc 3 |
|---|---|---|
| `idRecrutement` | oui | `b3_recrutement` |
| `idSelection` | oui | `b3_selection` |
| `idCandidatureB2` | oui | `b3_selection` |
| `idOffreB2` | oui | `b3_selection` |
| `idCandidatB2` | oui | `b3_selection` |
| `nom`, `prenom`, `email` | oui | `candidat` |
| `telephone` | non | `candidat` |
| `codeTypeContrat` | oui | proposition (`CDI`, `CDD`, `STAGE`, `AUTRE`) |
| `salaire` | oui | proposition, strictement positif |
| `dateDebut` | oui | proposition |
| `dureeMois` | non | proposition ; utilisée pour calculer la fin CDD |
| `fonction` | oui | intitulé du poste proposé |
| `nombrePostes` | oui | recrutement |
| `dateConfirmation` | non | recrutement |
| `idPoste`, `idDepartement` | oui pour la création automatique | sélection RH |
| `cin`, `adresse`, `dateNaissance` | non | complément RH |
| `categorieProfessionnelle`, `motifRecours` | non | complément RH |
| `idDocumentCv` | non | Bloc 2 |

`AUTRE` est refusé par le Bloc 4 : la RH doit choisir un type de contrat
reconnu avant de transférer.

## 3. Enchaînement

1. La RH confirme le recrutement.
2. La proposition acceptée la plus récente est sélectionnée.
3. La réponse du candidat doit être `ACCEPTEE`.
4. Le candidat est chargé depuis le Bloc 2.
5. `RecrutementAdapter` construit `EmbaucheDTO`.
6. `ContratService.creerDepuisEmbauche` :
   - retourne le contrat existant si `id_recrutement_externe` existe déjà ;
   - valide les champs indispensables ;
   - crée ou réutilise le salarié ;
   - crée le contrat en `BROUILLON` ;
   - crée l'affectation active ;
   - génère l'onboarding si un modèle s'applique ;
   - trace l'événement dans l'audit.
7. L'adaptateur appelle `SelectionService.enregistrerTransfert` avec la
   référence du contrat.
8. En cas de `Bloc4Exception`, l'échec est enregistré dans
   `b3_recrutement.erreur_transfert` et le statut de transfert passe à `ECHEC`.

## 4. Idempotence

- `contrat.id_recrutement_externe` est unique ;
- un second appel retourne le contrat existant sans recréer le salarié,
  l'affectation ni l'onboarding ;
- si `b3_recrutement.etat_transfert = TRANSMIS`, l'adaptateur ne rappelle
  même pas le Bloc 4 et renvoie la référence déjà enregistrée.

## 5. Interface utilisateur

Le dossier de sélection expose deux chemins :

- **Transfert automatique** : la RH choisit le poste et le département, puis
  clique sur « Transférer et créer le contrat ». Le formulaire appelle
  `RecrutementAdapter` et crée le dossier Bloc 4.
- **Transfert manuel (dépannage)** : la RH peut enregistrer une référence
  externe ou un échec, sans création automatique.

Le point d'entrée manuel du Bloc 4 reste disponible :

`GET /rh/contrats/nouveau?idEmploye=...`

Il permet de créer un contrat directement si le transfert automatique ne peut
pas aboutir.

## 6. Vérification

```bash
./mvnw -q test -Dtest=RecrutementAdapterTest
```

Le test couvre :

- la construction du DTO depuis les entités Bloc 3 ;
- la transmission de la référence du contrat au recrutement ;
- l'absence de second appel quand le transfert est déjà `TRANSMIS` ;
- l'enregistrement de l'échec et de son motif quand le Bloc 4 refuse.
