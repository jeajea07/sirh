/* Construit la barre latérale et la barre du haut selon l'espace (data-role de <body>) */

var ICONES = {
  home: '<path d="M3 11l9-8 9 8"/><path d="M5 10v10h14V10"/>',
  briefcase: '<rect x="3" y="7" width="18" height="13" rx="2"/><path d="M9 7V5a2 2 0 012-2h2a2 2 0 012 2v2M3 13h18"/>',
  users: '<circle cx="9" cy="8" r="3.5"/><path d="M2.5 20c.5-3.5 3-5.5 6.5-5.5s6 2 6.5 5.5"/><circle cx="17" cy="9" r="2.5"/><path d="M17.5 14.5c2.5.2 4 1.8 4.5 4.5"/>',
  clipboard: '<rect x="5" y="4" width="14" height="17" rx="2"/><path d="M9 4h6v3H9zM9 12h6M9 16h4"/>',
  calendar: '<rect x="3" y="5" width="18" height="16" rx="2"/><path d="M3 10h18M8 3v4M16 3v4"/>',
  trophy: '<path d="M8 4h8v5a4 4 0 01-8 0V4zM8 6H4v1a3 3 0 003 3M16 6h4v1a3 3 0 01-3 3M12 13v4M8 21h8M10 17h4"/>',
  history: '<path d="M3 12a9 9 0 109-9 9 9 0 00-6.5 2.8L3 8M3 3v5h5M12 7v5l3 2"/>',
  edit: '<path d="M4 20h4L19 9l-4-4L4 16v4zM13 7l4 4"/>',
  star: '<path d="M12 3l2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1L3.2 9.5l6.1-.9L12 3z"/>',
  settings: '<circle cx="12" cy="12" r="3"/><path d="M12 2v3M12 19v3M2 12h3M19 12h3M4.9 4.9l2.1 2.1M17 17l2.1 2.1M4.9 19.1L7 17M17 7l2.1-2.1"/>',
  bell: '<path d="M6 16V11a6 6 0 1112 0v5l2 2H4l2-2zM10 21h4"/>',
  logout: '<path d="M9 4H5a2 2 0 00-2 2v12a2 2 0 002 2h4M16 8l4 4-4 4M20 12H9"/>',
  search: '<circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/>',
  menu: '<path d="M4 6h16M4 12h16M4 18h16"/>'
};

function icone(nom) {
  return '<svg class="icon" viewBox="0 0 24 24" aria-hidden="true">' + ICONES[nom] + '</svg>';
}

var ESPACES = {
  rh: { nom: 'Ressources humaines', user: 'Hanta Ravelo', fonction: 'Responsable RH', menu: [
    { id: 'dashboard', texte: 'Tableau de bord', icone: 'home', lien: 'rh/dashboard.html' },
    { id: 'offres', texte: 'Offres', icone: 'briefcase', lien: 'rh/offres.html' },
    { id: 'candidatures', texte: 'Candidatures', icone: 'users', lien: 'rh/candidatures.html', badge: '5' },
    { id: 'test', texte: 'Test technique', icone: 'clipboard', lien: 'rh/test.html' },
    { id: 'entretiens', texte: 'Entretiens', icone: 'calendar', lien: 'rh/entretiens.html' },
    { id: 'final', texte: 'Confirmation finale', icone: 'trophy', lien: 'rh/final.html' },
    { id: 'historique', texte: 'Historique', icone: 'history', lien: 'rh/historique.html' } ],
    notifs: [['Nouvelle candidature reçue', 'Développeur web — il y a 2 h'], ['2 dossiers attendent depuis plus de 5 jours', 'Rappel automatique'], ['Test entièrement corrigé', 'Résultats prêts à confirmer'], ['Notes de l’entretien 1 prêtes', 'Rasolofo Nirina']] },
  technicien: { nom: 'Technicien', user: 'Mamy Andriamanga', fonction: 'Direction informatique', menu: [
    { id: 'candidats', texte: 'Mes candidats', icone: 'users', lien: 'technicien/candidats.html' },
    { id: 'notes', texte: 'Grille et notes', icone: 'edit', lien: 'technicien/notes.html' } ],
    notifs: [['Entretien 2 demain à 10:00', 'Rabe Fanja'], ['Notes à saisir', 'Entretien d’hier']] },
  dg: { nom: 'Direction générale', user: 'Lucie Rakotondrabe', fonction: 'Directrice générale', menu: [
    { id: 'candidats', texte: 'Candidats', icone: 'users', lien: 'dg/candidats.html' },
    { id: 'notes', texte: 'Notes et avis', icone: 'star', lien: 'dg/notes.html' } ],
    notifs: [['2 entretiens 3 cette semaine', 'Développeur web']] },
  direction: { nom: 'Direction responsable', user: 'Faly Randrianasolo', fonction: 'Direction informatique', menu: [
    { id: 'corrections', texte: 'Copies à corriger', icone: 'edit', lien: 'direction/corrections.html', badge: '4' } ],
    notifs: [['4 réponses libres à corriger', 'Test technique — Développeur web'], ['Rappel : correction attendue', 'Depuis 2 jours']] },
  candidat: { nom: 'Espace candidat', user: 'Andrianjafy Hery', fonction: 'Développeur web', menu: [
    { id: 'suivi', texte: 'Mon suivi', icone: 'home', lien: 'candidat/suivi.html' },
    { id: 'test', texte: 'Passer le test', icone: 'clipboard', lien: 'candidat/test.html' },
    { id: 'creneau', texte: 'Choisir mon créneau', icone: 'calendar', lien: 'candidat/creneau.html' } ],
    notifs: [['Votre dossier est validé', 'Convocation au test technique'], ['Rappel : test technique', 'Lundi 19 octobre à 08:30']] },
  admin: { nom: 'Administration', user: 'Administrateur', fonction: 'Compte système', menu: [
    { id: 'utilisateurs', texte: 'Utilisateurs', icone: 'users', lien: 'admin/utilisateurs.html' },
    { id: 'parametres', texte: 'Paramètres', icone: 'settings', lien: 'admin/parametres.html' } ],
    notifs: [['Nouvel utilisateur créé', 'Direction financière']] }
};

