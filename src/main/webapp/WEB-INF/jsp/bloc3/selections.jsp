<%-- Bloc 3 --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout titre="Sélection et embauche" actif="bloc3">
  <div class="page-head">
    <div>
      <h1>Sélection et embauche</h1>
      <p>Dossiers transmis par le Bloc 2, décisions finales et suivi des propositions.</p>
    </div>
  </div>

  <c:if test="${not empty succes}"><div class="alert alert-ok" style="margin-bottom:16px"><c:out value="${succes}"/></div></c:if>
  <c:if test="${not empty erreur}"><div class="alert alert-bad" style="margin-bottom:16px"><c:out value="${erreur}"/></div></c:if>

  <form class="toolbar" method="get" action="<c:url value='/bloc3/selections'/>">
    <input type="search" name="q" value="${q}" placeholder="N° candidature, offre ou candidat" aria-label="Rechercher par identifiant">
    <select name="tri" aria-label="Trier les dossiers">
      <option value="recent" ${tri == 'recent' ? 'selected' : ''}>Plus récents</option>
      <option value="note" ${tri == 'note' ? 'selected' : ''}>Note finale</option>
      <option value="rang" ${tri == 'rang' ? 'selected' : ''}>Rang</option>
      <option value="candidat" ${tri == 'candidat' ? 'selected' : ''}>Candidat</option>
      <option value="offre" ${tri == 'offre' ? 'selected' : ''}>Offre</option>
    </select>
    <button class="btn" type="submit">Rechercher</button>
  </form>

  <c:if test="${pageContext.request.isUserInRole('RH')}">
    <details class="card stack" style="margin-bottom:16px">
      <summary><b>Enregistrer un dossier reçu du Bloc 2</b></summary>
      <p class="muted">Saisissez les identifiants transmis par le Bloc 2. Le raccordement automatique entre les bases reste à configurer.</p>
      <form method="post" action="<c:url value='/bloc3/selections/reception'/>">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <div class="form-grid">
          <div class="field"><label for="idCandidatureB2">Identifiant candidature</label><input id="idCandidatureB2" type="number" min="1" name="idCandidatureB2" required></div>
          <div class="field"><label for="idOffreB2">Identifiant offre</label><input id="idOffreB2" type="number" min="1" name="idOffreB2" required></div>
          <div class="field"><label for="idCandidatB2">Identifiant candidat</label><input id="idCandidatB2" type="number" min="1" name="idCandidatB2" required></div>
          <div class="field"><label for="scoreTest">Test (/20)</label><input id="scoreTest" type="number" min="0" max="20" step="0.01" name="scoreTest"></div>
          <div class="field"><label for="scoreEntretienRh">Entretien RH (/5)</label><input id="scoreEntretienRh" type="number" min="0" max="5" step="0.01" name="scoreEntretienRh"></div>
          <div class="field"><label for="scoreEntretienTech">Entretien technique (/5)</label><input id="scoreEntretienTech" type="number" min="0" max="5" step="0.01" name="scoreEntretienTech"></div>
          <div class="field"><label for="scoreEntretienDg">Entretien DG (/5)</label><input id="scoreEntretienDg" type="number" min="0" max="5" step="0.01" name="scoreEntretienDg"></div>
          <div class="field"><label for="scoreFinal">Note finale (/5)</label><input id="scoreFinal" type="number" min="0" max="5" step="0.01" name="scoreFinal" required></div>
          <div class="field"><label for="rangClassement">Rang transmis par le Bloc 2</label><input id="rangClassement" type="number" min="1" name="rangClassement"></div>
        </div>
        <button class="btn btn-primary" type="submit" style="margin-top:12px">Enregistrer le dossier</button>
      </form>
    </details>
  </c:if>

  <c:choose>
    <c:when test="${empty selectionPage.content}">
      <div class="card empty">Aucun dossier reçu du Bloc 2 pour le moment.</div>
    </c:when>
    <c:otherwise>
      <div class="card card-flat">
        <div class="table-wrap"><table>
          <thead><tr><th>Rang</th><th>Candidat</th><th>Test</th><th>E1</th><th>E2</th><th>E3</th><th>Note finale</th><th>Offre</th><th>Dossier</th><th>Décision</th><th></th></tr></thead>
          <tbody>
            <c:forEach var="s" items="${selectionPage.content}">
              <tr>
                <td><c:out value="${empty s.rangClassement ? '—' : s.rangClassement}"/></td>
                <td><b>#<c:out value="${s.idCandidatB2}"/></b><br><span class="muted small">Candidature #<c:out value="${s.idCandidatureB2}"/></span></td>
                <td><c:out value="${empty s.scoreTest ? '—' : s.scoreTest}"/> / 20</td>
                <td><c:out value="${empty s.scoreEntretienRh ? '—' : s.scoreEntretienRh}"/></td>
                <td><c:out value="${empty s.scoreEntretienTech ? '—' : s.scoreEntretienTech}"/></td>
                <td><c:out value="${empty s.scoreEntretienDg ? '—' : s.scoreEntretienDg}"/></td>
                <td><b><c:out value="${empty s.scoreFinal ? '—' : s.scoreFinal}"/></b> / 5</td>
                <td>#<c:out value="${s.idOffreB2}"/></td>
                <td><span class="badge badge-info"><c:out value="${s.etatDossier.libelle}"/></span></td>
                <td><c:out value="${empty s.decision ? 'À décider' : s.decision.libelle}"/></td>
                <td class="right"><a class="btn btn-sm btn-primary" href="<c:url value='/bloc3/selections/${s.idSelection}'/>">Ouvrir</a></td>
              </tr>
            </c:forEach>
          </tbody>
        </table></div>
      </div>
      <div class="row wrap" style="justify-content:space-between;margin-top:14px">
        <span class="muted">Page <c:out value="${selectionPage.number + 1}"/> sur <c:out value="${selectionPage.totalPages}"/> · <c:out value="${selectionPage.totalElements}"/> dossier(s)</span>
        <div class="row wrap">
          <c:if test="${selectionPage.hasPrevious}"><a class="btn btn-sm" href="<c:url value='/bloc3/selections?page=${selectionPage.number - 1}&tri=${tri}&q=${q}'/>">Précédent</a></c:if>
          <c:if test="${selectionPage.hasNext}"><a class="btn btn-sm" href="<c:url value='/bloc3/selections?page=${selectionPage.number + 1}&tri=${tri}&q=${q}'/>">Suivant</a></c:if>
        </div>
      </div>
    </c:otherwise>
  </c:choose>
</t:layout>
