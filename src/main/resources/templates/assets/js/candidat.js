/* Espace candidat : passage du test et choix du créneau d'entretien */

var QUESTIONS_TEST = [
  { type: 'qcm', texte: 'Quelle balise HTML définit le titre principal d’une page ?', options: ['<head>', '<h1>', '<title>', '<header>'] },
  { type: 'qcm', texte: 'En JavaScript, quel mot-clé déclare une variable dont la valeur peut changer ?', options: ['const', 'let', 'fixed', 'static'] },
  { type: 'qcm', texte: 'Quelle requête SQL récupère toutes les lignes de la table « candidat » ?', options: ['GET * FROM candidat', 'SELECT * FROM candidat', 'READ candidat', 'OPEN candidat'] },
  { type: 'qcm', texte: 'Quel code HTTP signifie « ressource introuvable » ?', options: ['200', '301', '404', '500'] },
  { type: 'qcm', texte: 'Que fait la commande git commit ?', options: ['Envoie le code sur le serveur', 'Enregistre les changements dans l’historique local', 'Supprime une branche', 'Télécharge le projet'] },
  { type: 'libre', texte: 'Expliquez la différence entre une clé primaire et une clé étrangère.', points: 3 },
  { type: 'libre', texte: 'Décrivez comment vous protégeriez un formulaire de connexion.', points: 3 }
];

var reponses = {};
var courante = 0;
var secondes = 45 * 60;
var minuteur = null;

function heure() { return new Date().toLocaleTimeString('fr-FR'); }

function demarrerTest() {
  $('#vue-intro').classList.add('hidden');
  $('#vue-test').classList.remove('hidden');
  afficherQuestion();
  minuteur = setInterval(function () {
    secondes--;
    var m = Math.floor(secondes / 60), s = secondes % 60;
    var zone = $('#minuteur');
    zone.textContent = (m < 10 ? '0' : '') + m + ':' + (s < 10 ? '0' : '') + s;
    zone.classList.toggle('low', secondes < 300);
    if (secondes <= 0) { terminerTest(); }
  }, 1000);
}

function afficherQuestion() {
  var q = QUESTIONS_TEST[courante];
  var html = '<p class="muted small">Question ' + (courante + 1) + ' sur ' + QUESTIONS_TEST.length + (q.type === 'libre' ? ' — réponse libre, ' + q.points + ' points' : '') + '</p>' +
    '<h2 style="margin:6px 0 10px">' + q.texte + '</h2>';
  if (q.type === 'qcm') {
    q.options.forEach(function (opt, i) {
      var choisi = reponses[courante] === i;
      html += '<label class="option' + (choisi ? ' chosen' : '') + '"><input type="radio" name="rep" value="' + i + '"' + (choisi ? ' checked' : '') + '> <span>' + opt.replace(/</g, '&lt;') + '</span></label>';
    });
  } else {
    html += '<textarea id="reponse-libre" style="min-height:160px" placeholder="Écrivez votre réponse ici">' + (reponses[courante] || '') + '</textarea>';
  }
  $('#zone-question').innerHTML = html;

  $$('input[name=rep]').forEach(function (r) {
    r.addEventListener('change', function () { enregistrerReponse(Number(r.value)); afficherQuestion(); });
  });
  var libre = $('#reponse-libre');
  if (libre) { libre.addEventListener('input', function () { enregistrerReponse(libre.value); }); }

  var nav = '';
  QUESTIONS_TEST.forEach(function (x, i) {
    var classe = (i === courante ? 'current' : '') + (reponses[i] !== undefined && reponses[i] !== '' ? ' answered' : '');
    nav += '<button class="' + classe + '" data-q="' + i + '" aria-label="Question ' + (i + 1) + '">' + (i + 1) + '</button>';
  });
  $('#qnav').innerHTML = nav;
  $$('[data-q]').forEach(function (b) { b.addEventListener('click', function () { courante = Number(b.getAttribute('data-q')); afficherQuestion(); }); });
  $('#precedent').disabled = courante === 0;
  $('#suivant').classList.toggle('hidden', courante === QUESTIONS_TEST.length - 1);
}

function enregistrerReponse(valeur) {
  reponses[courante] = valeur;
  $('#sauvegarde').textContent = 'Réponses sauvegardées à ' + heure();
  var faites = 0;
  for (var i = 0; i < QUESTIONS_TEST.length; i++) { if (reponses[i] !== undefined && reponses[i] !== '') { faites++; } }
  $('#progression').style.width = (faites / QUESTIONS_TEST.length * 100) + '%';
  $('#restantes').textContent = QUESTIONS_TEST.length - faites;
  $$('[data-q]').forEach(function (b, i) { b.classList.toggle('answered', reponses[i] !== undefined && reponses[i] !== ''); });
}

function terminerTest() {
  clearInterval(minuteur);
  closeModals();
  $('#vue-test').classList.add('hidden');
  $('#vue-fin').classList.remove('hidden');
}

function pageTestCandidat() {
  $('#commencer').addEventListener('click', demarrerTest);
  $('#precedent').addEventListener('click', function () { courante--; afficherQuestion(); });
  $('#suivant').addEventListener('click', function () { courante++; afficherQuestion(); });
  $('#terminer').addEventListener('click', function () { openModal('fin-test'); });
  $('#confirmer-fin').addEventListener('click', terminerTest);
  $('#restantes').textContent = QUESTIONS_TEST.length;
}

/* ----- Choix du créneau ----- */
var creneauChoisi = null;

function pageCreneau() {
  var html = '';
  CRENEAUX.forEach(function (c, i) {
    html += '<button class="slot" data-creneau="' + i + '"><b>' + c.heure + '</b><span class="muted small">' + c.jour + '</span></button>';
  });
  $('#creneaux').innerHTML = html;
  $$('[data-creneau]').forEach(function (b) {
    b.addEventListener('click', function () {
      $$('.slot').forEach(function (s) { s.classList.remove('chosen'); });
      b.classList.add('chosen');
      creneauChoisi = CRENEAUX[Number(b.getAttribute('data-creneau'))];
      $('#valider-creneau').disabled = false;
    });
  });
  $('#valider-creneau').addEventListener('click', function () {
    $('#choix').classList.add('hidden');
    $('#confirme').classList.remove('hidden');
    $('#creneau-texte').textContent = creneauChoisi.jour + ' à ' + creneauChoisi.heure;
    toast('Rendez-vous confirmé. Un rappel vous sera envoyé avant la date.');
  });
}

document.addEventListener('DOMContentLoaded', function () {
  var page = document.body.getAttribute('data-page');
  if (page === 'test') { pageTestCandidat(); }
  if (page === 'creneau') { pageCreneau(); }
});
