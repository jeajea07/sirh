<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Pointage" actif="pointage">
  <c:choose>
  <c:when test="${sansEmploye}"><div class="page-head"><div><h1>Pointage</h1></div></div><div class="card empty-state"><b>Aucun salarié</b>Votre compte n'est rattaché à aucun salarié.</div></c:when>
  <c:otherwise>
  <div class="page-head"><div><h1>Pointage</h1><p>Régime : <c:out value="${empty regime ? 'non défini' : regime.libelle}"/></p></div></div>
  <f:flash/>
  <div class="detail-grid">
    <div class="stack">
      <div class="card"><div class="card-head"><h2>Aujourd'hui</h2></div>
        <form method="post" action="<c:url value='/pointage'/>" style="margin-bottom:12px">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
          <c:set var="dernier" value="${empty pointagesDuJour ? null : pointagesDuJour[pointagesDuJour.size() - 1]}"/>
          <button class="btn btn-primary btn-block" type="submit" style="font-size:1.1rem;padding:14px"><c:out value="${not empty dernier and dernier.sens == 'ENTREE' ? 'Pointer ma sortie' : 'Pointer mon entrée'}"/></button>
        </form>
        <c:choose><c:when test="${empty pointagesDuJour}"><p class="muted">Aucun pointage aujourd'hui.</p></c:when><c:otherwise>
          <ul class="timeline compact"><c:forEach var="p" items="${pointagesDuJour}"><li><b><c:out value="${p.heureFr}"/></b> — <c:out value="${p.sens == 'ENTREE' ? 'Entrée' : 'Sortie'}"/><c:if test="${p.manuel}"> <span class="badge badge-info">manuel</span></c:if></li></c:forEach></ul>
        </c:otherwise></c:choose>
      </div>

      <div class="card"><div class="card-head"><h2>Mes anomalies</h2></div>
        <c:choose><c:when test="${empty anomalies}"><p class="muted">Aucune anomalie.</p></c:when><c:otherwise>
          <c:forEach var="a" items="${anomalies}">
            <div class="doc-row" style="align-items:flex-start">
              <div class="grow"><b><c:out value="${a.typeLibelle}"/></b> — <c:out value="${a.jourFr}"/> <span class="badge <c:out value='${a.statutBadge}'/>"><c:out value="${a.statutLibelle}"/></span>
                <small><c:out value="${a.detail}"/></small>
                <c:if test="${not empty a.justification}"><small>Ma justification : <c:out value="${a.justification}"/><c:if test="${not empty a.heureProposee}"> (heure proposée : <c:out value="${a.heureProposeeFr}"/>)</c:if></small></c:if>
                <c:if test="${not empty a.commentaireTraitement}"><small>Décision : <c:out value="${a.commentaireTraitement}"/></small></c:if>
                <c:if test="${a.statut == 'OUVERTE'}">
                  <details style="margin-top:6px"><summary class="btn btn-sm">Justifier</summary>
                    <form method="post" action="<c:url value='/pointage/anomalies/${a.id}/justifier'/>" class="stack" style="margin-top:8px">
                      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                      <div class="field"><label>Justification *</label><textarea name="justification" rows="2" required placeholder="Oubli de badge, panne, mission…"></textarea></div>
                      <div class="form-grid">
                        <div class="field"><label>Heure réelle (si oubli)</label><input type="datetime-local" name="heure"></div>
                        <div class="field"><label>Sens</label><select name="sens"><option value="">—</option><option value="ENTREE">Entrée</option><option value="SORTIE">Sortie</option></select></div>
                      </div>
                      <span class="hint">Une absence se déclare plutôt dans « Congés » (maladie, absence autorisée).</span>
                      <button class="btn btn-sm btn-primary" type="submit">Envoyer à mon manager</button>
                    </form>
                  </details>
                </c:if>
              </div>
            </div>
          </c:forEach>
        </c:otherwise></c:choose>
      </div>
    </div>

    <div class="stack">
      <div class="card"><div class="card-head"><h2>Mon planning (14 jours)</h2></div>
        <div class="table-wrap"><table><thead><tr><th>Jour</th><th>Prévu</th><th>Durée</th></tr></thead><tbody>
          <c:forEach var="p" items="${previsions}" varStatus="s"><tr><td><c:out value="${jours[s.index]}"/></td><td><c:out value="${p.libelle}"/><c:if test="${p.source == 'REGIME'}"> <span class="small muted">(régime)</span></c:if></td><td><c:out value="${p.dureeFr}"/></td></tr></c:forEach>
        </tbody></table></div>
      </div>
      <div class="card"><div class="card-head"><h2>Mon relevé — <c:out value="${mois}"/></h2>
          <form method="get" action="<c:url value='/pointage'/>" style="display:flex;gap:6px"><input type="month" name="mois" value="${mois}"><button class="btn btn-sm" type="submit">Voir</button></form></div>
        <c:choose><c:when test="${empty journees}"><p class="muted">Aucune journée calculée pour ce mois (calcul chaque nuit, pour la veille).</p></c:when><c:otherwise>
          <div class="table-wrap"><table><thead><tr><th>Jour</th><th>Prévu</th><th>Travaillé</th><th>Retard</th><th>Sup.</th><th>Statut</th></tr></thead><tbody>
            <c:forEach var="j" items="${journees}"><tr><td><c:out value="${j.jourFr}"/></td><td><c:out value="${j.prevuesFr}"/></td><td><b><c:out value="${j.travailleesFr}"/></b></td><td class="${j.minutesRetard > 0 ? 'late' : ''}"><c:out value="${j.retardFr}"/></td><td><c:out value="${j.supFr}"/></td><td><span class="badge <c:out value='${j.statutBadge}'/>"><c:out value="${j.statutLibelle}"/></span></td></tr></c:forEach>
          </tbody></table></div>
        </c:otherwise></c:choose>
      </div>
    </div>
  </div>
  </c:otherwise>
  </c:choose>
</t:layout>
