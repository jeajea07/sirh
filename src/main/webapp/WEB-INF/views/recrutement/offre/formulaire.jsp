<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:include page="/WEB-INF/views/recrutement/fragments/entete.jsp"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="edition" value="${not empty offre}"/>
<c:set var="urlRetour" value="${ctx}/recrutement/offres"/>
<c:set var="urlAction" value="${ctx}/recrutement/offres"/>
<c:if test="${edition}">
  <c:set var="urlRetour" value="${ctx}/recrutement/offres/${offre.idOffre}"/>
  <c:set var="urlAction" value="${ctx}/recrutement/offres/${offre.idOffre}/modifier"/>
</c:if>

<div class="barre">
  <h1><c:out value="${titrePage}"/></h1>
  <a class="btn sec" href="${urlRetour}">Retour</a>
</div>

<div class="carte">
<form:form modelAttribute="offreForm" method="post"
           action="${urlAction}">

  <label>Titre de l'offre</label>
  <form:input path="titre" maxlength="200"/>
  <form:errors path="titre" cssClass="err"/>

  <label>Description</label>
  <form:textarea path="description" rows="4"/>

  <label>Missions</label>
  <form:textarea path="missions" rows="4"/>

  <div class="grille">
    <div>
      <label>Salaire propose (non negociable)</label>
      <form:input path="salairePropose" type="number" step="0.01" min="0"/>
      <form:errors path="salairePropose" cssClass="err"/>
    </div>
    <div>
      <label>Nombre de postes</label>
      <form:input path="nombrePostes" type="number" min="1"/>
      <form:errors path="nombrePostes" cssClass="err"/>
    </div>
    <div>
      <label>Duree du contrat (mois, facultatif)</label>
      <form:input path="dureeMois" type="number" min="0"/>
      <form:errors path="dureeMois" cssClass="err"/>
    </div>
    <div>
      <label>Date limite de candidature</label>
      <form:input path="dateLimite" type="date"/>
      <form:errors path="dateLimite" cssClass="err"/>
    </div>
  </div>

  <c:choose>
    <c:when test="${edition}">
      <p class="alert warn">Demande n&deg;<c:out value="${offre.idDemandeBesoin}"/> &middot; poste <c:out value="${offre.idPoste}"/> &middot; département <c:out value="${offre.idDepartement}"/> &middot; contrat <c:out value="${offre.idTypeContrat}"/>
        (fixes : repris de la demande acceptee).</p>
    </c:when>
    <c:otherwise><p class="alert warn">La création se fait depuis une demande de besoin acceptée.</p></c:otherwise>
  </c:choose>

  <p><button class="btn" type="submit">Enregistrer</button></p>
</form:form>
</div>

<jsp:include page="/WEB-INF/views/recrutement/fragments/pied.jsp"/>
