<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  SIDEBAR — barre latérale des pages connectées (adaptée de teamup-rh/layouts/sidebar.html).
  Paramètre : actif = lien à mettre en surbrillance.
  Variables du modèle (LayoutAdvice) : role, roleCode, roleLibelle, login, initiales.
--%>
<aside class="sidebar">
  <a class="brand" href="<c:url value='/accueil'/>"><img src="<c:url value='/logo.svg'/>" alt="" width="40" height="30"><span class="brand-text">Team<b>Up</b></span></a>
  <div class="role-tag"><i></i><c:out value="${roleLibelle}"/></div>

  <nav class="nav">
    <a class="${param.actif == 'accueil' ? 'active' : ''}" href="<c:url value='/accueil'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 11l9-8 9 8"/><path d="M5 10v10h14V10"/></svg>Accueil</a>
    <a class="${param.actif == 'nouvelle' ? 'active' : ''}" href="<c:url value='/demandes/nouvelle'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14"/></svg>Nouvelle demande</a>
    <a class="${param.actif == 'demandes' ? 'active' : ''}" href="<c:url value='/demandes'/>"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><rect x="5" y="4" width="14" height="17" rx="2"/><path d="M9 4h6v3H9zM9 12h6M9 16h4"/></svg>Mes demandes</a>

    <%--
      Ajouter un lien : copier la ligne ci-dessus, changer l'URL, le libellé et la valeur testée de param.actif
      (la même valeur que celle passée par <t:layout actif="..."> dans la page).
      Pour réserver un lien à un rôle, l'entourer de :
          <c:if test="${role == 'RH'}"> ... </c:if>
      Rôles possibles : EMPLOYE, RESPONSABLE, RH, FINANCE, DG.
      Exemple :
      <c:if test="${role == 'RH'}">
        <a class="${param.actif == 'demandes-rh' ? 'active' : ''}" href="<c:url value='/rh/demandes'/>">Demandes à vérifier</a>
      </c:if>
    --%>
  </nav>

  <div class="side-foot">
    <div class="side-user">
      <div class="avatar"><c:out value="${initiales}"/></div>
      <div><b><c:out value="${login}"/></b><span><c:out value="${roleLibelle}"/></span></div>
    </div>
    <%-- Déconnexion en POST avec le jeton CSRF --%>
    <form method="post" action="<c:url value='/deconnexion'/>">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
      <button class="side-link" type="submit"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M9 4H5a2 2 0 00-2 2v12a2 2 0 002 2h4M16 8l4 4-4 4M20 12H9"/></svg>Se déconnecter</button>
    </form>
  </div>
</aside>
