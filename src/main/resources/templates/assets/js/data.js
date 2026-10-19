/* Données fictives pour la démonstration du template */

var OFFRES = [
  { id: 1, titre: 'Développeur web', service: 'Direction informatique', lieu: 'Antananarivo', contrat: 'CDI', postes: 2,
    publie: '01/10/2026', limite: '15/11/2026', statut: 'Publiée', nb: 9,
    resume: 'Vous rejoignez l’équipe qui développe et maintient les applications internes de l’entreprise.',
    missions: ['Développer de nouvelles fonctionnalités web', 'Maintenir et corriger les applications existantes', 'Participer aux revues de code'],
    obligatoires: ['Licence en informatique ou équivalent', '2 ans d’expérience minimum', 'Compétence JavaScript'],
    souhaites: ['Connaissance de Spring Boot', 'Anglais technique'] },
  { id: 2, titre: 'Comptable', service: 'Direction financière', lieu: 'Antananarivo', contrat: 'CDD 12 mois', postes: 1,
    publie: '05/10/2026', limite: '20/11/2026', statut: 'Publiée', nb: 2,
    resume: 'Vous assurez la tenue de la comptabilité générale et le suivi des paiements fournisseurs.',
    missions: ['Saisir et contrôler les écritures', 'Préparer les clôtures mensuelles', 'Suivre les règlements'],
    obligatoires: ['Diplôme en comptabilité', '3 ans d’expérience minimum', 'Maîtrise d’un logiciel comptable'],
    souhaites: ['Connaissance du plan comptable malgache'] },
  { id: 3, titre: 'Responsable logistique', service: 'Direction des opérations', lieu: 'Toamasina', contrat: 'CDI', postes: 1,
    publie: '08/10/2026', limite: '30/11/2026', statut: 'Publiée', nb: 0,
    resume: 'Vous pilotez les flux de marchandises entre les entrepôts et les points de livraison.',
    missions: ['Organiser les tournées de livraison', 'Gérer les stocks', 'Encadrer une équipe de 6 personnes'],
    obligatoires: ['Diplôme en logistique ou gestion', '5 ans d’expérience minimum', 'Permis B'],
    souhaites: ['Expérience en entrepôt'] },
  { id: 4, titre: 'Assistant(e) RH', service: 'Ressources humaines', lieu: 'Antananarivo', contrat: 'CDI', postes: 1,
    publie: '', limite: '10/12/2026', statut: 'Validée', nb: 0,
    resume: 'Vous aidez l’équipe RH dans le suivi administratif du personnel.',
    missions: ['Tenir les dossiers du personnel', 'Préparer les contrats'],
    obligatoires: ['Licence en gestion', '1 an d’expérience'], souhaites: [] },
  { id: 5, titre: 'Technicien réseau', service: 'Direction informatique', lieu: 'Antananarivo', contrat: 'CDI', postes: 0,
    publie: '', limite: '', statut: 'Brouillon', nb: 0,
    resume: '', missions: [], obligatoires: [], souhaites: [] }
];

