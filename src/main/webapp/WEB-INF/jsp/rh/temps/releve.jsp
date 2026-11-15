<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Relevé de temps" actif="temps-rh">
  <c:set var="onglet" value="releve"/>
  <div class="page-head"><div><h1>Relevés et pointages</h1><p>Journées calculées, pointages bruts et saisie manuelle motivée (RG-PT03).</p></div></div>
  <f:flash/>
  <div class="tabs"><a class="tab ${onglet == 'anomalies' ? 'active' : ''}" href="<c:url value='/rh/temps'/>">Anomalies</a><a class="tab ${onglet == 'releve' ? 'active' : ''}" href="<c:url value='/rh/temps/releve'/>">Relevés et pointages</a><a class="tab" href="<c:url value='/planning'/>">Plannings</a><a class="tab ${onglet == 'periodes' ? 'active' : ''}" href="<c:url value='/rh/temps/periodes'/>">Clôture et paie</a><a class="tab ${onglet == 'parametres' ? 'active' : ''}" href="<c:url value='/rh/temps/parametres'/>">Paramétrage</a></div>
  <form method="get" action="<c:url value='/rh/temps/releve'/>" class="filter-bar">
    <div class="field grow"><label>Salarié</label><select name="idEmploye" required><option value="">Choisir…</option><c:forEach var="e" items="${employes}"><option value="${e.id}" ${salarie.id == e.id ? 'selected' : ''}><c:out value="${e.nomComplet}"/> — <c:out value="${e.matricule}"/></option></c:forEach></select></div>
    <div class="field"><label>Mois</label><input type="month" name="mois" value="${mois}"></div>
    <button class="btn" type="submit">Afficher</button>
  </form>
  <c:if test="${not empty salarie}">
  <div class="detail-grid">
    <div class="stack">
      <div class="card"><div class="card-head"><h2>Journées — <c:out value="${mois}"/></h2>
        <form method="post" action="<c:url value='/rh/temps/recalculer'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="hidden" name="idEmploye" value="${salarie.id}"><input type="hidden" name="mois" value="${mois}"><button class="btn btn-sm" type="submit">Recalculer</button></form></div>
        <c:choose><c:when test="${empty journees}"><p class="muted">Aucune journée calculée.</p></c:when><c:otherwise>
          <div class="table-wrap"><table><thead><tr><th>Jour</th><th>Type</th><th>Prévu</th><th>Travaillé</th><th>Retard</th><th>Sup.</th><th>Absence</th><th>Statut</th></tr></thead><tbody>
            <c:forEach var="j" items="${journees}"><tr><td><c:out value="${j.jourFr}"/></td><td class="small"><c:out value="${j.typeJour}"/> <span class="muted">(<c:out value="${j.sourcePrevision == 'PLANNING' ? 'planning' : 'régime'}"/>)</span></td><td><c:out value="${j.prevuesFr}"/></td><td><b><c:out value="${j.travailleesFr}"/></b></td><td class="${j.minutesRetard > 0 ? 'late' : ''}"><c:out value="${j.retardFr}"/></td><td><c:out value="${j.supFr}"/></td><td class="${j.minutesAbsence > 0 ? 'late' : ''}"><c:out value="${j.absenceFr}"/></td><td><span class="badge <c:out value='${j.statutBadge}'/>"><c:out value="${j.statutLibelle}"/></span></td></tr></c:forEach>
          </tbody></table></div>
        </c:otherwise></c:choose>
      </div>
      <div class="card"><div class="card-head"><h2>Pointages du mois</h2></div>
        <c:choose><c:when test="${empty pointages}"><p class="muted">Aucun pointage.</p></c:when><c:otherwise>
          <div class="table-wrap"><table><thead><tr><th>Horodatage</th><th>Sens</th><th>Source</th><th>Motif</th><th></th></tr></thead><tbody>
            <c:forEach var="p" items="${pointages}"><tr><td><c:out value="${p.horodatageFr}"/></td><td><c:out value="${p.sens}"/></td><td><c:out value="${p.source}"/><c:if test="${p.manuel}"> par <c:out value="${p.saisiPar.nomComplet}"/></c:if></td><td class="small"><c:out value="${p.motif}"/></td>
              <td><form method="post" action="<c:url value='/rh/temps/pointages/${p.id}/annuler'/>" style="display:flex;gap:4px"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="hidden" name="idEmploye" value="${salarie.id}"><input type="hidden" name="mois" value="${mois}"><input type="text" name="motif" placeholder="Motif" required style="width:110px"><button class="btn btn-sm" type="submit">Annuler</button></form></td></tr></c:forEach>
          </tbody></table></div>
        </c:otherwise></c:choose>
      </div>
    </div>
    <div class="stack">
      <div class="card"><div class="card-head"><h2>Pointage manuel</h2></div>
        <form method="post" action="<c:url value='/rh/temps/pointages'/>" class="stack">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="hidden" name="idEmploye" value="${salarie.id}">
          <div class="field"><label>Date et heure *</label><input type="datetime-local" name="horodatage" required></div>
          <div class="field"><label>Sens *</label><select name="sens"><option value="ENTREE">Entrée</option><option value="SORTIE">Sortie</option></select></div>
          <div class="field"><label>Motif *</label><input type="text" name="motif" placeholder="Panne de badgeuse, oubli…" required></div>
          <span class="hint">Jamais pour soi-même (RG-PT04). La journée est recalculée.</span>
          <button class="btn btn-primary" type="submit">Enregistrer</button>
        </form>
      </div>
      <div class="card"><div class="card-head"><h2>Régime de travail</h2></div>
        <p class="small">Actuel : <b><c:out value="${empty regime ? 'aucun' : regime.regime.libelle}"/></b><c:if test="${not empty regime}"> depuis le <c:out value="${regime.dateDebut}"/></c:if></p>
        <form method="post" action="<c:url value='/rh/temps/regime'/>" class="stack">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="hidden" name="idEmploye" value="${salarie.id}">
          <div class="field"><label>Nouveau régime</label><select name="idRegime"><c:forEach var="r" items="${regimes}"><option value="${r.id}"><c:out value="${r.libelle}"/></option></c:forEach></select></div>
          <div class="field"><label>À partir du</label><input type="date" name="dateEffet" required></div>
          <button class="btn btn-sm" type="submit">Changer (période datée, RG-RT04)</button>
        </form>
      </div>
    </div>
  </div>
  </c:if>
</t:layout>
