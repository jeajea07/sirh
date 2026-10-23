<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Pilotage RH" actif="tableau-rh">
  <div class="page-head">
    <div><h1>Pilotage RH</h1><p>Contrats, intégrations, formalités et documents à traiter.</p></div>
    <div class="page-actions"><a class="btn" href="<c:url value='/rh/contrats/echeances'/>">Échéances</a><a class="btn btn-primary" href="<c:url value='/rh/contrats/nouveau'/>">Nouveau contrat</a></div>
  </div>
  <f:flash/>

  <div class="stat-grid">
    <a class="stat-card" href="<c:url value='/rh/contrats?statut=BROUILLON'/>" style="text-decoration:none;color:inherit"><span class="stat-label">Contrats en préparation</span><span class="stat-value">${tableau.contratsEnPreparation}</span><span class="stat-note">brouillons à compléter</span></a>
    <a class="stat-card ${tableau.contratsAValider > 0 ? 'warn' : ''}" href="<c:url value='/rh/contrats?statut=A_VALIDER'/>" style="text-decoration:none;color:inherit"><span class="stat-label">À valider</span><span class="stat-value">${tableau.contratsAValider}</span><span class="stat-note">circuit de validation</span></a>
    <a class="stat-card" href="<c:url value='/rh/contrats?statut=A_SIGNER'/>" style="text-decoration:none;color:inherit"><span class="stat-label">À signer</span><span class="stat-value">${tableau.contratsASigner}</span><span class="stat-note">signatures attendues</span></a>
    <a class="stat-card" href="<c:url value='/rh/contrats/echeances'/>" style="text-decoration:none;color:inherit"><span class="stat-label">Échéances 30 j</span><span class="stat-value">${tableau.contratsEcheance30j}</span><span class="stat-note">contrats à renouveler</span></a>
  </div>
  <div class="stat-grid" style="margin-top:12px">
    <a class="stat-card" href="<c:url value='/rh/onboardings?statut=EN_COURS'/>" style="text-decoration:none;color:inherit"><span class="stat-label">Onboardings en cours</span><span class="stat-value">${tableau.onboardingsEnCours}</span><span class="stat-note">${tableau.arrivees30j} arrivée(s) sous 30 j</span></a>
    <a class="stat-card ${tableau.tachesEnRetard > 0 ? 'warn' : ''}" href="<c:url value='/mes-taches'/>" style="text-decoration:none;color:inherit"><span class="stat-label">Tâches en retard</span><span class="stat-value">${tableau.tachesEnRetard}</span><span class="stat-note">toutes équipes</span></a>
    <a class="stat-card" href="<c:url value='/rh/formalites?statut=A_PREPARER'/>" style="text-decoration:none;color:inherit"><span class="stat-label">Formalités en attente</span><span class="stat-value">${tableau.formalitesEnAttente}</span><span class="stat-note">à préparer ou transmettre</span></a>
    <a class="stat-card ${tableau.documentsARevoir > 0 ? 'warn' : ''}" href="<c:url value='/rh/documents?statut=RECU'/>" style="text-decoration:none;color:inherit"><span class="stat-label">Documents à revoir</span><span class="stat-value">${tableau.documentsARevoir}</span><span class="stat-note">vérification RH</span></a>
  </div>

  <div class="detail-grid" style="margin-top:20px">
    <div class="stack">
      <div class="card">
        <div class="card-head"><h2>Prochaines échéances de contrat</h2><a class="btn btn-sm" href="<c:url value='/rh/contrats/echeances'/>">Tout voir</a></div>
        <c:choose><c:when test="${empty tableau.prochainesEcheances}"><p class="muted">Aucune échéance dans les 30 prochains jours.</p></c:when><c:otherwise><div class="table-wrap"><table><thead><tr><th>Référence</th><th>Salarié</th><th>Fin</th><th>Reste</th><th></th></tr></thead><tbody>
          <c:forEach var="e" items="${tableau.prochainesEcheances}"><tr><td><b><c:out value="${e.contrat.reference}"/></b></td><td><c:out value="${e.contrat.employeNom}"/></td><td><c:out value="${e.contrat.dateFinFr}"/></td><td class="${e.joursRestants <= 7 ? 'late' : ''}">${e.joursRestants} j</td><td class="right"><a class="btn btn-sm" href="<c:url value='/rh/contrats/${e.contrat.id}'/>">Ouvrir</a></td></tr></c:forEach>
        </tbody></table></div></c:otherwise></c:choose>
      </div>
      <div class="card">
        <div class="card-head"><h2>Tâches en retard</h2><a class="btn btn-sm" href="<c:url value='/mes-taches'/>">Mes tâches</a></div>
        <c:choose><c:when test="${empty tableau.tachesEnRetardListe}"><p class="muted">Aucune tâche en retard.</p></c:when><c:otherwise><div class="task-list"><c:forEach var="t" items="${tableau.tachesEnRetardListe}"><div class="task late"><div class="task-main"><div class="task-title"><c:out value="${t.titre}"/> <span class="badge <c:out value='${t.statutBadge}'/>"><c:out value="${t.statutLibelle}"/></span></div><div class="task-meta"><span><c:out value="${t.employeNom}"/></span><span>Responsable : <b><c:out value="${t.responsableNom}"/></b></span><span>Échéance : <b class="late"><c:out value="${t.dateEcheanceFr}"/></b></span></div></div><div class="task-actions"><a class="btn btn-sm" href="<c:url value='/rh/onboardings/${t.idOnboarding}'/>">Ouvrir</a></div></div></c:forEach></div></c:otherwise></c:choose>
      </div>
    </div>
    <div class="card">
      <div class="card-head"><h2>Onboardings récents</h2><a class="btn btn-sm" href="<c:url value='/rh/onboardings'/>">Tout voir</a></div>
      <c:choose><c:when test="${empty tableau.onboardingsRecents}"><p class="muted">Aucun onboarding récent.</p></c:when><c:otherwise><div class="stack">
        <c:forEach var="o" items="${tableau.onboardingsRecents}"><a class="doc-row" href="<c:url value='/rh/onboardings/${o.id}'/>" style="text-decoration:none;color:inherit"><div class="doc-ico">RH</div><div class="grow"><b><c:out value="${o.employeNom}"/></b><small><c:out value="${o.reference}"/> · début <c:out value="${o.dateDebutPrevueFr}"/></small></div><span class="badge <c:out value='${o.statutBadge}'/>">${o.progression}%</span></a></c:forEach>
      </div></c:otherwise></c:choose>
      <div class="form-section"><a class="btn btn-block" href="<c:url value='/rh/onboardings/nouveau'/>">Créer un onboarding</a></div>
    </div>
  </div>
</t:layout>
