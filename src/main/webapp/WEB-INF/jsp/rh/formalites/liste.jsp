<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Formalités sociales" actif="formalites">
  <div class="page-head">
    <div><h1>Formalités sociales</h1><p>CNaPS, OSTIE, fiscalité et santé au travail : préparation, transmission et confirmation.</p></div>
    <a class="btn btn-primary" href="<c:url value='/rh/formalites/nouvelle'/>">Nouvelle formalité</a>
  </div>
  <f:flash/>

  <form class="filter-bar" method="get" action="<c:url value='/rh/formalites'/>">
    <div class="field"><label for="statut">Statut</label>
      <select id="statut" name="statut">
        <option value="">Tous</option>
        <option value="A_PREPARER" ${statut == 'A_PREPARER' ? 'selected' : ''}>À préparer</option>
        <option value="PREPAREE" ${statut == 'PREPAREE' ? 'selected' : ''}>Préparée</option>
        <option value="TRANSMISE" ${statut == 'TRANSMISE' ? 'selected' : ''}>Transmise</option>
        <option value="CONFIRMEE" ${statut == 'CONFIRMEE' ? 'selected' : ''}>Confirmée</option>
        <option value="REJETEE" ${statut == 'REJETEE' ? 'selected' : ''}>Rejetée</option>
      </select>
    </div>
    <button class="btn" type="submit">Filtrer</button>
    <a class="btn" href="<c:url value='/rh/formalites'/>">Réinitialiser</a>
  </form>

  <c:choose>
    <c:when test="${empty formalites.content}"><div class="card empty-state"><b>Aucune formalité</b>Aucune démarche ne correspond à ce filtre.</div></c:when>
    <c:otherwise>
      <div class="card card-flat"><div class="table-wrap"><table>
        <thead><tr><th>Salarié</th><th>Organisme</th><th>Type</th><th>Préparation</th><th>Envoi</th><th>Référence</th><th>Statut</th><th></th></tr></thead>
        <tbody>
          <c:forEach var="d" items="${formalites.content}">
            <tr>
              <td><b><c:out value="${d.employeNom}"/></b></td>
              <td><c:out value="${d.organisme}"/></td>
              <td><c:out value="${d.typeDeclaration}"/></td>
              <td><c:out value="${d.datePreparationFr}"/></td>
              <td><c:out value="${d.dateEnvoiFr}"/></td>
              <td><c:out value="${d.referenceExterne}"/></td>
              <td><span class="badge <c:out value='${d.statutBadge}'/>"><c:out value="${d.statutLibelle}"/></span></td>
              <td class="right"><a class="btn btn-sm" href="<c:url value='/rh/formalites/${d.id}'/>">Ouvrir</a></td>
            </tr>
          </c:forEach>
        </tbody>
      </table></div></div>
      <div class="pager">
        <c:if test="${not formalites.first}"><a class="btn btn-sm" href="<c:url value='/rh/formalites'><c:param name='page' value='${formalites.number - 1}'/><c:param name='statut' value='${statut}'/></c:url>">Précédent</a></c:if>
        <span>Page ${formalites.number + 1} / ${formalites.totalPages == 0 ? 1 : formalites.totalPages} — ${formalites.totalElements} formalité(s)</span>
        <c:if test="${not formalites.last}"><a class="btn btn-sm" href="<c:url value='/rh/formalites'><c:param name='page' value='${formalites.number + 1}'/><c:param name='statut' value='${statut}'/></c:url>">Suivant</a></c:if>
      </div>
    </c:otherwise>
  </c:choose>
</t:layout>
