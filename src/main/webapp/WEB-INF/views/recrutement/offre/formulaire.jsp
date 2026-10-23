<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout titre="${titrePage}" actif="offres-rh">
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="edition" value="${not empty offre}"/>
<c:set var="urlRetour" value="${ctx}/recrutement/offres"/>
<c:set var="urlAction" value="${ctx}/recrutement/offres"/>
<c:if test="${edition}">
  <c:set var="urlRetour" value="${ctx}/recrutement/offres/${offre.idOffre}"/>
  <c:set var="urlAction" value="${ctx}/recrutement/offres/${offre.idOffre}/modifier"/>
</c:if>

<div class="page-head">
  <h1><c:out value="${titrePage}"/></h1>
  <a class="btn" href="${urlRetour}">Retour</a>
</div>

<div class="card" style="max-width:900px">
<form:form modelAttribute="offreForm" method="post"
           action="${urlAction}" cssClass="stack" cssStyle="max-width:940px">

  <div class="field"><label for="titre">Titre de l'offre</label>
  <form:input path="titre" id="titre" maxlength="200"/>
  <form:errors path="titre" cssClass="err"/></div>

  <div class="field"><label for="description">Description</label>
  <form:textarea path="description" id="description" rows="4"/></div>

  <div class="field"><label for="missions">Missions</label>
  <form:textarea path="missions" id="missions" rows="4"/></div>

  <div class="grid g2">
    <div class="field">
      <label for="salairePropose">Salaire proposé (non négociable)</label>
      <form:input path="salairePropose" id="salairePropose" type="number" step="0.01" min="0"/>
      <form:errors path="salairePropose" cssClass="err"/>
    </div>
    <div class="field">
      <label for="nombrePostes">Nombre de postes</label>
      <form:input path="nombrePostes" id="nombrePostes" type="number" min="1"/>
      <form:errors path="nombrePostes" cssClass="err"/>
    </div>
    <div class="field">
      <label for="dureeMois">Durée du contrat (mois, facultatif)</label>
      <form:input path="dureeMois" id="dureeMois" type="number" min="0"/>
      <form:errors path="dureeMois" cssClass="err"/>
    </div>
    <div class="field">
      <label for="dateLimite">Date limite de candidature</label>
      <form:input path="dateLimite" id="dateLimite" type="date"/>
      <form:errors path="dateLimite" cssClass="err"/>
    </div>
  </div>

  <c:choose>
    <c:when test="${edition}">
      <p class="alert alert-info">Demande n&deg;<c:out value="${offre.idDemandeBesoin}"/> &middot; poste <c:out value="${offre.idPoste}"/> &middot; département <c:out value="${offre.idDepartement}"/> &middot; contrat <c:out value="${offre.idTypeContrat}"/>
        (fixes : repris de la demande acceptee).</p>
    </c:when>
    <c:otherwise><p class="alert alert-warn">La création se fait depuis une demande de besoin acceptée.</p></c:otherwise>
  </c:choose>

  <p class="row wrap"><button class="btn btn-primary" type="submit">Enregistrer</button><a class="btn" href="${urlRetour}">Annuler</a></p>
</form:form>
</div>

</t:layout>