/* classement : conforme / regarder / non */
var CANDIDATS = [
  { id: 101, nom: 'Rakoto Andry', offre: 1, recu: '05/10/2026', jours: 6, classement: 'conforme', valide: false, statut: 'À étudier',
    diplome: 'Licence en informatique', exp: '3 ans', crit: { diplome: 'ok', exp: 'ok', skill: 'ok' }, test: null, notes: [null, null, null] },
  { id: 102, nom: 'Rasoanaivo Miora', offre: 1, recu: '08/10/2026', jours: 3, classement: 'regarder', valide: false, statut: 'À étudier',
    diplome: 'Licence en informatique', exp: '1 an 10 mois', crit: { diplome: 'ok', exp: 'douteux', skill: 'ok' }, test: null, notes: [null, null, null] },
  { id: 103, nom: 'Randria Tiana', offre: 1, recu: '10/10/2026', jours: 2, classement: 'non', valide: false, statut: 'À étudier',
    diplome: 'Aucun diplôme indiqué', exp: '0 an', crit: { diplome: 'ko', exp: 'ko', skill: 'ok' }, test: null, notes: [null, null, null] },
  { id: 104, nom: 'Andrianjafy Hery', offre: 1, recu: '03/10/2026', jours: 0, classement: 'conforme', valide: true, statut: 'Test technique',
    diplome: 'Master en informatique', exp: '4 ans', crit: { diplome: 'ok', exp: 'ok', skill: 'ok' }, test: 15.5, notes: [null, null, null] },
  { id: 105, nom: 'Ravelo Lalaina', offre: 1, recu: '02/10/2026', jours: 0, classement: 'conforme', valide: true, statut: 'Entretien RH',
    diplome: 'Licence en informatique', exp: '2 ans', crit: { diplome: 'ok', exp: 'ok', skill: 'ok' }, test: 17, notes: [4.2, null, null] },
  { id: 106, nom: 'Rabe Fanja', offre: 1, recu: '02/10/2026', jours: 0, classement: 'conforme', valide: true, statut: 'Entretien technicien',
    diplome: 'Licence en informatique', exp: '5 ans', crit: { diplome: 'ok', exp: 'ok', skill: 'ok' }, test: 16, notes: [4.0, null, null] },
  { id: 107, nom: 'Razafy Toky', offre: 1, recu: '01/10/2026', jours: 0, classement: 'conforme', valide: true, statut: 'Confirmation finale',
    diplome: 'Master en informatique', exp: '6 ans', crit: { diplome: 'ok', exp: 'ok', skill: 'ok' }, test: 18, notes: [4.5, 4.3, 4.6] },
  { id: 108, nom: 'Rakotomalala Sitraka', offre: 1, recu: '01/10/2026', jours: 0, classement: 'conforme', valide: true, statut: 'Confirmation finale',
    diplome: 'Licence en informatique', exp: '3 ans', crit: { diplome: 'ok', exp: 'ok', skill: 'ok' }, test: 16.5, notes: [3.9, 4.1, 4.0] },
  { id: 109, nom: 'Rasolofo Nirina', offre: 1, recu: '02/10/2026', jours: 0, classement: 'conforme', valide: true, statut: 'Entretien RH',
    diplome: 'Licence en informatique', exp: '2 ans', crit: { diplome: 'ok', exp: 'ok', skill: 'ok' }, test: 14.5, notes: [5, 4.4, null] },
  { id: 110, nom: 'Andriamasy Voahangy', offre: 2, recu: '07/10/2026', jours: 7, classement: 'conforme', valide: false, statut: 'À étudier',
    diplome: 'Licence en comptabilité', exp: '4 ans', crit: { diplome: 'ok', exp: 'ok', skill: 'ok' }, test: null, notes: [null, null, null] },
  { id: 111, nom: 'Ramanana Zo', offre: 2, recu: '14/10/2026', jours: 1, classement: 'regarder', valide: false, statut: 'À étudier',
    diplome: 'BTS comptabilité', exp: '3 ans', crit: { diplome: 'douteux', exp: 'ok', skill: 'douteux' }, test: null, notes: [null, null, null] },
  { id: 112, nom: 'Ramiandrisoa Lova', offre: 1, recu: '01/10/2026', jours: 0, classement: 'conforme', valide: true, statut: 'Confirmation finale',
    diplome: 'Master en informatique', exp: '4 ans', crit: { diplome: 'ok', exp: 'ok', skill: 'ok' }, test: 17, notes: [4.2, 4.0, 4.4] },
  { id: 113, nom: 'Rafanomezantsoa Kanto', offre: 1, recu: '02/10/2026', jours: 0, classement: 'conforme', valide: true, statut: 'Confirmation finale',
    diplome: 'Licence en informatique', exp: '2 ans', crit: { diplome: 'ok', exp: 'ok', skill: 'ok' }, test: 15, notes: [3.8, 3.7, 4.0] }
];

var SPONTANEES = [
  { id: 201, nom: 'Rajaonarison Haja', domaine: 'Informatique', recu: '09/10/2026' },
  { id: 202, nom: 'Raharimalala Soa', domaine: 'Comptabilité', recu: '11/10/2026' }
];

/* Test technique : 14 points en questions à cocher + 6 points en réponses libres = 20 */
var SEUIL_TEST = 12;
var TESTS = [
  { nom: 'Razafy Toky', auto: 13, libre: 5, statut: 'Corrigé' },
  { nom: 'Ramiandrisoa Lova', auto: 12, libre: 5, statut: 'Corrigé' },
  { nom: 'Ravelo Lalaina', auto: 12, libre: 5, statut: 'Corrigé' },
  { nom: 'Rakotomalala Sitraka', auto: 11.5, libre: 5, statut: 'Corrigé' },
  { nom: 'Rabe Fanja', auto: 11, libre: 5, statut: 'Corrigé' },
  { nom: 'Andrianjafy Hery', auto: 10.5, libre: 5, statut: 'Corrigé' },
  { nom: 'Rasolofo Nirina', auto: 9.5, libre: 5, statut: 'Corrigé' },
  { nom: 'Randriamampionona Eric', auto: 8, libre: 2.5, statut: 'Corrigé' },
  { nom: 'Rakotonirina Mamy', auto: 11, libre: null, statut: 'En correction' },
  { nom: 'Andrianasolo Faly', auto: null, libre: null, statut: 'Absent' }
];

