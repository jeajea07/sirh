/* Espace RH : chaque page a sa fonction, appelée selon data-page */

function badgeOffre(statut) {
  if (statut === 'Publiée') { return badge('Publiée', 'ok'); }
  if (statut === 'Validée') { return badge('Validée, à publier', 'info'); }
  return badge('Brouillon', '');
}

/* ================= Offres ================= */
function pageOffres() {
  var corps = $('#offres-body');
  var html = '';
  OFFRES.forEach(function (o) {
    var action = '';
    if (o.statut === 'Brouillon') { action = '<button class="btn btn-sm" data-open="offre-modal">Compléter</button>'; }
    if (o.statut === 'Validée') { action = '<button class="btn btn-sm btn-primary" data-toast="Offre publiée sur le site vitrine. La date de publication est enregistrée.">Publier</button>'; }
    if (o.statut === 'Publiée') { action = '<a class="btn btn-sm" href="candidatures.html">Voir les candidatures</a>'; }
    html += '<tr><td><b>' + o.titre + '</b><br><span class="muted small">' + o.service + '</span></td>' +
      '<td>' + (o.postes || '—') + '</td><td>' + (o.limite || '—') + '</td><td>' + o.nb + '</td>' +
      '<td>' + badgeOffre(o.statut) + '</td><td><div class="actions">' + action + '</div></td></tr>';
  });
  corps.innerHTML = html;
}

/* ================= Candidatures ================= */
var filtreCandidatures = 'tous';
var candidatOuvert = null;

function libelleCritere(etat) {
  if (etat === 'ok') { return badge('Rempli', 'ok'); }
  if (etat === 'ko') { return badge('Non rempli', 'bad'); }
  return badge('Douteux ou incomplet', 'warn');
}

function pageCandidatures() {
  afficherCandidatures();
  afficherReserve();

  $$('[data-filtre]').forEach(function (bouton) {
    bouton.addEventListener('click', function () {
      $$('[data-filtre]').forEach(function (b) { b.classList.remove('active'); });
      bouton.classList.add('active');
      filtreCandidatures = bouton.getAttribute('data-filtre');
      var reserve = filtreCandidatures === 'reserve';
      $('#liste-candidatures').classList.toggle('hidden', reserve);
      $('#liste-reserve').classList.toggle('hidden', !reserve);
      afficherCandidatures();
    });
  });

  $('#valider-dossier').addEventListener('click', validerDossier);
}

function afficherCandidatures() {
  var corps = $('#candidatures-body');
  var html = '';
  var compte = { tous: 0, conforme: 0, regarder: 0, non: 0 };
  CANDIDATS.forEach(function (c) {
    if (c.statut !== 'À étudier' && !(c.valide && c.statut === 'Dossier validé')) { return; }
    compte.tous++; compte[c.classement]++;
    if (filtreCandidatures !== 'tous' && filtreCandidatures !== 'reserve' && c.classement !== filtreCandidatures) { return; }
    var retard = c.jours > 5;
    html += '<tr><td><div class="person"><div class="avatar">' + initiales(c.nom) + '</div>' + c.nom + '</div></td>' +
      '<td>' + trouverOffre(c.offre).titre + '</td><td>' + c.recu + '</td>' +
      '<td class="' + (retard ? 'late' : '') + '">' + c.jours + ' j' + (retard ? ' · en retard' : '') + '</td>' +
      '<td>' + badgeClassement(c.classement) + '</td>' +
      '<td>' + (c.valide ? badge('Validé', 'ok') : badge('À valider', '')) + '</td>' +
      '<td><div class="actions"><button class="btn btn-sm" data-dossier="' + c.id + '">Examiner</button></div></td></tr>';
  });
  corps.innerHTML = html || '<tr><td colspan="7" class="empty">Aucune candidature dans cette catégorie.</td></tr>';
  $('#n-tous').textContent = compte.tous;
  $('#n-conforme').textContent = compte.conforme;
  $('#n-regarder').textContent = compte.regarder;
  $('#n-non').textContent = compte.non;

  $$('[data-dossier]').forEach(function (b) {
    b.addEventListener('click', function () { ouvrirDossier(b.getAttribute('data-dossier')); });
  });
}

