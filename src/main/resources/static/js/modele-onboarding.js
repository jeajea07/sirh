/* Édition dynamique des tâches d'un modèle d'onboarding. */
(function () {
  function conteneur() { return document.querySelector('[data-taches]'); }

  function reindexer() {
    var zone = conteneur();
    if (!zone) { return; }
    var lignes = Array.prototype.slice.call(zone.querySelectorAll('[data-tache]'));
    lignes.forEach(function (ligne, index) {
      ligne.setAttribute('data-tache', index);
      ligne.querySelectorAll('[name]').forEach(function (champ) {
        champ.name = champ.name.replace(/taches\[\d+\]/, 'taches[' + index + ']');
      });
      var numero = ligne.querySelector('[data-numero]');
      if (numero) { numero.textContent = 'Tâche ' + (index + 1); }
    });
    zone.querySelectorAll('select[data-dependance]').forEach(function (select) {
      var valeur = select.value;
      select.innerHTML = '';
      var aucune = document.createElement('option');
      aucune.value = '';
      aucune.textContent = 'Aucune dépendance';
      select.appendChild(aucune);
      lignes.forEach(function (ligne, i) {
        if (ligne === select.closest('[data-tache]')) { return; }
        var titre = ligne.querySelector('[name$=".titre"]');
        var option = document.createElement('option');
        option.value = String(i);
        option.textContent = (i + 1) + '. ' + (titre && titre.value ? titre.value : 'Tâche sans titre');
        if (String(valeur) === String(i)) { option.selected = true; }
        select.appendChild(option);
      });
    });
    if (window.reinitialiserSelects) { window.reinitialiserSelects(); }
  }

  function ajouter() {
    var zone = conteneur();
    if (!zone) { return; }
    var modele = zone.querySelector('[data-tache]');
    if (!modele) { return; }
    var copie = modele.cloneNode(true);
    copie.querySelectorAll('input, textarea, select').forEach(function (champ) {
      if (champ.type === 'checkbox') { champ.checked = false; }
      else if (champ.tagName === 'SELECT') { champ.selectedIndex = 0; }
      else { champ.value = ''; }
    });
    var dependance = copie.querySelector('select[data-dependance]');
    if (dependance) { dependance.innerHTML = '<option value="">Aucune dépendance</option>'; }
    zone.appendChild(copie);
    reindexer();
  }

  document.addEventListener('click', function (e) {
    if (e.target.closest('[data-ajouter-tache]')) {
      e.preventDefault();
      ajouter();
    }
    var supprimer = e.target.closest('[data-supprimer-tache]');
    if (supprimer) {
      e.preventDefault();
      var zone = conteneur();
      if (zone && zone.querySelectorAll('[data-tache]').length <= 1) {
        if (window.toast) { toast('Le modèle doit conserver au moins une tâche.'); }
        return;
      }
      supprimer.closest('[data-tache]').remove();
      reindexer();
    }
  });

  document.addEventListener('input', function (e) {
    if (e.target.matches('[name$=".titre"]')) {
      var zone = conteneur();
      if (zone) {
        zone.querySelectorAll('select[data-dependance]').forEach(function (select) {
          var valeur = select.value;
          var ligne = select.closest('[data-tache]');
          var index = Array.prototype.indexOf.call(zone.querySelectorAll('[data-tache]'), ligne);
          var options = select.querySelectorAll('option');
          options.forEach(function (option, i) {
            if (i === 0) { return; }
            var lignes = zone.querySelectorAll('[data-tache]');
            var titre = lignes[i - 1] ? lignes[i - 1].querySelector('[name$=".titre"]') : null;
            option.textContent = i + '. ' + (titre && titre.value ? titre.value : 'Tâche sans titre');
          });
          select.value = valeur;
        });
      }
    }
  });

  document.addEventListener('DOMContentLoaded', function () {
    reindexer();
  });
})();
