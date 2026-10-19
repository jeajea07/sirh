/* Espaces Technicien (niveau 2) et DG (niveau 3) : liste des candidats et grille de notes */

var NIVEAU = Number(document.body.getAttribute('data-level'));
var JURYS = { 2: [106, 107, 108, 112, 113], 3: [107, 108, 112, 113] };
var notesSaisies = [];
var grille = GRILLES[NIVEAU];

function pageListe() {
  var html = '';
  JURYS[NIVEAU].forEach(function (id) {
    var c = trouverCandidat(id);
    var fait = NOTES_DEMO[NIVEAU][id] !== undefined;
    var prec = 'Test ' + nombre(c.test) + '/20, E1 ' + nombre(c.notes[0]) + '/5' + (NIVEAU === 3 ? ', E2 ' + nombre(c.notes[1]) + '/5' : '');
    html += '<tr><td><div class="person"><div class="avatar">' + initiales(c.nom) + '</div>' + c.nom + '</div></td>' +
      '<td>' + trouverOffre(c.offre).titre + '</td><td class="muted">' + prec + '</td>' +
      '<td>' + (fait ? badge('Notes saisies', 'ok') : badge('À noter, lundi 19 octobre, 14:30', 'warn')) + '</td>' +
      '<td><div class="actions"><a class="btn btn-sm ' + (fait ? '' : 'btn-primary') + '" href="notes.html?id=' + id + '">' + (fait ? 'Revoir la grille' : 'Ouvrir la grille') + '</a></div></td></tr>';
  });
  $('#liste-body').innerHTML = html;
}

function pageGrille() {
  var ids = JURYS[NIVEAU];
  var id = Number(new URLSearchParams(window.location.search).get('id')) || ids[0];
  var candidat = trouverCandidat(id);

  var options = '';
  ids.forEach(function (i) { options += '<option value="' + i + '"' + (i === id ? ' selected' : '') + '>' + trouverCandidat(i).nom + '</option>'; });
  $('#choix-candidat').innerHTML = options;
  $('#choix-candidat').addEventListener('change', function () { window.location.search = '?id=' + this.value; });

  var existantes = NOTES_DEMO[NIVEAU][id];
  notesSaisies = [];
  for (var i = 0; i < grille.questions.length; i++) { notesSaisies.push(existantes ? existantes[i] : null); }

  var html = '';
  grille.questions.forEach(function (q, i) {
    var boutons = '';
    for (var n = 1; n <= 5; n++) {
      boutons += '<button type="button" data-q="' + i + '" data-n="' + n + '" aria-label="Note ' + n + ' sur 5"' + (notesSaisies[i] === n ? ' class="on"' : '') + '>' + n + '</button>';
    }
    html += '<div class="question"><div><p>' + (i + 1) + '. ' + q.texte + '</p>' +
      '<span class="chip">Importance ' + q.imp + '</span> ' + (q.elim ? badge('Éliminatoire, minimum ' + q.min + '/5', 'warn') : '') + '</div>' +
      '<div class="rating">' + boutons + '</div></div>';
  });
  $('#questions').innerHTML = html;

  $$('.rating button').forEach(function (b) {
    b.addEventListener('click', function () {
      var q = Number(b.getAttribute('data-q'));
      notesSaisies[q] = Number(b.getAttribute('data-n'));
      $$('button[data-q="' + q + '"]').forEach(function (x) { x.classList.toggle('on', x === b); });
      afficherResultat();
    });
  });

  $('#nom-candidat').textContent = candidat.nom;
  var prec = '<div class="crit"><span>Test technique</span><b>' + nombre(candidat.test) + ' / 20</b></div>' +
    '<div class="crit"><span>Entretien 1 (RH)</span><b>' + nombre(candidat.notes[0], 2) + ' / 5</b></div>';
  if (NIVEAU === 3) { prec += '<div class="crit"><span>Entretien 2 (technicien)</span><b>' + nombre(candidat.notes[1], 2) + ' / 5</b></div>'; }
  $('#precedents').innerHTML = prec;

  $('#enregistrer').addEventListener('click', function () {
    var r = calculerResultat(notesSaisies, grille);
    if (!r.complet) { toast('Notez toutes les questions avant d’enregistrer.'); return; }
    toast('Notes enregistrées. Le RH reçoit les résultats et les compare aux seuils.');
  });
  afficherResultat();
}

function afficherResultat() {
  var r = calculerResultat(notesSaisies, grille);
  $('#moyenne').textContent = nombre(r.moyenne, 2);
  $('#barre-moyenne').style.width = (r.moyenne ? r.moyenne / 5 * 100 : 0) + '%';
  $('#seuil').textContent = 'Seuil général : ' + nombre(grille.seuil) + ' / 5';

  var elim = '';
  grille.questions.forEach(function (q, i) {
    if (!q.elim) { return; }
    var n = notesSaisies[i];
    var etat = n === null ? badge('À noter', '') : (n >= q.min ? badge('Respecté', 'ok') : badge('Échec', 'bad'));
    elim += '<div class="crit"><span>Question ' + (i + 1) + ' (min. ' + q.min + ')</span>' + etat + '</div>';
  });
  $('#eliminatoires').innerHTML = elim;

  var verdict = badge('Notes incomplètes', 'info');
  if (r.complet) { verdict = r.propose ? badge('Réussite proposée', 'ok') : badge('Non admis proposé', 'bad'); }
  $('#verdict').innerHTML = verdict;
}

document.addEventListener('DOMContentLoaded', function () {
  var page = document.body.getAttribute('data-page');
  if (page === 'candidats') { pageListe(); }
  if (page === 'notes') { pageGrille(); }
});
