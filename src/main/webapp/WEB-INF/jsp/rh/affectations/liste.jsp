<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Affectations" actif="affectations">
  <div class="page-head"><div><h1>Affectations</h1><p>Poste, département et responsable de chaque salarié.</p></div><a class="btn btn-primary" href="<c:url value='/rh/affectations/nouveau'/>">Nouvelle affectation</a></div>
  <f:flash/>
  <form class="filter-bar" method="get" action="<c:url value='/rh/affectations'/>">
    <div class="field grow"><label for="idEmploye">Salarié</label><select id="idEmploye" name="idEmploye"><option value="">Tous les salariés</option><c:forEach var="e" items="${employes}"><option value="${e.id}" ${idEmploye == e.id ? 'selected' : ''}><c:out value="${e.nomComplet}"/> — <c:out value="${e.matricule}"/></option></c:forEach></select></div>
    <button class="btn" type="submit">Filtrer</button><a class="btn" href="<c:url value='/rh/affectations'/>">Réinitialiser</a>
  </form>
  <c:choose>
    <c:when test="${empty affectations}"><div class="card empty-state"><b>Aucune affectation</b>Aucune affectation ne correspond à ce filtre.</div></c:when>
    <c:otherwise>
      <div class="card card-flat"><div class="table-wrap"><table><thead><tr><th>Salarié</th><th>Poste</th><th>Département</th><th>Responsable</th><th>Période</th><th>Statut</th><th></th></tr></thead><tbody>
        <c:forEach var="a" items="${affectations}"><tr><td><b><c:out value="${a.employeNom}"/></b></td><td><c:out value="${a.poste}"/></td><td><c:out value="${a.departement}"/></td><td><c:out value="${a.responsableNom}"/></td><td><c:out value="${a.periodeLibelle}"/></td><td><span class="badge <c:out value='${a.statutBadge}'/>"><c:out value="${a.statutLibelle}"/></span></td><td class="right"><a class="btn btn-sm" href="<c:url value='/rh/affectations/${a.id}'/>">Ouvrir</a></td></tr></c:forEach>
      </tbody></table></div></div>
    </c:otherwise>
  </c:choose>
</t:layout>
