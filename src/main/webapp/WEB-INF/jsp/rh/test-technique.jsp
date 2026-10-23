<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout titre="Test technique" actif="tests-rh">
  <div class="page-head">
    <div>
      <h1>Tests techniques</h1>
      <p>Ouverture des comptes candidats, suivi des notes et décision RH.</p>
    </div>
  </div>

  <c:if test="${not empty succes}"><div class="alert alert-ok" style="margin-bottom:16px"><c:out value="${succes}"/></div></c:if>
  <c:if test="${not empty erreur}"><div class="alert alert-bad" style="margin-bottom:16px"><c:out value="${erreur}"/></div></c:if>

  <c:if test="${not empty dossiersValides}">
    <section class="card stack" style="margin-bottom:16px">
      <div class="card-head"><h2>Dossiers validés sans compte de test</h2></div>
      <div class="table-wrap"><table>
        <thead><tr><th>Numéro</th><th>Candidat</th><th class="right">Ouvrir le test</th></tr></thead>
        <tbody>
          <c:forEach var="dossier" items="${dossiersValides}">
            <tr>
              <td><b><c:out value="${dossier.numero}"/></b></td>
              <td><c:out value="${dossier.candidat.prenom}"/> <c:out value="${dossier.candidat.nom}"/></td>
              <td class="right">
                <form method="post" action="<c:url value='/rh/tests/${dossier.idCandidature}/acces'/>" class="row" style="justify-content:flex-end;gap:8px">
                  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                  <input type="number" name="seuil" min="0" max="3" step="0.5" value="2" style="width:80px" aria-label="Seuil de validation">
                  <button class="btn btn-sm btn-primary" type="submit">Créer l’accès</button>
                </form>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table></div>
    </section>
  </c:if>

  <section class="card stack">
    <div class="card-head"><h2>Résultats</h2><span class="badge badge-info">${resultats.size()} test(s)</span></div>
    <c:if test="${empty resultats}">
      <div class="empty-state"><b>Aucun test technique</b>Aucun test n’a encore été enregistré.</div>
    </c:if>
    <c:if test="${not empty resultats}">
      <div class="table-wrap"><table>
        <thead><tr><th>Candidat</th><th>Note</th><th>Statut</th><th class="right">Décision</th></tr></thead>
        <tbody>
          <c:forEach var="resultat" items="${resultats}">
            <tr>
              <td><b><c:out value="${resultat.candidat}"/></b></td>
              <td><c:out value="${resultat.note}"/> / 3</td>
              <td><span class="badge ${resultat.admis ? 'badge-ok' : 'badge-warn'}"><c:out value="${resultat.statut}"/></span></td>
              <td class="right">
                <form method="post" action="<c:url value='/rh/tests/${resultat.idCandidature}/decision'/>" class="row" style="justify-content:flex-end;gap:6px">
                  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                  <button class="btn btn-sm btn-primary" type="submit" name="decision" value="admis">Admettre</button>
                  <button class="btn btn-sm" type="submit" name="decision" value="refuse">Refuser</button>
                </form>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table></div>
    </c:if>
  </section>
</t:layout>
