<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout titre="Historique" actif="historique-fin">

  <div class="page-head">
    <div><h1>Historique des vérifications</h1><p>Les demandes que vous avez traitées : validées, renvoyées ou rejetées.</p></div>
  </div>

  <c:choose>
    <c:when test="${empty lignes}">
      <div class="card empty">Vous n'avez encore traité aucune demande.</div>
    </c:when>
    <c:otherwise>
      <div class="card card-flat">
        <div class="table-wrap"><table>
          <thead><tr><th>N°</th><th>Poste</th><th>Département</th><th>Demandeur</th><th>Contrat</th><th>Salaire</th><th>Votre décision</th><th>Commentaire</th><th>Décidée le</th><th>Statut actuel</th></tr></thead>
          <tbody>
            <c:forEach var="l" items="${lignes}">
              <tr>
                <td><b><c:out value="${l.numero}"/></b></td>
                <td><c:out value="${l.poste}"/></td>
                <td><c:out value="${l.departement}"/></td>
                <td><c:out value="${l.demandeur}"/></td>
                <td><c:out value="${l.contrat}"/></td>
                <td><c:out value="${l.salaireFr}"/></td>
                <td><span class="badge ${l.decisionBadge}"><c:out value="${l.decisionLibelle}"/></span></td>
                <td><c:choose><c:when test="${empty l.commentaire}"><span class="muted">—</span></c:when><c:otherwise><c:out value="${l.commentaire}"/></c:otherwise></c:choose></td>
                <td><c:out value="${l.dateDecisionFr}"/></td>
                <td><span class="badge ${l.statutBadge}"><c:out value="${l.statutActuel}"/></span></td>
              </tr>
            </c:forEach>
          </tbody>
        </table></div>
      </div>
    </c:otherwise>
  </c:choose>

</t:layout>