function ouvrirDossier(id) {
  var c = trouverCandidat(id);
  var o = trouverOffre(c.offre);
  candidatOuvert = c;
  $('#dossier-titre').textContent = c.nom;
  $('#dossier-corps').innerHTML =
    '<dl class="kv"><dt>Offre</dt><dd>' + o.titre + '</dd><dt>Diplôme</dt><dd>' + c.diplome + '</dd>' +
    '<dt>Expérience</dt><dd>' + c.exp + '</dd><dt>Reçu le</dt><dd>' + c.recu + '</dd><dt>CV</dt><dd><a href="#" style="text-decoration:underline">cv-' + c.nom.split(' ')[0].toLowerCase() + '.pdf</a></dd></dl>' +
    '<h3 style="margin:18px 0 6px">Critères obligatoires de l’offre</h3>' +
    '<div class="crit"><span>' + o.obligatoires[0] + '</span>' + libelleCritere(c.crit.diplome) + '</div>' +
    '<div class="crit"><span>' + o.obligatoires[1] + '</span>' + libelleCritere(c.crit.exp) + '</div>' +
    '<div class="crit"><span>' + o.obligatoires[2] + '</span>' + libelleCritere(c.crit.skill) + '</div>' +
    '<h3 style="margin:18px 0 6px">Critères souhaités</h3><p class="muted small">' + (o.souhaites.join(' · ') || 'Aucun') + ' (affichés pour vous aider, ils ne changent pas le classement)</p>' +
    '<div class="field" style="margin-top:18px"><label for="classement-choix">Classement (automatique : ' + (c.classement === 'conforme' ? 'conforme' : c.classement === 'non' ? 'non conforme' : 'à regarder') + ')</label>' +
    '<select id="classement-choix"><option value="conforme">Conforme</option><option value="regarder">À regarder</option><option value="non">Non conforme</option></select></div>';
  $('#classement-choix').value = c.classement;
  $('#valider-dossier').disabled = c.valide;
  openModal('dossier');
}

function validerDossier() {
  var choix = $('#classement-choix').value;
  if (choix === 'regarder') { toast('Choisissez « Conforme » ou « Non conforme » avant de valider.'); return; }
  candidatOuvert.classement = choix;
  candidatOuvert.valide = true;
  if (choix === 'conforme') {
    candidatOuvert.statut = 'Dossier validé';
    toast('Dossier validé. Convocation et accès au test envoyés à ' + candidatOuvert.nom + '.');
  } else {
    toast('Dossier validé. Message de réponse envoyé à ' + candidatOuvert.nom + '. Le dossier est conservé.');
  }
  closeModals();
  afficherCandidatures();
}

function afficherReserve() {
  var html = '';
  SPONTANEES.forEach(function (s) {
    html += '<tr><td><div class="person"><div class="avatar">' + initiales(s.nom) + '</div>' + s.nom + '</div></td><td>' + s.domaine + '</td><td>' + s.recu + '</td>' +
      '<td><div class="actions"><select style="width:auto" aria-label="Offre">' +
      OFFRES.filter(function (o) { return o.statut === 'Publiée'; }).map(function (o) { return '<option>' + o.titre + '</option>'; }).join('') +
      '</select><button class="btn btn-sm btn-primary" data-toast="Candidature rattachée à l’offre. Elle suit le parcours à partir de l’étude du dossier.">Rattacher</button></div></td></tr>';
  });
  $('#reserve-body').innerHTML = html;
}

/* ================= Test technique ================= */
function totalTest(t) { return (t.auto || 0) + (t.libre || 0); }