/* Grilles d'évaluation : importance 1, 2 ou 3 ; elim = question éliminatoire avec une note minimale */
var GRILLES = {
  1: { titre: 'Entretien 1 — RH', jury: 'RH', seuil: 3.5, debut: '02/10/2026', questions: [
    { texte: 'Présentez votre parcours et vos motivations', imp: 1, elim: false, min: 0 },
    { texte: 'Comment gérez-vous un désaccord dans une équipe ?', imp: 2, elim: false, min: 0 },
    { texte: 'Quelles sont vos disponibilités et vos attentes ?', imp: 1, elim: false, min: 0 },
    { texte: 'Pourquoi ce poste dans notre entreprise ?', imp: 2, elim: true, min: 3 } ] },
  2: { titre: 'Entretien 2 — Technicien', jury: 'Technicien', seuil: 3.5, debut: '08/10/2026', questions: [
    { texte: 'Concevoir une API REST pour la gestion des candidatures', imp: 3, elim: true, min: 3 },
    { texte: 'Modéliser et interroger une base de données', imp: 2, elim: false, min: 0 },
    { texte: 'Débogage : étude de cas sur une application lente', imp: 3, elim: false, min: 0 },
    { texte: 'Bonnes pratiques de code et de tests', imp: 1, elim: false, min: 0 },
    { texte: 'Veille et apprentissage des nouvelles technologies', imp: 1, elim: false, min: 0 } ] },
  3: { titre: 'Entretien 3 — DG', jury: 'DG', seuil: 3.5, debut: '13/10/2026', questions: [
    { texte: 'Vision du poste et ambition dans l’entreprise', imp: 2, elim: false, min: 0 },
    { texte: 'Adhésion aux valeurs de l’entreprise', imp: 3, elim: true, min: 3 },
    { texte: 'Autonomie et leadership', imp: 2, elim: false, min: 0 },
    { texte: 'Projection à trois ans', imp: 1, elim: false, min: 0 } ] }
};

/* Notes déjà saisies (par candidat et par niveau) pour la démonstration */
var NOTES_DEMO = {
  1: { 105: [4, 4, 5, 4], 106: [4, 4, 4, 4], 107: [5, 4, 5, 4], 108: [4, 4, 3, 4], 109: [5, 5, 5, 2], 112: [4, 4, 4, 5], 113: [4, 4, 3, 4] },
  2: { 107: [5, 4, 4, 5, 4], 108: [4, 4, 4, 3, 5], 112: [4, 4, 4, 4, 4], 113: [4, 4, 4, 3, 4] },
  3: { 107: [5, 5, 4, 4], 108: [4, 4, 4, 4], 112: [4, 4, 4, 5], 113: [4, 4, 4, 3] }
};

/* Créneaux proposés au candidat */
var CRENEAUX = [
  { jour: 'Lundi 19 octobre', heure: '09:00' }, { jour: 'Lundi 19 octobre', heure: '14:30' },
  { jour: 'Mardi 20 octobre', heure: '10:00' }, { jour: 'Mardi 20 octobre', heure: '15:00' },
  { jour: 'Mercredi 21 octobre', heure: '08:30' }, { jour: 'Jeudi 22 octobre', heure: '11:00' }
];

var UTILISATEURS = [
  { nom: 'Hanta Ravelo', mail: 'h.ravelo@entreprise.mg', role: 'RH', service: 'Ressources humaines', actif: true },
  { nom: 'Mamy Andriamanga', mail: 'm.andriamanga@entreprise.mg', role: 'Technicien', service: 'Direction informatique', actif: true },
  { nom: 'Lucie Rakotondrabe', mail: 'l.rakotondrabe@entreprise.mg', role: 'DG', service: 'Direction générale', actif: true },
  { nom: 'Faly Randrianasolo', mail: 'f.randrianasolo@entreprise.mg', role: 'Direction responsable', service: 'Direction informatique', actif: true },
  { nom: 'Noro Rabemanana', mail: 'n.rabemanana@entreprise.mg', role: 'Direction responsable', service: 'Direction financière', actif: false }
];

function trouverOffre(id) {
  for (var i = 0; i < OFFRES.length; i++) { if (OFFRES[i].id == id) { return OFFRES[i]; } }
  return null;
}
function trouverCandidat(id) {
  for (var i = 0; i < CANDIDATS.length; i++) { if (CANDIDATS[i].id == id) { return CANDIDATS[i]; } }
  return null;
}

/* Calcul d'une note d'entretien : moyenne pondérée par l'importance + questions éliminatoires */
function calculerResultat(notes, grille) {
  var somme = 0, poids = 0, saisies = 0, echecs = [];
  for (var i = 0; i < grille.questions.length; i++) {
    var q = grille.questions[i];
    var n = notes[i];
    if (n !== null && n !== undefined) {
      somme += n * q.imp;
      poids += q.imp;
      saisies++;
      if (q.elim && n < q.min) { echecs.push(i + 1); }
    }
  }
  var moyenne = poids > 0 ? somme / poids : null;
  return {
    moyenne: moyenne,
    complet: saisies === grille.questions.length,
    echecs: echecs,
    propose: moyenne !== null && moyenne >= grille.seuil && echecs.length === 0
  };
}

/* Les notes générales des candidats sont calculées à partir des notes par question */
for (var niveau = 1; niveau <= 3; niveau++) {
  for (var idCand in NOTES_DEMO[niveau]) {
    var cand = trouverCandidat(idCand);
    if (cand) {
      cand.notes[niveau - 1] = calculerResultat(NOTES_DEMO[niveau][idCand], GRILLES[niveau]).moyenne;
    }
  }
}

/* Note finale : moyenne du test (ramené sur 5) et des trois entretiens */
function noteFinale(c) {
  return (c.test / 4 + c.notes[0] + c.notes[1] + c.notes[2]) / 4;
}
