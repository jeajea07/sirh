<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Affectation" actif="affectations">
  <div class="page-head"><div><h1>Affectation</h1><p><c:out value="${affectation.employeNom}"/> — <c:out value="${affectation.poste}"/></p></div><a class="btn" href="<c:url value='/rh/affectations'/>">Retour</a></div>
  <f:flash/>
  <div class="detail-grid">
    <div class="card"><div class="card-head"><h2>Détails</h2><span class="badge <c:out value='${affectation.statutBadge}'/>"><c:out value="${affectation.statutLibelle}"/></span></div>
      <dl class="meta-list">
        <div><dt>Salarié</dt><dd><a href="<c:url value='/rh/salaries/${affectation.idEmploye}'/>"><c:out value="${affectation.employeNom}"/></a></dd></div>
        <div><dt>Poste</dt><dd><c:out value="${affectation.poste}"/></dd></div>
        <div><dt>Département</dt><dd><c:out value="${affectation.departement}"/></dd></div>
        <div><dt>Responsable</dt><dd><c:out value="${affectation.responsableNom}"/></dd></div>
        <div><dt>Site</dt><dd><c:out value="${affectation.site}"/></dd></div>
        <div><dt>Période</dt><dd><c:out value="${affectation.periodeLibelle}"/></dd></div>
      </dl>
      <c:if test="${not empty affectation.motif}"><p class="muted" style="margin-top:14px;white-space:pre-line"><c:out value="${affectation.motif}"/></p></c:if>
    </div>
    <div class="card"><div class="card-head"><h2>Clôturer</h2></div>
      <c:choose>
        <c:when test="${affectation.active}"><form method="post" action="<c:url value='/rh/affectations/${affectation.id}/cloturer'/>" class="stack"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><div class="field"><label for="dateFin">Date de fin *</label><input id="dateFin" type="date" name="dateFin" required></div><button class="btn btn-danger btn-block" type="submit">Clôturer cette affectation</button></form></c:when>
        <c:otherwise><p class="muted">Cette affectation est déjà terminée.</p></c:otherwise>
      </c:choose>
    </div>
  </div>
</t:layout>
