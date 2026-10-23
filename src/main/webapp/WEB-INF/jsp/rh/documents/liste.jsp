<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Documents RH" actif="documents">
  <div class="page-head">
    <div><h1>Documents RH</h1><p>Pièces du dossier salarié, versions et contrôle.</p></div>
    <a class="btn btn-primary" href="<c:url value='/rh/documents/nouveau'/>">Déposer un document</a>
  </div>
  <f:flash/>

  <form class="filter-bar" method="get" action="<c:url value='/rh/documents'/>">
    <div class="field"><label for="type">Type</label>
      <select id="type" name="type">
        <option value="">Tous</option>
        <option value="CONTRAT" ${type == 'CONTRAT' ? 'selected' : ''}>Contrat</option>
        <option value="ADMINISTRATIF" ${type == 'ADMINISTRATIF' ? 'selected' : ''}>Administratif</option>
        <option value="SOCIAL" ${type == 'SOCIAL' ? 'selected' : ''}>Social</option>
        <option value="SANTE" ${type == 'SANTE' ? 'selected' : ''}>Santé</option>
        <option value="INTERNE" ${type == 'INTERNE' ? 'selected' : ''}>Interne</option>
        <option value="FORMATION" ${type == 'FORMATION' ? 'selected' : ''}>Formation</option>
        <option value="MATERIEL" ${type == 'MATERIEL' ? 'selected' : ''}>Matériel</option>
        <option value="CV" ${type == 'CV' ? 'selected' : ''}>CV</option>
      </select>
    </div>
    <div class="field"><label for="statut">Statut</label>
      <select id="statut" name="statut">
        <option value="">Tous</option>
        <option value="RECU" ${statut == 'RECU' ? 'selected' : ''}>Reçu</option>
        <option value="VERIFIE" ${statut == 'VERIFIE' ? 'selected' : ''}>Vérifié</option>
        <option value="REJETE" ${statut == 'REJETE' ? 'selected' : ''}>Rejeté</option>
      </select>
    </div>
    <button class="btn" type="submit">Filtrer</button>
    <a class="btn" href="<c:url value='/rh/documents'/>">Réinitialiser</a>
  </form>

  <c:choose>
    <c:when test="${empty documents.content}">
      <div class="card empty-state"><b>Aucun document</b>Aucune pièce ne correspond à ces critères.</div>
    </c:when>
    <c:otherwise>
      <div class="card card-flat">
        <div class="table-wrap"><table>
          <thead><tr><th>Document</th><th>Salarié</th><th>Contrat</th><th>Version</th><th>Confidentialité</th><th>Dépôt</th><th>Statut</th><th></th></tr></thead>
          <tbody>
            <c:forEach var="d" items="${documents.content}">
              <tr>
                <td><b><c:out value="${d.titre}"/></b><br><span class="muted small"><c:out value="${d.typeLibelle}"/> · <c:out value="${d.tailleFr}"/></span></td>
                <td><c:out value="${d.employeNom}"/></td>
                <td><c:out value="${d.contratReference}"/></td>
                <td>v<c:out value="${d.version}"/></td>
                <td><span class="badge ${d.confidentialite == 'SECRET' ? 'badge-bad' : (d.confidentialite == 'CONFIDENTIEL' ? 'badge-warn' : (d.confidentialite == 'PUBLIC' ? 'badge-ok' : 'badge'))}"><c:out value="${d.confidentialiteLibelle}"/></span></td>
                <td><c:out value="${d.dateDepotFr}"/></td>
                <td><span class="badge <c:out value='${d.statutBadge}'/>"><c:out value="${d.statutLibelle}"/></span><c:if test="${not empty d.motifRejet}"><br><span class="small late"><c:out value="${d.motifRejet}"/></span></c:if></td>
                <td class="right">
                  <div class="actions">
                    <a class="btn btn-sm" href="<c:url value='/rh/documents/${d.id}/telecharger'/>">Télécharger</a>
                    <c:if test="${d.statut == 'RECU'}">
                      <form method="post" action="<c:url value='/rh/documents/${d.id}/verifier'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-sm btn-ok" type="submit">Vérifier</button></form>
                      <details><summary class="btn btn-sm btn-danger">Rejeter</summary><form method="post" action="<c:url value='/rh/documents/${d.id}/rejeter'/>" class="stack" style="margin-top:8px;min-width:220px"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="text" name="motif" placeholder="Motif du rejet" required><button class="btn btn-sm btn-danger btn-block" type="submit">Confirmer le rejet</button></form></details>
                    </c:if>
                  </div>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table></div>
      </div>
      <div class="pager">
        <c:if test="${not documents.first}"><a class="btn btn-sm" href="<c:url value='/rh/documents'><c:param name='page' value='${documents.number - 1}'/><c:param name='type' value='${type}'/><c:param name='statut' value='${statut}'/></c:url>">Précédent</a></c:if>
        <span>Page ${documents.number + 1} / ${documents.totalPages == 0 ? 1 : documents.totalPages} — ${documents.totalElements} document(s)</span>
        <c:if test="${not documents.last}"><a class="btn btn-sm" href="<c:url value='/rh/documents'><c:param name='page' value='${documents.number + 1}'/><c:param name='type' value='${type}'/><c:param name='statut' value='${statut}'/></c:url>">Suivant</a></c:if>
      </div>
    </c:otherwise>
  </c:choose>
</t:layout>
