/* Page de connexion : vérifie les champs avant l'envoi au serveur.
   La vraie vérification du compte est faite par Spring Security. */
(function () {
  var form = document.getElementById('form-connexion');
  if (!form) { return; }

  var alerte = form.querySelector('.form-errors');
  var bouton = form.querySelector('button[type=submit]');
  var texteBouton = bouton.textContent;
  var champLogin = form.querySelector('#login-email');
  var loginEnvoye = form.querySelector('input[name="login"]');
  var champMdp = form.querySelector('#mdp');
  var boutonVisibilite = form.querySelector('.password-toggle-btn');
  var erreurConnexion = form.querySelector('#erreur-connexion');

  function effacerAncienneErreur() {
    if (erreurConnexion) { erreurConnexion.remove(); }
  }

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
    var roleChoisi = form.querySelector('input[name="roleChoisi"]:checked');
    var blocRole = form.querySelector('.role-choice');

    if (blocRole) { blocRole.classList.toggle('error', !roleChoisi); }
    if (!roleChoisi) {
      manquants.push('Rôle');
      premier = form.querySelector('input[name="roleChoisi"]');
    }

    Array.prototype.forEach.call(form.querySelectorAll('[required]'), function (champ) {
      if (champ.type === 'radio') { return; }
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
    // Le serveur compare ce rôle au rôle réellement enregistré pour ce compte.
    // Le candidat s'authentifie sans préfixe de rôle (identifiant ou email).
    loginEnvoye.value = roleChoisi.value === 'CANDIDAT'
      ? champLogin.value.trim()
      : roleChoisi.value + '|' + champLogin.value;
    bouton.disabled = true;              // évite le double envoi
    bouton.textContent = 'Connexion…';
  });

  // l'erreur disparaît dès que l'on retape dans le champ
  form.addEventListener('input', function (e) {
    effacerAncienneErreur();
    var bloc = e.target.closest('.field');
    if (bloc) { bloc.classList.remove('error'); }
  });
  form.addEventListener('change', function (e) {
    effacerAncienneErreur();
    if (e.target.name === 'roleChoisi') {
      var bloc = e.target.closest('.role-choice');
      if (bloc) { bloc.classList.remove('error'); }
    }
  });

  // retour arrière du navigateur : le bouton ne doit pas rester bloqué
  window.addEventListener('pageshow', remettreBouton);
})();
