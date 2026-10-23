<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout titre="Échéances des contrats" actif="echeances">
  <div class="page-head">
    <div><h1>Échéances des contrats</h1><p>Contrats dont la date de fin arrive dans les <c:out value="${jours}"/> prochains jours.</p></div>
    <a class="btn" href="<c:url value='/rh/contrats'/>">Retour aux contrats</a>
  </div>
  <form class="filter-bar" method="get" action="<c:url value='/rh/contrats/echeances'/>">
    <div class="field"><label for="jours">Horizon</label><select id="jours" name="jours"><option value="7" ${jours == 7 ? 'selected' : ''}>7 jours</option><option value="30" ${jours == 30 ? 'selected' : ''}>30 jours</option><option value="60" ${jours == 60 ? 'selected' : ''}>60 jours</option><option value="90" ${jours == 90 ? 'selected' : ''}>90 jours</option></select></div>
    <button class="btn" type="submit">Afficher</button>
  </form>
  <c:choose>
    <c:when test="${empty echeances}"><div class="card empty-state"><b>Aucune échéance</b>Aucun contrat n'arrive à terme sur cette période.</div></c:when>
    <c:otherwise>
      <div class="card card-flat"><div class="table-wrap"><table><thead><tr><th>Urgence</th><th>Référence</th><th>Salarié</th><th>Type</th><th>Fin</th><th>Jours restants</th><th></th></tr></thead><tbody>
        <c:forEach var="e" items="${echeances}">
          <tr><td><span class="badge ${e.niveauUrgence == 'critique' ? 'badge-bad' : (e.niveauUrgence == 'attention' ? 'badge-warn' : 'badge-info')}">${e.niveauUrgence == 'critique' ? 'Critique' : (e.niveauUrgence == 'attention' ? 'À surveiller' : 'Normal')}</span></td><td><b><c:out value="${e.contrat.reference}"/></b></td><td><c:out value="${e.contrat.employeNom}"/></td><td><c:out value="${e.contrat.typeContrat}"/></td><td><c:out value="${e.contrat.dateFinFr}"/></td><td class="${e.joursRestants <= 7 ? 'late' : ''}"><c:out value="${e.joursRestants}"/> j</td><td class="right"><a class="btn btn-sm" href="<c:url value='/rh/contrats/${e.contrat.id}'/>">Ouvrir</a></td></tr>
        </c:forEach>
      </tbody></table></div></div>
    </c:otherwise>
  </c:choose>
</t:layout>