function pageTest() {
  afficherTest();
  $('#simuler').addEventListener('click', function () {
    TESTS.forEach(function (t) { if (t.statut === 'En correction') { t.libre = 4; t.statut = 'Corrigé'; } });
    toast('Correction terminée : les résultats complets sont envoyés au RH.');
    afficherTest();
  });
  $('#confirmer-admis').addEventListener('click', function () {
    var admis = TESTS.filter(function (t) { return t.statut === 'Corrigé' && totalTest(t) >= SEUIL_TEST; }).length;
    toast(admis + ' admis confirmés : e-mail avec le lien pour choisir le créneau de l’entretien 1. Les autres reçoivent une réponse.');
  });
}

function afficherTest() {
  var html = '', attente = 0, admis = 0;
  TESTS.forEach(function (t) {
    var etat, total = '—';
    if (t.statut === 'Absent') { etat = badge('Absent, non retenu', 'bad'); }
    else if (t.statut === 'En correction') { etat = badge('En correction par la direction', 'warn'); attente++; total = nombre(t.auto) + ' + ?'; }
    else if (totalTest(t) >= SEUIL_TEST) { etat = badge('Admis', 'ok'); admis++; total = nombre(totalTest(t)); }
    else { etat = badge('Non admis', 'bad'); total = nombre(totalTest(t)); }
    var action = t.statut === 'Absent' ? '<button class="btn btn-sm" data-open="exception">Accorder une exception</button>' : '';
    if (t.statut === 'En correction') { action = '<button class="btn btn-sm" data-toast="Rappel envoyé à la direction responsable.">Relancer la correction</button>'; }
    html += '<tr><td><div class="person"><div class="avatar">' + initiales(t.nom) + '</div>' + t.nom + '</div></td>' +
      '<td>' + nombre(t.auto) + ' / 14</td><td>' + nombre(t.libre) + ' / 6</td><td><b>' + total + '</b> / 20</td><td>' + etat + '</td>' +
      '<td><div class="actions">' + action + '</div></td></tr>';
  });
  $('#test-body').innerHTML = html;
  $('#confirmer-admis').disabled = attente > 0;
  $('#alerte-test').className = attente > 0 ? 'alert alert-warn' : 'alert alert-ok';
  $('#alerte-test').textContent = attente > 0
    ? attente + ' copie en attente de correction. La confirmation sera possible quand tout sera corrigé.'
    : 'Tout est corrigé : ' + admis + ' candidats atteignent le seuil. Vous pouvez confirmer les admis.';
}

/* ================= Entretiens ================= */
var decisionEnCours = '';

function pageEntretiens() {
  for (var n = 1; n <= 3; n++) { afficherNiveau(n); }

  document.addEventListener('click', function (e) {
    var b = e.target.closest('[data-decider]');
    if (b) { decisionEnCours = b.getAttribute('data-decider'); $('#decision-nom').textContent = decisionEnCours; openModal('decision'); }
  });
  $('#ajout-question').addEventListener('click', function () {
    var ligne = document.createElement('div');
    ligne.className = 'form-grid span2';
    ligne.style.cssText = 'grid-template-columns:1fr 110px 110px 90px;margin-top:8px';
    ligne.innerHTML = '<input type="text" placeholder="Question" aria-label="Question"><select aria-label="Importance"><option>1 normale</option><option>2 importante</option><option>3 très importante</option></select><select aria-label="Éliminatoire"><option>Non</option><option>Oui</option></select><input type="number" min="1" max="5" placeholder="Min" aria-label="Note minimale">';
    $('#questions-grille').appendChild(ligne);
  });
}

function nomImportance(i) { return i === 3 ? 'Très importante' : i === 2 ? 'Importante' : 'Normale'; }

