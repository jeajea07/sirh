<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<aside class="sidebar">
  <a class="brand" href="<c:url value='/accueil'/>"><img src="<c:url value='/logo.svg'/>" alt="" width="40" height="30"><span class="brand-text">Team<b>Up</b></span></a>
  <div class="role-tag"><i></i><c:out value="${roleLibelle}"/></div>

  <nav class="nav">
    <a class="${param.actif == 'accueil' ? 'active' : ''}" href="<c:url value='/accueil'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 11l9-8 9 8"/><path d="M5 10v10h14V10"/></svg>Accueil</a>
    <a class="${param.actif == 'nouvelle' ? 'active' : ''}" href="<c:url value='/demandes/nouvelle'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14"/></svg>Nouvelle demande</a>
    <a class="${param.actif == 'demandes' ? 'active' : ''}" href="<c:url value='/demandes'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><rect x="5" y="4" width="14" height="17" rx="2"/><path d="M9 4h6v3H9zM9 12h6M9 16h4"/></svg>Mes demandes</a>

    <c:if test="${role == 'RH'}">
      <a class="${param.actif == 'demandes-rh' ? 'active' : ''}" href="<c:url value='/rh/demandes'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M9 11l3 3 8-8"/><path d="M20 12v7a2 2 0 01-2 2H6a2 2 0 01-2-2V6a2 2 0 012-2h9"/></svg>Demandes à vérifier</a>
      <a class="${param.actif == 'tests-rh' ? 'active' : ''}" href="<c:url value='/rh/tests'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M7 3h10a2 2 0 012 2v14l-4-3-4 3-4-3-4 3V5a2 2 0 012-2z"/><path d="M8 8h8M8 12h8"/></svg>Candidatures / Tests</a>
      <a class="${param.actif == 'historique-rh' ? 'active' : ''}" href="<c:url value='/rh/demandes/historique'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 12a9 9 0 109-9 9 9 0 00-6.4 2.6L3 8"/><path d="M3 3v5h5M12 7v5l3 2"/></svg>Historique</a>
    </c:if>

    <c:if test="${role == 'FINANCE'}">
      <a class="${param.actif == 'demandes-fin' ? 'active' : ''}" href="<c:url value='/finance/demandes'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M9 11l3 3 8-8"/><path d="M20 12v7a2 2 0 01-2 2H6a2 2 0 01-2-2V6a2 2 0 012-2h9"/></svg>Demandes à vérifier</a>
      <a class="${param.actif == 'historique-fin' ? 'active' : ''}" href="<c:url value='/finance/demandes/historique'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 12a9 9 0 109-9 9 9 0 00-6.4 2.6L3 8"/><path d="M3 3v5h5M12 7v5l3 2"/></svg>Historique</a>
    </c:if>

    <c:if test="${role == 'DG'}">
      <a class="${param.actif == 'demandes-dg' ? 'active' : ''}" href="<c:url value='/dg/demandes'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M9 11l3 3 8-8"/><path d="M20 12v7a2 2 0 01-2 2H6a2 2 0 01-2-2V6a2 2 0 012-2h9"/></svg>Validation finale</a>
      <a class="${param.actif == 'historique-dg' ? 'active' : ''}" href="<c:url value='/dg/demandes/historique'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 12a9 9 0 109-9 9 9 0 00-6.4 2.6L3 8"/><path d="M3 3v5h5M12 7v5l3 2"/></svg>Historique</a>
    </c:if>

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
