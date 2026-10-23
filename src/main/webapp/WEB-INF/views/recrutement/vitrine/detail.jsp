<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout titre="${offre.titre}" actif="offres-public">
  <div class="page-head">
    <div>
      <p class="muted"><c:out value="${offre.reference}"/> · Date limite : <c:out value="${offre.dateLimite}"/></p>
      <h1 style="margin-top:5px"><c:out value="${offre.titre}"/></h1>
    </div>
    <a class="btn" href="<c:url value='/offres'/>">Retour aux offres</a>
  </div>

  <div class="grid g2" style="align-items:start">
    <section class="card stack">
      <div><h2>Description du poste</h2><p style="margin-top:8px;white-space:pre-line"><c:out value="${offre.description}"/></p></div>
      <div><h2>Missions</h2><p style="margin-top:8px;white-space:pre-line"><c:out value="${offre.missions}"/></p></div>
      <div><h2>Salaire proposé</h2><p style="margin-top:8px"><c:out value="${offre.salaire}"/></p></div>
    </section>
    <aside class="card stack">
      <div><h2>Critères du poste</h2>
        <c:choose><c:when test="${empty offre.criteres}"><p class="muted" style="margin-top:8px">Les détails sont indiqués dans la description.</p></c:when><c:otherwise>
          <ul style="padding-left:20px;margin-top:8px">
            <c:forEach items="${offre.criteres}" var="critere"><li><c:if test="${critere.obligatoire}"><b>Obligatoire — </b></c:if><c:out value="${critere.nature}"/> : <c:out value="${critere.libelle}"/><c:if test="${not empty critere.valeurMin}"> — minimum <c:out value="${critere.valeurMin}"/></c:if></li></c:forEach>
          </ul>
        </c:otherwise></c:choose>
      </div>
      <c:url var="postulerUrl" value="/candidatures/nouvelle"><c:param name="offreId" value="${offre.id}"/></c:url>
      <a class="btn btn-primary" href="${postulerUrl}">Postuler à cette offre</a>
    </aside>
  </div>
</t:layout>
