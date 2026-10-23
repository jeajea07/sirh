<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Contrats" actif="contrats">
  <div class="page-head">
    <div><h1>Contrats</h1><p>Cycle de vie des contrats : brouillon, validation, signature, activation.</p></div>
    <div class="page-actions">
      <a class="btn" href="<c:url value='/rh/contrats/echeances'/>">Échéances</a>
      <a class="btn btn-primary" href="<c:url value='/rh/contrats/nouveau'/>">Nouveau contrat</a>
    </div>
  </div>
  <f:flash/>

  <form class="filter-bar" method="get" action="<c:url value='/rh/contrats'/>">
    <div class="field grow"><label for="recherche">Rechercher</label><input id="recherche" type="text" name="recherche" value="<c:out value='${filtre.recherche}'/>" placeholder="Référence, salarié…"></div>
    <div class="field"><label for="statut">Statut</label>
      <select id="statut" name="statut">
        <option value="">Tous</option>
        <option value="BROUILLON" ${filtre.statut == 'BROUILLON' ? 'selected' : ''}>Brouillon</option>
        <option value="A_VALIDER" ${filtre.statut == 'A_VALIDER' ? 'selected' : ''}>À valider</option>
        <option value="A_SIGNER" ${filtre.statut == 'A_SIGNER' ? 'selected' : ''}>À signer</option>
        <option value="SIGNE" ${filtre.statut == 'SIGNE' ? 'selected' : ''}>Signé</option>
        <option value="EN_VIGUEUR" ${filtre.statut == 'EN_VIGUEUR' ? 'selected' : ''}>En vigueur</option>
        <option value="TERMINE" ${filtre.statut == 'TERMINE' ? 'selected' : ''}>Terminé</option>
        <option value="ANNULE" ${filtre.statut == 'ANNULE' ? 'selected' : ''}>Annulé</option>
      </select>
    </div>
    <div class="field"><label for="typeOperation">Opération</label>
      <select id="typeOperation" name="typeOperation">
        <option value="">Toutes</option>
        <option value="INITIAL" ${filtre.typeOperation == 'INITIAL' ? 'selected' : ''}>Initial</option>
        <option value="AVENANT" ${filtre.typeOperation == 'AVENANT' ? 'selected' : ''}>Avenant</option>
        <option value="RENOUVELLEMENT" ${filtre.typeOperation == 'RENOUVELLEMENT' ? 'selected' : ''}>Renouvellement</option>
      </select>
    </div>
    <button class="btn" type="submit">Filtrer</button>
    <a class="btn" href="<c:url value='/rh/contrats'/>">Réinitialiser</a>
  </form>

  <c:choose>
    <c:when test="${empty contrats.content}">
      <div class="card empty-state"><b>Aucun contrat</b>Aucun contrat ne correspond à ces critères.</div>
    </c:when>
    <c:otherwise>
      <div class="card card-flat">
        <div class="table-wrap"><table>
          <thead><tr><th>Référence</th><th>Salarié</th><th>Type</th><th>Période</th><th>Statut</th><th class="right">Salaire</th><th>Échéance</th><th></th></tr></thead>
          <tbody>
            <c:forEach var="c" items="${contrats.content}">
              <tr>
                <td><b><c:out value="${c.reference}"/></b><br><span class="muted small"><c:out value="${c.typeOperationLibelle}"/></span></td>
                <td><c:out value="${c.employeNom}"/></td>
                <td><c:out value="${c.typeContrat}"/></td>
                <td><c:out value="${c.dateDebutFr}"/><c:if test="${not empty c.dateFin}"> → <c:out value="${c.dateFinFr}"/></c:if></td>
                <td><span class="badge <c:out value='${c.statutBadge}'/>"><c:out value="${c.statutWorkflow}"/></span></td>
                <td class="right" style="font-variant-numeric:tabular-nums"><c:out value="${c.salaireFr}"/></td>
                <td>
                  <c:choose>
                    <c:when test="${c.joursRestants < 0}"><span class="muted">—</span></c:when>
                    <c:when test="${c.joursRestants <= 7}"><span class="late"><c:out value="${c.joursRestants}"/> j</span></c:when>
                    <c:otherwise><c:out value="${c.joursRestants}"/> j</c:otherwise>
                  </c:choose>
                </td>
                <td class="right"><a class="btn btn-sm" href="<c:url value='/rh/contrats/${c.id}'/>">Ouvrir</a></td>
              </tr>
            </c:forEach>
          </tbody>
        </table></div>
      </div>
      <div class="pager">
        <c:if test="${not contrats.first}"><a class="btn btn-sm" href="<c:url value='/rh/contrats'><c:param name='page' value='${contrats.number - 1}'/><c:param name='recherche' value='${filtre.recherche}'/><c:param name='statut' value='${filtre.statut}'/><c:param name='typeOperation' value='${filtre.typeOperation}'/></c:url>">Précédent</a></c:if>
        <span>Page ${contrats.number + 1} / ${contrats.totalPages == 0 ? 1 : contrats.totalPages} — ${contrats.totalElements} contrat(s)</span>
        <c:if test="${not contrats.last}"><a class="btn btn-sm" href="<c:url value='/rh/contrats'><c:param name='page' value='${contrats.number + 1}'/><c:param name='recherche' value='${filtre.recherche}'/><c:param name='statut' value='${filtre.statut}'/><c:param name='typeOperation' value='${filtre.typeOperation}'/></c:url>">Suivant</a></c:if>
      </div>
    </c:otherwise>
  </c:choose>
</t:layout>
