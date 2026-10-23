<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout titre="Accueil" actif="accueil">

  <div class="page-head">
    <div>
      <h1>Bienvenue, <c:out value="${login}"/></h1>
      <p>Rôle : <strong><c:out value="${roleLibelle}"/></strong></p>
    </div>
  </div>

  <div class="card stack">
    <h2>Votre espace</h2>

    <c:if test="${pageContext.request.isUserInRole('EMPLOYE')}">
      <p>Vous pouvez créer et suivre vos demandes de besoin.</p>
    </c:if>
    <c:if test="${pageContext.request.isUserInRole('RESPONSABLE')}">
      <p>Vous validez les demandes de votre département.</p>
    </c:if>
    <c:if test="${pageContext.request.isUserInRole('RH')}">
      <p>Vous vérifiez les demandes de besoin en personnel.</p>
      <div class="row wrap">
        <a class="btn btn-primary" href="<c:url value='/rh/demandes'/>">Voir les demandes à vérifier</a>
        <a class="btn" href="<c:url value='/rh/tests'/>">Voir les candidatures et tests</a>
      </div>
    </c:if>
    <c:if test="${pageContext.request.isUserInRole('FINANCE')}">
      <p>Vous vérifiez le budget des demandes.</p>
    </c:if>
    <c:if test="${pageContext.request.isUserInRole('DG')}">
      <p>Vous prenez la décision finale sur les demandes.</p>
    </c:if>
    <c:if test="${pageContext.request.isUserInRole('CANDIDAT')}">
      <p>Vous pouvez consulter votre candidature et accéder au test technique associé.</p>
      <div class="row wrap">
        <a class="btn btn-primary" href="<c:url value='/candidat'/>">Voir mon espace candidat</a>
      </div>
    </c:if>
  </div>

  <c:if test="${not pageContext.request.isUserInRole('CANDIDAT')}">
    <div class="card stack" style="margin-top:16px">
      <h2>Demande de besoin en personnel</h2>
      <p class="muted">Tout employé peut demander un recrutement. Vous suivez ici l'avancement de chaque demande.</p>
      <div class="row wrap">
        <a class="btn btn-primary" href="<c:url value='/demandes/nouvelle'/>">Faire une demande</a>
        <a class="btn" href="<c:url value='/demandes'/>">Suivre mes demandes</a>
      </div>
    </div>
  </c:if>

</t:layout>
