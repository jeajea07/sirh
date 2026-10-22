<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout titre="Validation finale" actif="demandes-dg">
  <div class="page-head"><div><h1>Validation finale — Direction générale</h1><p>Demandes contrôlées par les Ressources humaines et la Finance, en attente de votre décision.</p></div></div>
  <c:if test="${not empty succes}"><div class="alert alert-ok" style="margin-bottom:16px"><c:out value="${succes}"/></div></c:if>
  <c:if test="${not empty erreur}"><div class="alert alert-bad" style="margin-bottom:16px"><c:out value="${erreur}"/></div></c:if>
  <c:choose>
    <c:when test="${empty demandes}"><div class="card empty">Aucune demande n'attend votre décision.</div></c:when>
    <c:otherwise>
      <div class="card card-flat"><div class="table-wrap"><table>
        <thead><tr><th>N°</th><th>Poste</th><th>Département</th><th>Demandeur</th><th>Motif</th><th>Contrat</th><th>Postes</th><th>Salaire mensuel</th><th>Date souhaitée</th><th>Statut</th><th></th></tr></thead>
        <tbody><c:forEach var="ligne" items="${demandes}"><c:set var="d" value="${ligne.demande}"/><tr>
          <td><b><c:out value="${d.numero}"/></b></td><td><c:out value="${d.poste.designation}"/></td>
          <td><c:out value="${d.departement.designation}"/></td>
          <td><c:out value="${ligne.demandeur}"/></td><td><c:out value="${d.motifLibelle}"/></td>
          <td><c:out value="${d.typeContrat.code}"/></td><td><c:out value="${d.nombrePostes}"/></td>
          <td><c:out value="${d.salaireFr}"/></td><td><c:out value="${d.dateSouhaiteeFr}"/></td>
          <td><span class="badge ${d.statutBadge}"><c:out value="${d.statutLibelle}"/></span></td>
          <td class="right"><a class="btn btn-sm btn-primary" href="<c:url value='/dg/demandes/${d.id}'/>">Examiner</a></td>
        </tr></c:forEach></tbody>
      </table></div></div>
    </c:otherwise>
  </c:choose>
</t:layout>
