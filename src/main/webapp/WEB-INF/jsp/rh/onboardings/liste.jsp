<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Onboardings" actif="onboardings">
  <div class="page-head"><div><h1>Onboardings</h1><p>Parcours d'intégration et suivi des tâches par salarié.</p></div><div class="page-actions"><a class="btn" href="<c:url value='/rh/onboardings/modeles'/>">Modèles</a><a class="btn btn-primary" href="<c:url value='/rh/onboardings/nouveau'/>">Nouvel onboarding</a></div></div>
  <f:flash/>
  <form class="filter-bar" method="get" action="<c:url value='/rh/onboardings'/>">
    <div class="field"><label for="statut">Statut</label><select id="statut" name="statut"><option value="">Tous</option><option value="A_PREPARER" ${statut == 'A_PREPARER' ? 'selected' : ''}>À préparer</option><option value="PRET" ${statut == 'PRET' ? 'selected' : ''}>Prêt</option><option value="EN_COURS" ${statut == 'EN_COURS' ? 'selected' : ''}>En cours</option><option value="BLOQUE" ${statut == 'BLOQUE' ? 'selected' : ''}>Bloqué</option><option value="A_VERIFIER" ${statut == 'A_VERIFIER' ? 'selected' : ''}>À vérifier</option><option value="CLOTURE" ${statut == 'CLOTURE' ? 'selected' : ''}>Clôturé</option><option value="ANNULE" ${statut == 'ANNULE' ? 'selected' : ''}>Annulé</option></select></div>
    <button class="btn" type="submit">Filtrer</button><a class="btn" href="<c:url value='/rh/onboardings'/>">Réinitialiser</a>
  </form>
  <c:choose>
    <c:when test="${empty onboardings.content}"><div class="card empty-state"><b>Aucun onboarding</b>Aucun parcours ne correspond à ce filtre.</div></c:when>
    <c:otherwise>
      <div class="card card-flat"><div class="table-wrap"><table><thead><tr><th>Référence</th><th>Salarié</th><th>Poste / département</th><th>Début prévu</th><th>Progression</th><th>Statut</th><th></th></tr></thead><tbody>
        <c:forEach var="o" items="${onboardings.content}">
          <tr><td><b><c:out value="${o.reference}"/></b><c:if test="${o.tachesEnRetard > 0}"><br><span class="late small"><c:out value="${o.tachesEnRetard}"/> en retard</span></c:if></td><td><c:out value="${o.employeNom}"/></td><td><c:out value="${o.poste}"/><br><span class="muted small"><c:out value="${o.departement}"/></span></td><td><c:out value="${o.dateDebutPrevueFr}"/></td><td><div class="progress-cell"><div class="progress-line"><span style="width:${o.progression}%"></span></div><div class="progress-text"><span>${o.progression}%</span><span>${o.tachesTerminees}/${o.nombreTaches}</span></div></div></td><td><span class="badge <c:out value='${o.statutBadge}'/>"><c:out value="${o.statutLibelle}"/></span></td><td class="right"><a class="btn btn-sm" href="<c:url value='/rh/onboardings/${o.id}'/>">Ouvrir</a></td></tr>
        </c:forEach>
      </tbody></table></div></div>
      <div class="pager">
        <c:if test="${not onboardings.first}"><a class="btn btn-sm" href="<c:url value='/rh/onboardings'><c:param name='page' value='${onboardings.number - 1}'/><c:param name='statut' value='${statut}'/></c:url>">Précédent</a></c:if>
        <span>Page ${onboardings.number + 1} / ${onboardings.totalPages == 0 ? 1 : onboardings.totalPages} — ${onboardings.totalElements} parcours</span>
        <c:if test="${not onboardings.last}"><a class="btn btn-sm" href="<c:url value='/rh/onboardings'><c:param name='page' value='${onboardings.number + 1}'/><c:param name='statut' value='${statut}'/></c:url>">Suivant</a></c:if>
      </div>
    </c:otherwise>
  </c:choose>
</t:layout>
