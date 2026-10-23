<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Salariés" actif="salaries">
  <div class="page-head"><div><h1>Salariés</h1><p>Dossiers individuels, contrats, affectations et formalités.</p></div><a class="btn" href="<c:url value='/rh/contrats/nouveau'/>">Créer un contrat</a></div>
  <f:flash/>
  <form class="filter-bar" method="get" action="<c:url value='/rh/salaries'/>">
    <div class="field grow"><label for="recherche">Rechercher</label><input id="recherche" type="text" name="recherche" value="<c:out value='${recherche}'/>" placeholder="Nom, matricule, email…"></div>
    <div class="field"><label for="idDepartement">Département</label><select id="idDepartement" name="idDepartement"><option value="">Tous</option><c:forEach var="d" items="${departements}"><option value="${d.id}" ${idDepartement == d.id ? 'selected' : ''}><c:out value="${d.designation}"/></option></c:forEach></select></div>
    <button class="btn" type="submit">Filtrer</button><a class="btn" href="<c:url value='/rh/salaries'/>">Réinitialiser</a>
  </form>
  <c:choose>
    <c:when test="${empty salaries.content}"><div class="card empty-state"><b>Aucun salarié</b>Aucun dossier ne correspond à ces critères.</div></c:when>
    <c:otherwise>
      <div class="card card-flat"><div class="table-wrap"><table><thead><tr><th>Salarié</th><th>Matricule</th><th>Poste</th><th>Département</th><th>Embauche</th><th>Statut</th><th></th></tr></thead><tbody>
        <c:forEach var="s" items="${salaries.content}">
          <tr><td><div class="person"><div class="avatar"><c:out value="${s.initiales}"/></div><div><b><c:out value="${s.nomComplet}"/></b><br><span class="muted small"><c:out value="${s.email}"/></span></div></div></td><td><c:out value="${s.matricule}"/></td><td><c:out value="${s.poste}"/></td><td><c:out value="${s.departement}"/></td><td><c:out value="${s.dateEmbaucheFr}"/></td><td><span class="badge <c:out value='${s.statutBadge}'/>"><c:out value="${s.statutLibelle}"/></span></td><td class="right"><a class="btn btn-sm" href="<c:url value='/rh/salaries/${s.id}'/>">Dossier</a></td></tr>
        </c:forEach>
      </tbody></table></div></div>
      <div class="pager">
        <c:if test="${not salaries.first}"><a class="btn btn-sm" href="<c:url value='/rh/salaries'><c:param name='page' value='${salaries.number - 1}'/><c:param name='recherche' value='${recherche}'/><c:param name='idDepartement' value='${idDepartement}'/></c:url>">Précédent</a></c:if>
        <span>Page ${salaries.number + 1} / ${salaries.totalPages == 0 ? 1 : salaries.totalPages} — ${salaries.totalElements} salarié(s)</span>
        <c:if test="${not salaries.last}"><a class="btn btn-sm" href="<c:url value='/rh/salaries'><c:param name='page' value='${salaries.number + 1}'/><c:param name='recherche' value='${recherche}'/><c:param name='idDepartement' value='${idDepartement}'/></c:url>">Suivant</a></c:if>
      </div>
    </c:otherwise>
  </c:choose>
</t:layout>
