/* Direction responsable : correction des réponses libres (copies anonymes) */

var COPIES = [
  { code: 'C-0412', question: 'Expliquez la différence entre une clé primaire et une clé étrangère.', max: 3,
    reponse: 'La clé primaire identifie une ligne de façon unique dans une table. La clé étrangère est un champ qui renvoie vers la clé primaire d’une autre table, pour relier les données.' },
  { code: 'C-0412', question: 'Décrivez comment vous protégeriez un formulaire de connexion.', max: 3,
    reponse: 'Je limite le nombre d’essais, je hache les mots de passe, j’utilise HTTPS et je vérifie les données côté serveur.' },
  { code: 'C-0415', question: 'Expliquez la différence entre une clé primaire et une clé étrangère.', max: 3,
    reponse: 'La clé primaire est le numéro de la ligne. La clé étrangère est une deuxième clé dans la même table.' },
  { code: 'C-0415', question: 'Décrivez comment vous protégeriez un formulaire de connexion.', max: 3,
    reponse: 'Je mettrais un captcha et un mot de passe long.' }
];
var corrigees = 0;

function afficherCopies() {
  var html = '';
  COPIES.forEach(function (c, i) {
    html += '<div class="card" id="copie-' + i + '"><div class="card-head"><h3>Copie ' + c.code + '</h3><span class="badge badge-warn" id="etat-' + i + '">À corriger</span></div>' +
      '<p class="muted small">Question</p><p class="bold" style="margin-bottom:10px">' + c.question + '</p>' +
      '<p class="muted small">Réponse du candidat</p><p style="background:var(--bg);border-radius:8px;padding:12px;margin:4px 0 14px">' + c.reponse + '</p>' +
      '<div class="row"><div class="field" style="width:140px"><label for="note-' + i + '">Note sur ' + c.max + '</label><input type="number" id="note-' + i + '" min="0" max="' + c.max + '" step="0.5"></div>' +
      '<button class="btn btn-primary" style="margin-top:20px" data-copie="' + i + '">Enregistrer la note</button></div></div>';
  });
  $('#copies').innerHTML = html;

  $$('[data-copie]').forEach(function (b) {
    b.addEventListener('click', function () {
      var i = Number(b.getAttribute('data-copie'));
      var champ = $('#note-' + i);
      var note = parseFloat(champ.value);
      if (isNaN(note) || note < 0 || note > COPIES[i].max) {
        champ.closest('.field').classList.add('error');
        toast('Saisissez une note entre 0 et ' + COPIES[i].max + '.');
        return;
      }
      champ.closest('.field').classList.remove('error');
      if ($('#etat-' + i).textContent === 'À corriger') { corrigees++; }
      $('#etat-' + i).className = 'badge badge-ok';
      $('#etat-' + i).textContent = 'Corrigée : ' + note + ' / ' + COPIES[i].max;
      majProgression();
    });
  });
  majProgression();
}

function majProgression() {
  $('#compteur').textContent = corrigees + ' / ' + COPIES.length + ' corrigées';
  $('#barre').style.width = (corrigees / COPIES.length * 100) + '%';
  if (corrigees === COPIES.length) { toast('Toutes les copies sont corrigées. Le RH est prévenu.'); }
}

document.addEventListener('DOMContentLoaded', afficherCopies);
