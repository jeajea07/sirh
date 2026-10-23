(function () {
  var salaire = document.getElementById('salairePropose');
  var champ = document.getElementById('champ-salaire');
  var erreur = document.getElementById('erreur-salaire');
  var commentaire = document.getElementById('commentaire');

  function verifierSalaire() {
    if (!salaire || !champ || !erreur) { return; }
    var plafond = parseFloat(salaire.getAttribute('data-plafond'));
    var valeur = parseFloat(salaire.value);
    var depasse = !isNaN(plafond) && !isNaN(valeur) && valeur > plafond;

    champ.classList.toggle('error', depasse);
    erreur.classList.toggle('hidden', !depasse);
    if (depasse) {
      erreur.textContent = 'Ce salaire dépasse la grille salariale (' + salaire.getAttribute('data-plafond-fr') + ').';
    }
  }

  if (salaire) {
    salaire.addEventListener('input', verifierSalaire);
    verifierSalaire();
  }

  document.querySelectorAll('[data-commentaire="requis"]').forEach(function (bouton) {
    bouton.addEventListener('click', function (e) {
      if (commentaire && !commentaire.value.trim()) {
        e.preventDefault();
        commentaire.focus();
        commentaire.parentNode.classList.add('error');
        window.alert('Veuillez saisir un commentaire.');
      }
    });
  });
})();