function afficherNiveau(n) {
  var g = GRILLES[n];
  var lignes = '';
  g.questions.forEach(function (q, i) {
    lignes += '<tr><td>' + (i + 1) + '</td><td>' + q.texte + '</td><td>' + nomImportance(q.imp) + '</td><td>' + (q.elim ? badge('Minimum ' + q.min + '/5', 'warn') : '—') + '</td></tr>';
  });
  var resultats = '';
  for (var id in NOTES_DEMO[n]) {
    var c = trouverCandidat(id);
    var r = calculerResultat(NOTES_DEMO[n][id], g);
    var elim = r.echecs.length ? badge('Échec question ' + r.echecs.join(', '), 'bad') : badge('Respectées', 'ok');
    var prop = r.propose ? badge('Réussite proposée', 'ok') : badge('Non admis proposé', 'bad');
    var action = r.propose
      ? '<button class="btn btn-sm btn-primary" data-toast="Passage confirmé pour ' + c.nom + '. E-mail envoyé au candidat.">Confirmer le passage</button>'
      : '<button class="btn btn-sm" data-decider="' + c.nom + '">Décider</button>';
    resultats += '<tr><td><div class="person"><div class="avatar">' + initiales(c.nom) + '</div>' + c.nom + '</div></td>' +
      '<td><b>' + nombre(r.moyenne, 2) + '</b> / 5</td><td>' + elim + '</td><td>' + prop + '</td><td><div class="actions">' + action + '</div></td></tr>';
  }
  $('#niv' + n).innerHTML =
    '<div class="alert alert-info" style="margin-bottom:14px">Grille figée depuis le ' + g.debut + '. Les questions ne peuvent plus être modifiées. Seuil général : ' + nombre(g.seuil) + ' / 5.</div>' +
    '<div class="card card-flat" style="margin-bottom:16px"><div class="card-head"><h3>Grille d’évaluation — ' + g.titre + '</h3></div><div class="table-wrap"><table><thead><tr><th>N°</th><th>Question</th><th>Importance</th><th>Éliminatoire</th></tr></thead><tbody>' + lignes + '</tbody></table></div></div>' +
    '<div class="card card-flat"><div class="card-head"><h3>Résultats du jury</h3></div><div class="table-wrap"><table><thead><tr><th>Candidat</th><th>Note générale</th><th>Questions éliminatoires</th><th>Proposition</th><th></th></tr></thead><tbody>' + resultats + '</tbody></table></div></div>';
}

/* ================= Confirmation finale ================= */
var classementFinal = [];
var confirmes = {};
var refuses = {};
var AVIS_DG = { 107: 'Favorable', 108: 'Favorable', 112: 'Favorable', 113: 'Réservé' };

function pageFinal() {
  classementFinal = CANDIDATS.filter(function (c) { return c.statut === 'Confirmation finale'; });
  classementFinal.sort(function (a, b) { return noteFinale(b) - noteFinale(a); });
  afficherFinal();
}

