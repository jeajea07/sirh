(function () {
  var motif = document.getElementById('motif');
  var typeContrat = document.getElementById('idTypeContrat');
  var blocDuree = document.getElementById('bloc-duree');

  function mettreAJour() {
    if (typeContrat && blocDuree) {
      var option = typeContrat.options[typeContrat.selectedIndex];
      var code = option ? option.getAttribute('data-code') : null;
      var avecDuree = code === 'CDD' || code === 'CE' || code === 'STAGE';
      blocDuree.classList.toggle('hidden', !avecDuree);
    }
  }

  if (typeContrat) { typeContrat.addEventListener('change', mettreAJour); }
  mettreAJour();

  document.addEventListener('submit', function (e) {
    var message = e.target.getAttribute('data-confirm');
    if (message && !window.confirm(message)) { e.preventDefault(); }
  });
})();
