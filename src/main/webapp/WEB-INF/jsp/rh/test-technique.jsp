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
      <div class="card-head"><h2>Dossiers validés — accès au test</h2><span class="muted small">L'accès est créé et envoyé automatiquement à la validation du dossier.</span></div>
      <div class="table-wrap"><table>
        <thead><tr><th>Numéro</th><th>Candidat</th><th>Accès</th><th class="right">Accès au test</th></tr></thead>
        <tbody>
          <c:forEach var="dossier" items="${dossiersValides}">
            <tr>
              <td><b><c:out value="${dossier.numero}"/></b></td>
              <td><c:out value="${dossier.candidat.prenom}"/> <c:out value="${dossier.candidat.nom}"/></td>
              <td><c:choose><c:when test="${dossier.accesCree}"><span class="badge badge-ok">Envoyé — <c:out value="${dossier.candidat.identifiant}"/></span></c:when><c:otherwise><span class="badge badge-warn">À créer</span></c:otherwise></c:choose></td>
              <td class="right">
                <form method="post" action="<c:url value='/rh/tests/${dossier.idCandidature}/acces'/>" class="row" style="justify-content:flex-end;gap:8px">
                  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                  <input type="number" name="seuil" min="0" max="3" step="0.5" value="2" style="width:80px" aria-label="Seuil de validation">
                  <button class="btn btn-sm ${dossier.accesCree ? '' : 'btn-primary'}" type="submit">${dossier.accesCree ? 'Renvoyer un nouvel accès' : 'Créer l’accès'}</button>
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
                <div class="row" style="justify-content:flex-end;gap:6px;flex-wrap:wrap">
                  <form method="post" action="<c:url value='/rh/tests/${resultat.idCandidature}/correction'/>" class="row" style="gap:6px;align-items:center">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <input type="text" name="Q1" value="1" style="width:58px" aria-label="Note Q1">
                    <input type="text" name="Q2" value="1" style="width:58px" aria-label="Note Q2">
                    <input type="text" name="Q3" value="1" style="width:58px" aria-label="Note Q3">
                    <input type="text" name="commentaire" value="Correction RH" style="width:120px" aria-label="Commentaire de correction">
                    <button class="btn btn-sm btn-primary" type="submit">Corriger</button>
                  </form>
                  <form method="post" action="<c:url value='/rh/tests/${resultat.idCandidature}/decision'/>" class="row" style="gap:6px">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button class="btn btn-sm btn-primary" type="submit" name="decision" value="admis">Admettre</button>
                    <button class="btn btn-sm" type="submit" name="decision" value="refuse">Refuser</button>
                  </form>
                  <form method="post" action="<c:url value='/rh/tests/${resultat.idCandidature}/relance'/>" class="row">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button class="btn btn-sm" type="submit">Relancer</button>
                  </form>
                  <form method="post" action="<c:url value='/rh/tests/${resultat.idCandidature}/absence'/>" class="row" style="gap:6px;align-items:center">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <input type="text" name="motif" value="Absence" style="width:110px" aria-label="Motif d’absence">
                    <button class="btn btn-sm" type="submit" name="exception" value="true">Exception</button>
                    <button class="btn btn-sm" type="submit" name="exception" value="false">Absent</button>
                  </form>
                </div>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table></div>
    </c:if>
  </section>
</t:layout>
