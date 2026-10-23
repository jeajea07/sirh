/* Page de connexion : vérifie les champs avant l'envoi au serveur.
   La vraie vérification du compte est faite par Spring Security. */
(function () {
  var form = document.getElementById('form-connexion');
  if (!form) { return; }

  var alerte = form.querySelector('.form-errors');
  var bouton = form.querySelector('button[type=submit]');
  var texteBouton = bouton.textContent;
  var champMdp = form.querySelector('#mdp');
  var boutonVisibilite = form.querySelector('.password-toggle-btn');

  if (champMdp && boutonVisibilite) {
    boutonVisibilite.addEventListener('click', function () {
      var afficher = champMdp.type === 'password';
      champMdp.type = afficher ? 'text' : 'password';
      boutonVisibilite.setAttribute('aria-pressed', afficher ? 'true' : 'false');
      boutonVisibilite.setAttribute('aria-label', afficher ? 'Masquer le mot de passe' : 'Afficher le mot de passe');
      champMdp.focus();
    });
  }

  function estVide(champ) {
    // le mot de passe n'est jamais "nettoyé" : seuls les espaces autour de l'identifiant le sont
    return champ.name === 'mdp' ? champ.value === '' : !champ.value.trim();
  }

  function remettreBouton() {
    bouton.disabled = false;
    bouton.textContent = texteBouton;
  }

  form.addEventListener('submit', function (e) {
    var manquants = [];
    var premier = null;

    Array.prototype.forEach.call(form.querySelectorAll('[required]'), function (champ) {
      var vide = estVide(champ);
      var bloc = champ.closest('.field');
      if (bloc) { bloc.classList.toggle('error', vide); }
      if (vide) {
        var label = bloc ? bloc.querySelector('label') : null;
        manquants.push(label ? label.textContent.replace('*', '').trim() : 'Champ obligatoire');
        if (!premier) { premier = champ; }
      }
    });

    if (manquants.length) {
      e.preventDefault();
      alerte.className = 'form-errors alert alert-bad';
      alerte.innerHTML = 'Il manque des informations :<ul><li>' + manquants.join('</li><li>') + '</li></ul>';
      premier.focus();
      return;
    }

    alerte.className = 'form-errors';
    alerte.innerHTML = '';
    bouton.disabled = true;              // évite le double envoi
    bouton.textContent = 'Connexion…';
  });

  // l'erreur disparaît dès que l'on retape dans le champ
  form.addEventListener('input', function (e) {
    var bloc = e.target.closest('.field');
    if (bloc) { bloc.classList.remove('error'); }
  });

  // retour arrière du navigateur : le bouton ne doit pas rester bloqué
  window.addEventListener('pageshow', remettreBouton);
})();