function construireEspace() {
  var body = document.body;
  var role = body.getAttribute('data-role');
  var espace = ESPACES[role];
  var racine = body.getAttribute('data-root') || '';
  var page = body.getAttribute('data-page');
  var titre = body.getAttribute('data-title') || '';
  var contenu = $('main');
  if (!espace || !contenu) { return; }

  var liens = '';
  espace.menu.forEach(function (m) {
    liens += '<a href="' + racine + m.lien + '"' + (m.id === page ? ' class="active"' : '') + '>' +
      icone(m.icone) + m.texte + (m.badge ? '<span class="nav-badge">' + m.badge + '</span>' : '') + '</a>';
  });

  var notifs = '';
  espace.notifs.forEach(function (n) { notifs += '<p>' + n[0] + '<small>' + n[1] + '</small></p>'; });

  var menu = document.createElement('aside');
  menu.className = 'sidebar';
  menu.innerHTML =
    '<a class="brand" href="' + racine + 'index.html"><img src="' + racine + 'assets/logo.svg" alt="" width="40" height="30"><span class="brand-text">Team<b>Up</b></span></a>' +
    '<div class="role-tag"><i></i>' + espace.nom + '</div>' +
    '<nav class="nav">' + liens + '</nav>' +
    '<div class="side-foot">' +
      '<div class="side-user"><div class="avatar">' + initiales(espace.user) + '</div><div><b>' + espace.user + '</b><span>' + espace.fonction + '</span></div></div>' +
      '<a class="side-link" href="' + racine + 'index.html">' + icone('home') + 'Changer d’espace</a>' +
      '<a class="side-link" href="' + racine + 'connexion.html">' + icone('logout') + 'Se déconnecter</a>' +
    '</div>';

  var haut = document.createElement('header');
  haut.className = 'topbar';
  haut.innerHTML =
    '<button class="burger" aria-label="Ouvrir le menu">' + icone('menu') + '</button>' +
    '<h2>' + titre + '</h2>' +
    '<div class="search"><svg viewBox="0 0 24 24">' + ICONES.search + '</svg><input type="text" placeholder="Rechercher" aria-label="Rechercher"></div>' +
    '<div style="position:relative"><button class="icon-btn" data-bell aria-label="Notifications">' + icone('bell') + '<span class="dot"></span></button>' +
    '<div class="drop"><h3>Notifications</h3>' + notifs + '</div></div>' +
    '<div class="avatar">' + initiales(espace.user) + '</div>';

  var appli = document.createElement('div');
  appli.className = 'app';
  var colonne = document.createElement('div');
  colonne.className = 'app-main';
  body.insertBefore(appli, contenu);
  appli.appendChild(menu);
  appli.appendChild(colonne);
  colonne.appendChild(haut);
  contenu.classList.add('content');
  colonne.appendChild(contenu);
}

document.addEventListener('DOMContentLoaded', construireEspace);
