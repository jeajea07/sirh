<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<aside class="sidebar">
  <a class="brand" href="<c:url value='/accueil'/>"><img src="<c:url value='/logo.svg'/>" alt="" width="40" height="30"><span class="brand-text">Team<b>Up</b></span></a>
  <div class="role-tag"><i></i><c:out value="${roleLibelle}"/></div>

  <nav class="nav">
    <c:if test="${role == 'CANDIDAT'}">
      <a class="${param.actif == 'candidat' ? 'active' : ''}" href="<c:url value='/candidat'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="3.5"/><path d="M5 20c0-3.8 3-6 7-6s7 2.2 7 6"/></svg>Mon espace</a>
    </c:if>

    <c:if test="${role != 'CANDIDAT'}">
      <a class="${param.actif == 'accueil' ? 'active' : ''}" href="<c:url value='/accueil'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 11l9-8 9 8"/><path d="M5 10v10h14V10"/></svg>Accueil</a>
      <a class="${param.actif == 'nouvelle' ? 'active' : ''}" href="<c:url value='/demandes/nouvelle'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14"/></svg>Nouvelle demande</a>
      <a class="${param.actif == 'demandes' ? 'active' : ''}" href="<c:url value='/demandes'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><rect x="5" y="4" width="14" height="17" rx="2"/><path d="M9 4h6v3H9zM9 12h6M9 16h4"/></svg>Mes demandes</a>
    </c:if>

    <c:if test="${role == 'RH'}">
      <a class="${param.actif == 'demandes-rh' ? 'active' : ''}" href="<c:url value='/rh/demandes'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M9 11l3 3 8-8"/><path d="M20 12v7a2 2 0 01-2 2H6a2 2 0 01-2-2V6a2 2 0 012-2h9"/></svg>Demandes à vérifier</a>
      <a class="${param.actif == 'historique-rh' ? 'active' : ''}" href="<c:url value='/rh/demandes/historique'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 12a9 9 0 109-9 9 9 0 00-6.4 2.6L3 8"/><path d="M3 3v5h5M12 7v5l3 2"/></svg>Historique</a>

      <div class="nav-section">Recrutement</div>
      <a class="${param.actif == 'offres' ? 'active' : ''}" href="<c:url value='/recrutement/offres'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="7" width="18" height="13" rx="2"/><path d="M9 7V5a2 2 0 012-2h2a2 2 0 012 2v2M3 12h18"/></svg>Offres d'emploi</a>
      <a class="${param.actif == 'candidatures' ? 'active' : ''}" href="<c:url value='/rh/recrutement/candidatures'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M6 3h9l4 4v14H6z"/><path d="M14 3v5h5M9 13h6M9 17h4"/></svg>Suivi des candidatures</a>
      <a class="${param.actif == 'tests-rh' ? 'active' : ''}" href="<c:url value='/rh/tests'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M9 3h6v4l4 10a3 3 0 01-3 4H8a3 3 0 01-3-4l4-10z"/><path d="M7 13h10"/></svg>Tests techniques</a>
      <a class="${param.actif == 'entretiens' ? 'active' : ''}" href="<c:url value='/rh/entretiens'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="8" cy="9" r="3"/><circle cx="16" cy="9" r="3"/><path d="M2 20c0-3 2.7-5 6-5s6 2 6 5M14 15.2c.6-.1 1.3-.2 2-.2 3.3 0 6 2 6 5"/></svg>Entretiens</a>
      <a class="${param.actif == 'bloc3' ? 'active' : ''}" href="<c:url value='/bloc3/selections'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 5h16v14H4z"/><path d="M8 9l2 2 4-4M8 16l2 2 4-4"/></svg>Sélection et embauche</a>

      <div class="nav-section">Pilotage RH</div>
      <a class="${param.actif == 'tableau-rh' ? 'active' : ''}" href="<c:url value='/rh/tableau-de-bord'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>Tableau de bord</a>
      <a class="${param.actif == 'contrats' ? 'active' : ''}" href="<c:url value='/rh/contrats'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M6 3h9l3 3v15H6z"/><path d="M9 9h6M9 13h6M9 17h4"/><path d="M15 3v4h4"/></svg>Contrats</a>
      <a class="${param.actif == 'echeances' ? 'active' : ''}" href="<c:url value='/rh/contrats/echeances'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M8 3v4M16 3v4M3 10h18M8 15h3"/></svg>Échéances</a>
      <a class="${param.actif == 'salaries' ? 'active' : ''}" href="<c:url value='/rh/salaries'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="9" cy="8" r="3.2"/><path d="M3 20c0-3.4 2.7-5.5 6-5.5s6 2.1 6 5.5"/><path d="M17 8h4M19 6v4M17 14h4M17 18h4"/></svg>Salariés</a>
      <a class="${param.actif == 'affectations' ? 'active' : ''}" href="<c:url value='/rh/affectations'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M4 12h10M4 17h7"/><circle cx="18" cy="16" r="3"/><path d="M18 13v-2"/></svg>Affectations</a>
      <a class="${param.actif == 'onboardings' ? 'active' : ''}" href="<c:url value='/rh/onboardings'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M9 11l3 3 8-8"/><path d="M20 12v7a2 2 0 01-2 2H6a2 2 0 01-2-2V6a2 2 0 012-2h9"/></svg>Onboardings</a>
      <a class="${param.actif == 'modeles-onboarding' ? 'active' : ''}" href="<c:url value='/rh/onboardings/modeles'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 5h16v14H4z"/><path d="M8 9h8M8 13h8M8 17h5"/></svg>Modèles</a>
      <a class="${param.actif == 'documents' ? 'active' : ''}" href="<c:url value='/rh/documents'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M7 3h7l4 4v14H7z"/><path d="M14 3v5h4"/><path d="M10 13h5M10 17h5"/></svg>Documents</a>
      <a class="${param.actif == 'formalites' ? 'active' : ''}" href="<c:url value='/rh/formalites'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 4h16v16H4z"/><path d="M8 8h8M8 12h8M8 16h5"/><path d="M16 16l2 2 3-4"/></svg>Formalités</a>

      <div class="nav-section">Temps et congés</div>
      <a class="${param.actif == 'temps-rh' ? 'active' : ''}" href="<c:url value='/rh/temps'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg>Temps de travail</a>
      <a class="${param.actif == 'conges-rh' ? 'active' : ''}" href="<c:url value='/rh/conges'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg>Congés et absences</a>
    </c:if>

    <c:if test="${role == 'FINANCE'}">
      <a class="${param.actif == 'demandes-fin' ? 'active' : ''}" href="<c:url value='/finance/demandes'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M9 11l3 3 8-8"/><path d="M20 12v7a2 2 0 01-2 2H6a2 2 0 01-2-2V6a2 2 0 012-2h9"/></svg>Demandes à vérifier</a>
      <a class="${param.actif == 'historique-fin' ? 'active' : ''}" href="<c:url value='/finance/demandes/historique'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 12a9 9 0 109-9 9 9 0 00-6.4 2.6L3 8"/><path d="M3 3v5h5M12 7v5l3 2"/></svg>Historique</a>
    </c:if>

    <c:if test="${role == 'DG'}">
      <a class="${param.actif == 'demandes-dg' ? 'active' : ''}" href="<c:url value='/dg/demandes'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M9 11l3 3 8-8"/><path d="M20 12v7a2 2 0 01-2 2H6a2 2 0 01-2-2V6a2 2 0 012-2h9"/></svg>Validation finale</a>
      <a class="${param.actif == 'historique-dg' ? 'active' : ''}" href="<c:url value='/dg/demandes/historique'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 12a9 9 0 109-9 9 9 0 00-6.4 2.6L3 8"/><path d="M3 3v5h5M12 7v5l3 2"/></svg>Historique</a>
    </c:if>

      <c:if test="${role != 'CANDIDAT'}">
      <div class="nav-section">Mon espace</div>
      <a class="${param.actif == 'pointage' ? 'active' : ''}" href="<c:url value='/pointage'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg>Pointage</a>
      <a class="${param.actif == 'conges' ? 'active' : ''}" href="<c:url value='/conges'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg>Mes congés</a>
      <c:if test="${estManager or role == 'RH' or role == 'FINANCE' or role == 'DG'}"><a class="${param.actif == 'planning' ? 'active' : ''}" href="<c:url value='/planning'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M3 10h18M8 3v4M16 3v4"/></svg>Plannings<c:if test="${nbPlanningsAValider > 0}"><span class="nav-badge"><c:out value="${nbPlanningsAValider}"/></span></c:if></a></c:if>
      <c:if test="${estManager}"><a class="${param.actif == 'equipe' ? 'active' : ''}" href="<c:url value='/equipe'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="8" cy="9" r="3"/><circle cx="16" cy="9" r="3"/><path d="M2 20c0-3 2.7-5 6-5s6 2 6 5M14 15c3.3 0 8 1 8 5"/></svg>Mon équipe</a></c:if>
      </c:if>
      <c:if test="${role != 'CANDIDAT'}"><a class="${param.actif == 'mon-dossier' ? 'active' : ''}" href="<c:url value='/mon-dossier'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 6a2 2 0 012-2h4l2 2h6a2 2 0 012 2v10a2 2 0 01-2 2H6a2 2 0 01-2-2z"/><circle cx="12" cy="12" r="2.2"/><path d="M8.5 17c.6-1.6 2-2.4 3.5-2.4s2.9.8 3.5 2.4"/></svg>Mon dossier</a></c:if>
      <a class="${param.actif == 'mes-taches' ? 'active' : ''}" href="<c:url value='/mes-taches'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M9 11l3 3 8-8"/><path d="M20 12v7a2 2 0 01-2 2H6a2 2 0 01-2-2V6a2 2 0 012-2h9"/></svg>Mes tâches<c:if test="${nbTaches > 0}"><span class="nav-badge"><c:out value="${nbTaches}"/></span></c:if></a>
    </nav>

  <div class="side-foot">
    <div class="side-user">
      <div class="avatar"><c:out value="${initiales}"/></div>
      <div><b><c:out value="${login}"/></b><span><c:out value="${roleLibelle}"/></span></div>
    </div>
    <form method="post" action="<c:url value='/deconnexion'/>">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
      <button class="side-link" type="submit"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M9 4H5a2 2 0 00-2 2v12a2 2 0 002 2h4M16 8l4 4-4 4M20 12H9"/></svg>Se déconnecter</button>
    </form>
  </div>
</aside>