function afficherFinal() {
  var postes = 2, actifs = 0, pourvus = 0, html = '';
  classementFinal.forEach(function (c) {
    var etat, action = '';
    if (refuses[c.id]) {
      etat = badge('A refusé', 'bad');
    } else {
      actifs++;
      if (actifs <= postes) {
        if (confirmes[c.id]) {
          etat = badge('Recruté', 'ok'); pourvus++;
          action = '<button class="btn btn-sm btn-danger" data-refus="' + c.id + '">A refusé</button>';
        } else {
          etat = badge('À recruter', 'info');
          action = '<button class="btn btn-sm btn-primary" data-confirmer="' + c.id + '">Confirmer le recrutement</button>';
        }
      } else {
        etat = badge('Liste d’attente n° ' + (actifs - postes), 'warn');
      }
    }
    html += '<tr><td><b>' + (refuses[c.id] ? '—' : actifs) + '</b></td><td><div class="person"><div class="avatar">' + initiales(c.nom) + '</div>' + c.nom + '</div></td>' +
      '<td>' + nombre(c.test) + ' / 20</td><td>' + nombre(c.notes[0]) + '</td><td>' + nombre(c.notes[1]) + '</td><td>' + nombre(c.notes[2]) + '</td>' +
      '<td><b>' + nombre(noteFinale(c), 2) + '</b> / 5</td><td>' + AVIS_DG[c.id] + '</td><td>' + etat + '</td><td><div class="actions">' + action + '</div></td></tr>';
  });
  $('#final-body').innerHTML = html;
  $('#postes-pourvus').textContent = pourvus + ' / ' + postes;
  $('#postes-barre').style.width = (pourvus / postes * 100) + '%';
  $('#alerte-final').className = pourvus === postes ? 'alert alert-ok' : 'hidden';
  $('#alerte-final').textContent = 'Tous les postes sont pourvus : l’offre est terminée et les candidats encore en cours sont prévenus. L’historique est conservé.';

  $$('[data-confirmer]').forEach(function (b) {
    b.addEventListener('click', function () {
      confirmes[b.getAttribute('data-confirmer')] = true;
      toast('Recrutement confirmé. E-mail envoyé au candidat et dossier transmis au bloc 3.');
      afficherFinal();
    });
  });
  $$('[data-refus]').forEach(function (b) {
    b.addEventListener('click', function () {
      refuses[b.getAttribute('data-refus')] = true;
      toast('Refus enregistré. Le candidat suivant de la liste d’attente vous est proposé.');
      afficherFinal();
    });
  });
}

/* ================= Historique ================= */
var HISTORIQUE = [
  ['17/10/2026 09:12', 'Dossier de Rakoto Andry classé « conforme » (automatique)', 'Application', 'dossier'],
  ['16/10/2026 16:40', 'Passage confirmé en entretien 3 : Ramiandrisoa Lova', 'Hanta Ravelo', 'entretien'],
  ['16/10/2026 11:05', 'Notes de l’entretien 3 saisies : Razafy Toky', 'Lucie Rakotondrabe', 'entretien'],
  ['15/10/2026 15:30', 'Offre « Assistant(e) RH » validée', 'Hanta Ravelo', 'offre'],
  ['14/10/2026 10:02', 'Admis du test technique confirmés (8 candidats)', 'Hanta Ravelo', 'test'],
  ['13/10/2026 17:20', 'Réponses libres corrigées : 7 copies', 'Faly Randrianasolo', 'test'],
  ['12/10/2026 09:00', 'Exception accordée : Andrianasolo Faly (motif : panne d’électricité)', 'Hanta Ravelo', 'test'],
  ['08/10/2026 14:15', 'Dossier de Rasolofo Nirina validé, convocation envoyée', 'Hanta Ravelo', 'dossier'],
  ['01/10/2026 08:00', 'Offre « Développeur web » publiée sur le site vitrine', 'Hanta Ravelo', 'offre']
];

function pageHistorique() {
  var liste = $('#historique-liste');
  var filtre = $('#filtre-historique');
  function afficher() {
    var html = '';
    HISTORIQUE.forEach(function (h) {
      if (filtre.value !== 'tous' && h[3] !== filtre.value) { return; }
      html += '<li><time>' + h[0] + '</time><p><b>' + h[1] + '</b></p><p class="muted small">Par ' + h[2] + '</p></li>';
    });
    liste.innerHTML = html || '<li class="muted">Aucune décision pour ce filtre.</li>';
  }
  filtre.addEventListener('change', afficher);
  afficher();
}

document.addEventListener('DOMContentLoaded', function () {
  var page = document.body.getAttribute('data-page');
  if (page === 'offres') { pageOffres(); }
  if (page === 'candidatures') { pageCandidatures(); }
  if (page === 'test') { pageTest(); }
  if (page === 'entretiens') { pageEntretiens(); }
  if (page === 'final') { pageFinal(); }
  if (page === 'historique') { pageHistorique(); }
});
