/* Remplace visuellement les <select> natifs par un bouton et une liste accessibles. */
(function () {
  function construire(select) {
    if (select.dataset.selectPret === '1') { return; }
    select.dataset.selectPret = '1';

    var wrapper = document.createElement('div');
    wrapper.className = 'select';
    select.parentNode.insertBefore(wrapper, select);
    wrapper.appendChild(select);
    select.classList.add('select-native');
    select.setAttribute('tabindex', '-1');

    var bouton = document.createElement('button');
    bouton.type = 'button';
    bouton.className = 'select-btn';
    bouton.setAttribute('aria-haspopup', 'listbox');
    bouton.setAttribute('aria-expanded', 'false');
    if (select.id) { bouton.id = select.id + '-bouton'; }

    var texte = document.createElement('span');
    var chevron = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    chevron.setAttribute('viewBox', '0 0 24 24');
    chevron.setAttribute('aria-hidden', 'true');
    var path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
    path.setAttribute('d', 'M6 9l6 6 6-6');
    chevron.appendChild(path);
    bouton.appendChild(texte);
    bouton.appendChild(chevron);

    var liste = document.createElement('div');
    liste.className = 'select-list';
    liste.setAttribute('role', 'listbox');
    liste.id = (select.id || ('select-' + Math.random().toString(36).slice(2))) + '-liste';
    bouton.setAttribute('aria-controls', liste.id);
    wrapper.appendChild(bouton);
    wrapper.appendChild(liste);

    function majTexte() {
      var choix = select.options[select.selectedIndex];
      var vide = !choix || choix.value === '';
      texte.textContent = choix ? choix.textContent : '—';
      texte.className = vide ? 'placeholder' : '';
    }

    function construireOptions() {
      liste.innerHTML = '';
      Array.prototype.forEach.call(select.options, function (option, index) {
        var el = document.createElement('button');
        el.type = 'button';
        el.className = 'select-option';
        el.setAttribute('role', 'option');
        el.setAttribute('aria-selected', String(option.selected));
        el.textContent = option.textContent;
        if (option.disabled) { el.disabled = true; }
        el.addEventListener('click', function () {
          select.selectedIndex = index;
          select.dispatchEvent(new Event('change', { bubbles: true }));
          construireOptions();
          majTexte();
          fermer();
          bouton.focus();
        });
        liste.appendChild(el);
      });
    }

    function ouvrir() {
      document.querySelectorAll('.select.open').forEach(function (autre) {
        if (autre !== wrapper) { autre.classList.remove('open'); }
      });
      wrapper.classList.add('open');
      bouton.setAttribute('aria-expanded', 'true');
      construireOptions();
      var actif = liste.querySelector('[aria-selected="true"]');
      if (actif) { actif.focus(); }
    }

    function fermer() {
      wrapper.classList.remove('open');
      bouton.setAttribute('aria-expanded', 'false');
    }

    bouton.addEventListener('click', function () {
      if (wrapper.classList.contains('open')) { fermer(); } else { ouvrir(); }
    });
    bouton.addEventListener('keydown', function (e) {
      if (e.key === 'ArrowDown' || e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        ouvrir();
      }
    });
    liste.addEventListener('keydown', function (e) {
      var options = Array.prototype.slice.call(liste.querySelectorAll('.select-option:not(:disabled)'));
      var index = options.indexOf(document.activeElement);
      if (e.key === 'ArrowDown') {
        e.preventDefault();
        options[Math.min(index + 1, options.length - 1)].focus();
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        options[Math.max(index - 1, 0)].focus();
      } else if (e.key === 'Escape') {
        fermer();
        bouton.focus();
      } else if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        if (document.activeElement.classList.contains('select-option')) { document.activeElement.click(); }
      }
    });
    select.addEventListener('change', majTexte);
    majTexte();
  }

  document.addEventListener('click', function (e) {
    if (!e.target.closest('.select')) {
      document.querySelectorAll('.select.open').forEach(function (s) { s.classList.remove('open'); });
      document.querySelectorAll('.select-btn[aria-expanded="true"]').forEach(function (b) { b.setAttribute('aria-expanded', 'false'); });
    }
  });

  window.initialiserSelects = function (racine) {
    (racine || document).querySelectorAll('select').forEach(construire);
  };
  window.reinitialiserSelects = function () {
    document.querySelectorAll('.select').forEach(function (wrapper) {
      var select = wrapper.querySelector('select');
      if (!select) { return; }
      select.dataset.selectPret = '0';
      select.classList.remove('select-native');
      select.removeAttribute('tabindex');
      wrapper.parentNode.insertBefore(select, wrapper);
      wrapper.remove();
    });
    window.initialiserSelects();
  };
  document.addEventListener('DOMContentLoaded', function () {
    window.initialiserSelects();
  });
})();
