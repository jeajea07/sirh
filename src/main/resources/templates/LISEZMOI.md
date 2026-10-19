# TeamUp — template de l'application RH (bloc 2 : recrutement)

Ouvrir `index.html` dans un navigateur : c'est l'accueil du template, avec un lien vers chaque espace.
Les données sont fictives (`assets/js/data.js`). Aucun serveur n'est nécessaire.

## Structure

- `index.html`, `connexion.html` : accueil du template et page de connexion
- `site/` : site vitrine public (offres, détail, formulaire de candidature)
- `candidat/` : suivi, test technique, choix du créneau
- `rh/` : tableau de bord, offres, candidatures, test, entretiens, confirmation finale, historique
- `technicien/`, `dg/` : liste des candidats et grille de notes (niveaux 2 et 3)
- `direction/` : correction des réponses libres
- `admin/` : utilisateurs et paramètres
- `assets/css/` : `base.css` (palette, textes), `components.css` (boutons, tableaux, formulaires...), `layout.css` (barre latérale, site vitrine)
- `assets/js/` : `data.js` (données), `ui.js` (composants), `shell.js` (menu par espace), puis un fichier par espace

## Palette

Les couleurs sont des variables CSS dans `assets/css/base.css` (`--navy-800`, `--orange-500`, `--steel-300`...).
Chaque espace a sa couleur de repère (`--role`) : RH orange, technicien turquoise, DG violet, direction bleu, administration rose, candidat gris-bleu.

## Ajouter une page à un espace

1. Copier une page existante du même dossier et changer `data-page` et `data-title` sur `<body>`.
2. Ajouter le lien dans le menu de l'espace, dans `ESPACES` de `assets/js/shell.js`.
