/* Site vitrine : liste des offres, détail d'une offre, formulaire de candidature */

function parametre(nom) { return new URLSearchParams(window.location.search).get(nom); }

/* ----- Liste des offres ----- */
function afficherOffresSite() {
  var mot = $('#recherche').value.toLowerCase();
  var contrat = $('#contrat').value;
  var html = '';
  OFFRES.forEach(function (o) {
    if (o.statut !== 'Publiée') { return; }
    if (contrat !== 'tous' && o.contrat.indexOf(contrat) !== 0) { return; }
    if (mot && (o.titre + ' ' + o.service + ' ' + o.lieu).toLowerCase().indexOf(mot) === -1) { return; }
    html += '<article class="offer"><div class="grow"><h3>' + o.titre + '</h3><p class="muted small">' + o.service + '</p>' +
      '<div class="meta"><span class="chip">' + o.lieu + '</span><span class="chip">' + o.contrat + '</span><span class="chip">' + o.postes + (o.postes > 1 ? ' postes' : ' poste') + '</span><span class="chip">Jusqu’au ' + o.limite + '</span></div></div>' +
      '<a class="btn btn-primary" href="offre.html?id=' + o.id + '">Voir l’offre</a></article>';
  });
  $('#offres-liste').innerHTML = html || '<div class="empty card">Aucune offre ne correspond à votre recherche. Essayez un autre mot ou postulez en candidature spontanée.</div>';
}

function pageSiteOffres() {
  afficherOffresSite();
  $('#recherche').addEventListener('input', afficherOffresSite);
  $('#contrat').addEventListener('change', afficherOffresSite);
}

/* ----- Détail d'une offre ----- */
function listeHtml(tableau) {
  return '<ul style="margin:8px 0 0;padding-left:18px">' + tableau.map(function (t) { return '<li>' + t + '</li>'; }).join('') + '</ul>';
}

function pageSiteOffre() {
  var o = trouverOffre(parametre('id'));
  var zone = $('#detail');
  if (!o || o.statut !== 'Publiée') {
    zone.innerHTML = '<div class="card empty">Cette offre n’est plus disponible. <a href="offres.html" style="text-decoration:underline">Voir les offres ouvertes</a></div>';
    return;
  }
  zone.innerHTML =
    '<div class="grid g-main"><div class="card stack">' +
      '<div><h1>' + o.titre + '</h1><p class="muted" style="margin-top:4px">' + o.service + '</p></div>' +
      '<div class="row wrap"><span class="chip">' + o.lieu + '</span><span class="chip">' + o.contrat + '</span><span class="chip">' + o.postes + (o.postes > 1 ? ' postes' : ' poste') + '</span></div>' +
      '<p>' + o.resume + '</p>' +
      '<div><h3>Vos missions</h3>' + listeHtml(o.missions) + '</div>' +
      '<div><h3>Profil recherché</h3>' + listeHtml(o.obligatoires) + '</div>' +
      (o.souhaites.length ? '<div><h3>Un plus</h3>' + listeHtml(o.souhaites) + '</div>' : '') +
    '</div>' +
    '<aside class="card stack sticky"><div><b>Date limite</b><p class="muted">' + o.limite + '</p></div>' +
      '<a class="btn btn-primary btn-block" href="postuler.html?id=' + o.id + '">Postuler</a>' +
      '<p class="muted small">Les conditions du poste (salaire, contrat) ne sont pas négociables dans le formulaire.</p></aside></div>';
  document.title = o.titre + ' — TeamUp';
}

/* ----- Formulaire de candidature ----- */
var MODELES = {
  etudes: '<input type="text" placeholder="Diplôme" aria-label="Diplôme"><input type="text" placeholder="Établissement" aria-label="Établissement"><input type="number" placeholder="Année" aria-label="Année">',
  experiences: '<input type="text" placeholder="Poste occupé" aria-label="Poste occupé"><input type="text" placeholder="Entreprise" aria-label="Entreprise"><input type="text" placeholder="Durée (ex. 2 ans)" aria-label="Durée">',
  langues: '<input type="text" placeholder="Langue" aria-label="Langue"><select aria-label="Niveau"><option>Débutant</option><option>Intermédiaire</option><option>Courant</option><option>Langue maternelle</option></select>'
};

function ajouterLigne(type) {
  var ligne = document.createElement('div');
  ligne.className = 'ligne ligne-' + type;
  ligne.innerHTML = MODELES[type];
  $('#lignes-' + type).appendChild(ligne);
}

function pagePostuler() {
  var o = trouverOffre(parametre('id'));
  var spontanee = parametre('spontanee') === '1';

  if (spontanee) {
    $('#titre-candidature').textContent = 'Candidature spontanée';
    $('#sous-titre').textContent = 'Votre dossier est gardé dans notre réserve. Le RH le rattache à une offre dès qu’un poste correspond.';
    $('#bloc-domaine').classList.remove('hidden');
  } else if (o && o.statut === 'Publiée') {
    $('#titre-candidature').textContent = 'Postuler : ' + o.titre;
    $('#sous-titre').textContent = o.service + ', ' + o.lieu + ' — ' + o.contrat;
  } else {
    $('#formulaire').classList.add('hidden');
    $('#ferme').classList.remove('hidden');
    return;
  }

  ['etudes', 'experiences', 'langues'].forEach(function (type) { ajouterLigne(type); });
  $$('[data-add]').forEach(function (b) {
    b.addEventListener('click', function () { ajouterLigne(b.getAttribute('data-add')); });
  });
}

function candidatureEnvoyee(form) {
  var mail = $('#mail').value.trim().toLowerCase();
  if (mail === 'deja@exemple.mg') {
    var alerte = $('.form-errors', form);
    alerte.className = 'form-errors alert alert-bad';
    alerte.textContent = 'Vous avez déjà postulé à cette offre avec cette adresse. Une seule candidature par personne et par offre est possible.';
    return;
  }
  $('#formulaire').classList.add('hidden');
  $('#confirmation').classList.remove('hidden');
  $('#numero').textContent = 'C-2026-0' + (Math.floor(Math.random() * 800) + 200);
  $('#date-reception').textContent = new Date().toLocaleDateString('fr-FR');
  $('#mail-confirmation').textContent = $('#mail').value;
  window.scrollTo(0, 0);
}

document.addEventListener('DOMContentLoaded', function () {
  var page = document.body.getAttribute('data-page');
  if (page === 'offres') { pageSiteOffres(); }
  if (page === 'offre') { pageSiteOffre(); }
  if (page === 'postuler') { pagePostuler(); }
});
