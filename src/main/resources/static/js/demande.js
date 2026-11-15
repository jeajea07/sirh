(function () {
  var motif = document.getElementById('motif');
  var typeContrat = document.getElementById('idTypeContrat');
  var blocDuree = document.getElementById('bloc-duree');
  var blocSortant = document.getElementById('bloc-employe-sortant');

  function mettreAJour() {
    if (typeContrat && blocDuree) {
      var option = typeContrat.options[typeContrat.selectedIndex];
      var avecDuree = !!option && option.getAttribute('data-duree') === 'true';
      blocDuree.classList.toggle('hidden', !avecDuree);
    }
    if (motif && blocSortant) {
      blocSortant.classList.toggle('hidden', motif.value !== 'REMPLACEMENT');
    }
  }

  if (typeContrat) { typeContrat.addEventListener('change', mettreAJour); }
  if (motif) { motif.addEventListener('change', mettreAJour); }
  mettreAJour();

  document.addEventListener('submit', function (e) {
    var message = e.target.getAttribute('data-confirm');
    if (message && !window.confirm(message)) { e.preventDefault(); }
  });
})();
