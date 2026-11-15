<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Plannings" actif="planning">
  <div class="page-head"><div><h1>Plannings</h1><p>Le manager prépare, le RH valide, la Finance valide s'il y a des heures majorées, la DG arbitre un dépassement de budget.</p></div></div>
  <f:flash/>
  <c:if test="${not empty aValider}">
    <div class="card" style="margin-bottom:16px"><div class="card-head"><h2>À valider (<c:out value="${aValider.size()}"/>)</h2></div>
      <div class="table-wrap"><table><thead><tr><th>Référence</th><th>Département</th><th>Période</th><th>Heures majorées</th><th></th></tr></thead><tbody>
        <c:forEach var="p" items="${aValider}"><tr><td><b><c:out value="${p.reference}"/></b> v<c:out value="${p.version}"/></td><td><c:out value="${p.departement.designation}"/></td><td><c:out value="${p.periodeLibelle}"/></td><td><c:out value="${p.impactBudgetaire ? p.coutFr : 'aucune'}"/></td><td class="right"><a class="btn btn-sm btn-primary" href="<c:url value='/planning/${p.id}'/>">Examiner</a></td></tr></c:forEach>
      </tbody></table></div>
    </div>
  </c:if>
  <c:if test="${not empty departements}">
    <form method="post" action="<c:url value='/planning'/>" class="card filter-bar" style="margin-bottom:16px">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
      <div class="field grow"><label for="idDepartement">Département</label><select id="idDepartement" name="idDepartement" required><c:forEach var="d" items="${departements}"><option value="${d.id}"><c:out value="${d.designation}"/></option></c:forEach></select></div>
      <div class="field"><label for="debut">Du</label><input id="debut" type="date" name="debut" value="${debutPropose}" required></div>
      <div class="field"><label for="fin">Au</label><input id="fin" type="date" name="fin" value="${finPropose}" required></div>
      <button class="btn btn-primary" type="submit">Préparer un planning</button>
    </form>
  </c:if>
  <c:choose>
    <c:when test="${empty plannings}"><div class="card empty-state"><b>Aucun planning</b>Sans planning publié, le temps prévu vient du régime de travail de chaque salarié.</div></c:when>
    <c:otherwise>
      <div class="card card-flat"><div class="table-wrap"><table><thead><tr><th>Référence</th><th>Département</th><th>Période</th><th>Auteur</th><th>Heures majorées</th><th>Statut</th><th></th></tr></thead><tbody>
        <c:forEach var="p" items="${plannings}"><tr><td><b><c:out value="${p.reference}"/></b> v<c:out value="${p.version}"/></td><td><c:out value="${p.departement.designation}"/></td><td><c:out value="${p.periodeLibelle}"/></td><td><c:out value="${p.auteur.nomComplet}"/></td><td><c:out value="${p.impactBudgetaire ? p.coutFr : '—'}"/></td><td><span class="badge <c:out value='${p.statutBadge}'/>"><c:out value="${p.statutLibelle}"/></span></td><td class="right"><a class="btn btn-sm" href="<c:url value='/planning/${p.id}'/>">Ouvrir</a></td></tr></c:forEach>
      </tbody></table></div></div>
    </c:otherwise>
  </c:choose>
</t:layout>
