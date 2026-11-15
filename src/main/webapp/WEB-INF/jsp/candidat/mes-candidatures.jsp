<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout titre="Mon espace candidat" actif="candidat">
  <div class="page-head">
    <div>
      <h1>Mon espace candidat</h1>
      <p>Bonjour <strong><c:out value="${candidat.prenom}"/> <c:out value="${candidat.nom}"/></strong></p>
    </div>
    <div class="badge badge-info">Candidat</div>
  </div>

  <c:if test="${not empty succes}"><div class="alert alert-ok" style="margin-bottom:16px"><c:out value="${succes}"/></div></c:if>
  <c:if test="${not empty erreur}"><div class="alert alert-bad" style="margin-bottom:16px"><c:out value="${erreur}"/></div></c:if>

  <div class="card stack">
    <div class="card-head"><h2>Mes candidatures</h2></div>

    <c:if test="${empty candidatures}">
      <div class="empty-state"><b>Aucune candidature</b>Aucune candidature n’est rattachée à votre compte.</div>
    </c:if>

    <c:if test="${not empty candidatures}">
      <div class="table-wrap">
        <table>
          <thead><tr><th>Numéro</th><th>Offre</th><th>Étape</th><th>Prochain rendez-vous</th><th class="right">Test technique</th></tr></thead>
          <tbody>
            <c:forEach var="candidature" items="${candidatures}">
              <tr>
                <td><b><c:out value="${candidature.numero}"/></b></td>
                <td><c:out value="${empty candidature.offre ? 'Réserve' : candidature.offre.titre}"/></td>
                <td><span class="badge ${candidature.statut == 'NON_RETENUE' ? 'badge-bad' : 'badge-info'}">
                  <c:choose>
                    <c:when test="${candidature.statut == 'RECUE' or candidature.statut == 'A_ETUDIER'}">Dossier en cours d'étude</c:when>
                    <c:when test="${candidature.statut == 'DOSSIER_VALIDE' or candidature.statut == 'TEST_TECHNIQUE'}">Test technique</c:when>
                    <c:when test="${candidature.statut == 'ENTRETIEN_RH'}">Entretien 1 — RH</c:when>
                    <c:when test="${candidature.statut == 'ENTRETIEN_TECHNICIEN'}">Entretien 2 — Technicien</c:when>
                    <c:when test="${candidature.statut == 'ENTRETIEN_DG'}">Entretien 3 — DG</c:when>
                    <c:when test="${candidature.statut == 'CONFIRMATION_FINALE'}">Confirmation finale</c:when>
                    <c:when test="${candidature.statut == 'NON_RETENUE'}">Non retenue</c:when>
                    <c:otherwise><c:out value="${candidature.statut}"/></c:otherwise>
                  </c:choose></span></td>
                <td><c:set var="conv" value="${convocations[candidature.idCandidature]}"/>
                  <c:choose>
                    <c:when test="${not empty conv}"><b><c:out value="${conv.datePrevueFr}"/></b><c:if test="${not empty conv.lieu}"><br><span class="muted small"><c:out value="${conv.lieu}"/></span></c:if></c:when>
                    <c:otherwise><span class="muted">—</span></c:otherwise>
                  </c:choose></td>
                <td class="right">
                  <c:choose>
                    <c:when test="${candidature.testTechnique != null and (candidature.statut == 'DOSSIER_VALIDE' or candidature.statut == 'TEST_TECHNIQUE')}">
                      <a class="btn btn-sm btn-primary" href="<c:url value='/candidat/test/${candidature.idCandidature}'/>">Accéder au test</a>
                    </c:when>
                    <c:otherwise><span class="muted small">Non disponible</span></c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </c:if>
  </div>
</t:layout>
