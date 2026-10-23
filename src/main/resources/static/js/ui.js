/* Composants communs : fenêtres modales, onglets, messages, formulaires */

function $(selecteur, racine) { return (racine || document).querySelector(selecteur); }
function $$(selecteur, racine) { return Array.prototype.slice.call((racine || document).querySelectorAll(selecteur)); }

/* ----- Message éphémère ----- */
function toast(message) {
  var zone = $('.toasts');
  if (!zone) {
    zone = document.createElement('div');
    zone.className = 'toasts';
    document.body.appendChild(zone);
  }
  var t = document.createElement('div');
  t.className = 'toast';
  t.textContent = message;
  zone.appendChild(t);
  setTimeout(function () { t.remove(); }, 3600);
}

/* ----- Fenêtres modales ----- */
function openModal(id) { var m = document.getElementById(id); if (m) { m.classList.add('open'); } }
function closeModals() { $$('.modal-back.open').forEach(function (m) { m.classList.remove('open'); }); }

/* ----- Petits outils d'affichage ----- */
function badge(texte, type) { return '<span class="badge ' + (type ? 'badge-' + type : '') + '">' + texte + '</span>'; }

function badgeClassement(c) {
  if (c === 'conforme') { return badge('Conforme', 'ok'); }
  if (c === 'non') { return badge('Non conforme', 'bad'); }
  return badge('À regarder', 'warn');
}
function initiales(nom) {
  var mots = nom.split(' ');
  return (mots[0].charAt(0) + (mots[1] ? mots[1].charAt(0) : '')).toUpperCase();
}
function nombre(n, decimales) { return n === null || n === undefined ? '—' : Number(n).toFixed(decimales === undefined ? 1 : decimales).replace('.', ','); }

/* ----- Évènements globaux ----- */
document.addEventListener('click', function (e) {
  var cible = e.target;

  var ouvrir = cible.closest('[data-open]');
  if (ouvrir) { openModal(ouvrir.getAttribute('data-open')); return; }

  if (cible.closest('[data-close]') || cible.classList.contains('modal-back')) { closeModals(); return; }

  var msg = cible.closest('[data-toast]');
  if (msg) {
    toast(msg.getAttribute('data-toast'));
    if (msg.hasAttribute('data-close-modal')) { closeModals(); }
  }

  var onglet = cible.closest('.tab');
  if (onglet && onglet.getAttribute('data-tab')) {
    var barre = onglet.parentNode;
    $$('.tab', barre).forEach(function (t) { t.classList.remove('active'); });
    onglet.classList.add('active');
    var zone = barre.parentNode;
    $$('.tab-panel', zone).forEach(function (p) { p.classList.remove('active'); });
    var panneau = document.getElementById(onglet.getAttribute('data-tab'));
    if (panneau) { panneau.classList.add('active'); }
  }

  var burger = cible.closest('.burger');
  if (burger) { document.body.classList.toggle('sidebar-open'); }

  var cloche = cible.closest('[data-bell]');
  if (cloche) { $('.drop', cloche.parentNode).classList.toggle('open'); }
  else if (!cible.closest('.drop')) { $$('.drop.open').forEach(function (d) { d.classList.remove('open'); }); }
});

document.addEventListener('keydown', function (e) { if (e.key === 'Escape') { closeModals(); } });

/* ----- Formulaires : vérification des champs obligatoires ----- */
document.addEventListener('submit', function (e) {
  var form = e.target;
  if (!form.hasAttribute('data-validate')) { return; }
  e.preventDefault();

  var manquants = [];
  $$('[required]', form).forEach(function (champ) {
    var vide = champ.type === 'checkbox' ? !champ.checked : !champ.value.trim();
    var bloc = champ.closest('.field');
    if (bloc) { bloc.classList.toggle('error', vide); }
    if (vide) {
      var label = bloc ? $('label', bloc) : null;
      manquants.push(label ? label.textContent.replace('*', '').trim() : (champ.getAttribute('aria-label') || 'Champ obligatoire'));
    }
  });

  var alerte = $('.form-errors', form);
  if (alerte) {
    if (manquants.length) {
      alerte.className = 'form-errors alert alert-bad';
      alerte.innerHTML = 'Il manque des informations :<ul><li>' + manquants.join('</li><li>') + '</li></ul>';
    } else {
      alerte.className = 'form-errors';
      alerte.innerHTML = '';
    }
  }
  if (manquants.length) { return; }

  var succes = form.getAttribute('data-success');
  if (succes) { toast(succes); }
  if (form.hasAttribute('data-close-modal')) { closeModals(); }
  var suite = form.getAttribute('data-redirect');
  if (suite) { setTimeout(function () { window.location.href = suite; }, 900); }
  var apres = form.getAttribute('data-after');
  if (apres && window[apres]) { window[apres](form); }
});

/* ----- Dépôt d'un CV : PDF ou DOCX, 5 Mo maximum ----- */
document.addEventListener('change', function (e) {
  var champ = e.target;
  if (!champ.hasAttribute('data-cv')) { return; }
  var zone = champ.closest('.dropzone');
  var info = $('.cv-info', zone);
  var fichier = champ.files[0];
  zone.classList.remove('ok', 'bad');
  if (!fichier) { info.textContent = ''; return; }
  var nom = fichier.name.toLowerCase();
  var formatOk = nom.slice(-4) === '.pdf' || nom.slice(-5) === '.docx';
  var tailleOk = fichier.size <= 5 * 1024 * 1024;
  if (!formatOk) {
    zone.classList.add('bad'); info.textContent = 'Format refusé. Choisissez un fichier PDF ou DOCX.'; champ.value = '';
  } else if (!tailleOk) {
    zone.classList.add('bad'); info.textContent = 'Fichier trop lourd (5 Mo maximum).'; champ.value = '';
  } else {
    zone.classList.add('ok'); info.textContent = fichier.name + ' prêt à l’envoi.';
  }
});
