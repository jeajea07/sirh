<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Mon équipe" actif="equipe">
  <div class="page-head"><div><h1>Mon équipe</h1><p><c:out value="${equipe.size()}"/> salarié(s) dont vous êtes le manager (responsable d'affectation, ou à défaut responsable du département / DG).</p></div><a class="btn" href="<c:url value='/planning'/>">Plannings</a></div>
  <f:flash/>
  <c:if test="${empty equipe}"><div class="card empty-state"><b>Aucune équipe</b>Aucun salarié ne vous est rattaché.</div></c:if>

  <c:if test="${not empty demandes}">
  <div class="card" style="margin-bottom:16px"><div class="card-head"><h2>Congés à valider (<c:out value="${demandes.size()}"/>)</h2></div>
    <c:forEach var="d" items="${demandes}">
      <div class="doc-row" style="align-items:flex-start">
        <div class="grow"><b><c:out value="${d.employe.nomComplet}"/></b> — <c:out value="${d.type.libelle}"/> · <c:out value="${d.periodeLibelle}"/> (<c:out value="${d.nbJoursFr}"/>)
          <c:if test="${not empty d.motif}"><small>Motif : <c:out value="${d.motif}"/></small></c:if>
          <c:if test="${not empty simultanes[d.id]}"><small class="late">Déjà absents sur la période : <c:forEach var="n" items="${simultanes[d.id]}" varStatus="s"><c:out value="${n}"/><c:if test="${not s.last}">, </c:if></c:forEach> (RG-DC13)</small></c:if>
          <div style="display:flex;gap:6px;margin-top:6px;flex-wrap:wrap">
            <form method="post" action="<c:url value='/equipe/conges/${d.id}/valider'/>" style="display:flex;gap:6px"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="text" name="commentaire" placeholder="Commentaire"><button class="btn btn-sm btn-ok" type="submit">Valider</button></form>
            <form method="post" action="<c:url value='/equipe/conges/${d.id}/refuser'/>" style="display:flex;gap:6px"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="text" name="motif" placeholder="Motif du refus" required><button class="btn btn-sm btn-danger" type="submit">Refuser</button></form>
          </div>
        </div>
      </div>
    </c:forEach>
  </div>
  </c:if>

  <div class="card" style="margin-bottom:16px"><div class="card-head"><h2>Anomalies de pointage (<c:out value="${anomalies.size()}"/>)</h2></div>
    <c:choose><c:when test="${empty anomalies}"><p class="muted">Aucune anomalie à traiter.</p></c:when><c:otherwise>
      <c:forEach var="a" items="${anomalies}">
        <div class="doc-row" style="align-items:flex-start">
          <div class="grow"><b><c:out value="${a.employe.nomComplet}"/></b> — <c:out value="${a.typeLibelle}"/> le <c:out value="${a.jourFr}"/> <span class="badge <c:out value='${a.statutBadge}'/>"><c:out value="${a.statutLibelle}"/></span>
            <small><c:out value="${a.detail}"/></small>
            <c:choose><c:when test="${not empty a.justification}"><small>Justification : <c:out value="${a.justification}"/><c:if test="${not empty a.heureProposee}"> — pointage proposé : <c:out value="${a.sensPropose}"/> le <c:out value="${a.heureProposeeFr}"/></c:if></small></c:when>
              <c:otherwise><small class="muted">Pas encore justifiée par le salarié.</small></c:otherwise></c:choose>
            <div style="display:flex;gap:6px;margin-top:6px;flex-wrap:wrap">
              <form method="post" action="<c:url value='/equipe/anomalies/${a.id}/accepter'/>" style="display:flex;gap:6px"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="text" name="commentaire" placeholder="Commentaire"><button class="btn btn-sm btn-ok" type="submit">Accepter</button></form>
              <form method="post" action="<c:url value='/equipe/anomalies/${a.id}/refuser'/>" style="display:flex;gap:6px"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="text" name="motif" placeholder="Motif (confirmer l'anomalie)" required><button class="btn btn-sm btn-danger" type="submit">Confirmer</button></form>
            </div>
          </div>
        </div>
      </c:forEach>
    </c:otherwise></c:choose>
  </div>

  <div class="card"><div class="card-head"><h2>Absents dans les 30 prochains jours</h2></div>
    <c:choose><c:when test="${empty congesEquipe and empty absencesEquipe}"><p class="muted">Personne n'est absent.</p></c:when><c:otherwise>
      <div class="table-wrap"><table><thead><tr><th>Salarié</th><th>Période</th><th>Nature</th></tr></thead><tbody>
        <c:forEach var="d" items="${congesEquipe}"><tr><td><c:out value="${d.employe.nomComplet}"/></td><td><c:out value="${d.periodeLibelle}"/></td><td><c:out value="${d.type.libelle}"/></td></tr></c:forEach>
        <%-- RG-AB07 : le manager ne voit pas le motif médical, seulement justifié / non justifié. --%>
        <c:forEach var="a" items="${absencesEquipe}"><tr><td><c:out value="${a.employe.nomComplet}"/></td><td><c:out value="${a.periodeLibelle}"/></td><td>Absent — <c:out value="${a.statut == 'INJUSTIFIEE' ? 'non justifié' : (a.statut == 'JUSTIFIEE' ? 'justifié' : 'en attente de justificatif')}"/></td></tr></c:forEach>
      </tbody></table></div>
    </c:otherwise></c:choose>
  </div>
</t:layout>
