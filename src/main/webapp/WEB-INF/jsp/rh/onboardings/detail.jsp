<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Onboarding ${detail.onboarding.reference}" actif="onboardings">
  <div class="page-head"><div><h1>Onboarding <c:out value="${detail.onboarding.reference}"/></h1><p><c:out value="${detail.onboarding.employeNom}"/> — <c:out value="${detail.onboarding.poste}"/></p></div><a class="btn" href="<c:url value='/rh/onboardings'/>">Retour</a></div>
  <f:flash/>
  <div class="stat-grid" style="margin-bottom:16px">
    <div class="stat-card accent"><span class="stat-label">Progression</span><span class="stat-value">${detail.onboarding.progression}%</span><div class="progress-line" style="background:rgba(255,255,255,.2)"><span style="width:${detail.onboarding.progression}%"></span></div></div>
    <div class="stat-card"><span class="stat-label">Tâches terminées</span><span class="stat-value">${detail.tachesTerminees}</span><span class="stat-note">${detail.tachesRestantes} restante(s)</span></div>
    <div class="stat-card ${detail.tachesEnRetard > 0 ? 'warn' : ''}"><span class="stat-label">En retard</span><span class="stat-value">${detail.tachesEnRetard}</span><span class="stat-note">échéances dépassées</span></div>
    <div class="stat-card"><span class="stat-label">Statut</span><span class="stat-value" style="font-size:1.1rem"><span class="badge <c:out value='${detail.onboarding.statutBadge}'/>"><c:out value="${detail.onboarding.statutLibelle}"/></span></span><span class="stat-note"><c:out value="${detail.onboarding.modeleNom}"/> v<c:out value="${detail.onboarding.versionModele}"/></span></div>
  </div>
  <div class="detail-grid">
    <div class="stack">
      <div class="card">
        <div class="card-head"><h2>Tâches du parcours</h2><span class="muted small">${detail.tachesTerminees}/${detail.taches.size()} terminées</span></div>
        <c:choose>
          <c:when test="${empty detail.taches}"><p class="muted">Aucune tâche générée.</p></c:when>
          <c:otherwise>
            <div class="task-list">
              <c:forEach var="t" items="${detail.taches}">
                <div class="task ${t.enRetard ? 'late' : ''} ${t.bloquee ? 'blocked' : ''}">
                  <div class="task-main">
                    <div class="task-title"><c:out value="${t.titre}"/> <c:if test="${t.obligatoire}"><span class="chip">Obligatoire</span></c:if><c:if test="${t.bloquante}"><span class="chip">Bloquante</span></c:if><span class="badge <c:out value='${t.statutBadge}'/>"><c:out value="${t.statutLibelle}"/></span></div>
                    <c:if test="${not empty t.description}"><div class="task-desc"><c:out value="${t.description}"/></div></c:if>
                    <div class="task-meta"><span>Responsable : <b><c:out value="${t.responsableNom}"/></b></span><span>Rôle : <b><c:out value="${t.roleLibelle}"/></b></span><span>Échéance : <b class="${t.enRetard ? 'late' : ''}"><c:out value="${t.dateEcheanceFr}"/></b></span><c:if test="${not empty t.dependanceTitre}"><span>Après : <b><c:out value="${t.dependanceTitre}"/></b></span></c:if></div>
                    <c:if test="${not empty t.commentaire}"><div class="task-desc"><c:out value="${t.commentaire}"/></div></c:if>
                    <c:if test="${not empty t.motifBlocage}"><div class="task-desc late">Blocage : <c:out value="${t.motifBlocage}"/></div></c:if>
                  </div>
                  <div class="task-actions">
                    <c:if test="${not t.terminee and not t.annulee}">
                      <c:if test="${t.statut == 'A_FAIRE' and t.dependancePrete}"><form method="post" action="<c:url value='/rh/onboardings/${detail.onboarding.id}/taches/${t.id}/demarrer'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-sm" type="submit">Démarrer</button></form></c:if>
                      <c:if test="${t.statut == 'BLOQUEE'}"><form method="post" action="<c:url value='/rh/onboardings/${detail.onboarding.id}/taches/${t.id}/debloquer'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-sm btn-ok" type="submit">Débloquer</button></form></c:if>
                      <c:if test="${t.statut != 'BLOQUEE' and t.dependancePrete}"><form method="post" action="<c:url value='/rh/onboardings/${detail.onboarding.id}/taches/${t.id}/terminer'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="hidden" name="commentaire" value=""><button class="btn btn-sm btn-ok" type="submit">Terminer</button></form></c:if>
                      <details><summary class="btn btn-sm">Plus</summary><div class="stack" style="margin-top:8px;min-width:220px">
                        <c:if test="${t.statut != 'BLOQUEE'}"><form method="post" action="<c:url value='/rh/onboardings/${detail.onboarding.id}/taches/${t.id}/bloquer'/>" class="stack"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="text" name="motif" placeholder="Motif du blocage" required><button class="btn btn-sm btn-danger btn-block" type="submit">Bloquer</button></form></c:if>
                        <form method="post" action="<c:url value='/rh/onboardings/${detail.onboarding.id}/taches/${t.id}/reassigner'/>" class="stack"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><select name="idResponsable" required><option value="">Réassigner à…</option><c:forEach var="e" items="${employes}"><option value="${e.id}"><c:out value="${e.nomComplet}"/></option></c:forEach></select><button class="btn btn-sm btn-block" type="submit">Réassigner</button></form>
                      </div></details>
                    </c:if>
                    <c:if test="${t.terminee}"><span class="muted small">Terminée</span></c:if>
                  </div>
                </div>
              </c:forEach>
            </div>
          </c:otherwise>
        </c:choose>
      </div>
      <c:if test="${not empty detail.historique}"><div class="card"><div class="card-head"><h2>Historique</h2></div><ul class="timeline compact"><c:forEach var="h" items="${detail.historique}"><li><b><c:out value="${h.action}"/></b><br><time><c:out value="${h.dateEvenementFr}"/> — <c:out value="${h.auteurNom}"/></time><c:if test="${not empty h.commentaire}"><div class="small muted"><c:out value="${h.commentaire}"/></div></c:if></li></c:forEach></ul></div></c:if>
    </div>
    <div class="stack">
      <div class="card"><div class="card-head"><h2>Informations</h2></div><dl class="meta-list">
        <div><dt>Salarié</dt><dd><a href="<c:url value='/rh/salaries/${detail.onboarding.idEmploye}'/>"><c:out value="${detail.onboarding.employeNom}"/></a></dd></div>
        <div><dt>Matricule</dt><dd><c:out value="${detail.onboarding.employeMatricule}"/></dd></div>
        <div><dt>Contrat</dt><dd><a href="<c:url value='/rh/contrats/${detail.onboarding.idContrat}'/>"><c:out value="${detail.onboarding.contratReference}"/></a></dd></div>
        <div><dt>Département</dt><dd><c:out value="${detail.onboarding.departement}"/></dd></div>
        <div><dt>Début prévu</dt><dd><c:out value="${detail.onboarding.dateDebutPrevueFr}"/></dd></div>
        <div><dt>Début réel</dt><dd><c:out value="${detail.onboarding.dateDebutReelleFr}"/></dd></div>
        <div><dt>Fin prévue</dt><dd><c:out value="${detail.onboarding.dateFinPrevueFr}"/></dd></div>
        <div><dt>Responsable RH</dt><dd><c:out value="${detail.onboarding.responsableRhNom}"/></dd></div>
        <c:if test="${not empty detail.onboarding.clotureParNom}"><div><dt>Clôturé par</dt><dd><c:out value="${detail.onboarding.clotureParNom}"/></dd></div><div><dt>Clôturé le</dt><dd><c:out value="${detail.onboarding.dateClotureFr}"/></dd></div></c:if>
        <c:if test="${not empty detail.onboarding.motifAnnulation}"><div><dt>Motif d'annulation</dt><dd><c:out value="${detail.onboarding.motifAnnulation}"/></dd></div></c:if>
      </dl></div>
      <div class="card"><div class="card-head"><h2>Actions</h2></div><div class="stack">
        <c:if test="${detail.peutCloturer}"><form method="post" action="<c:url value='/rh/onboardings/${detail.onboarding.id}/cloturer'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-ok btn-block" type="submit">Clôturer l'onboarding</button></form></c:if>
        <c:if test="${detail.onboarding.statut == 'A_VERIFIER' and not detail.peutCloturer and not empty detail.raisonsNonCloture}"><div class="alert alert-warn"><b>Clôture impossible pour le moment</b><ul><c:forEach var="r" items="${detail.raisonsNonCloture}" varStatus="st"><c:if test="${st.index < 5}"><li><c:out value="${r}"/></li></c:if></c:forEach><c:if test="${detail.raisonsNonCloture.size() > 5}"><li>et ${detail.raisonsNonCloture.size() - 5} autre(s) point(s) à traiter.</li></c:if></ul></div></c:if>
        <c:if test="${detail.onboarding.statut == 'CLOTURE'}"><form method="post" action="<c:url value='/rh/onboardings/${detail.onboarding.id}/rouvrir'/>" class="stack"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="text" name="motif" placeholder="Motif de réouverture" required><button class="btn btn-block" type="submit">Rouvrir</button></form></c:if>
        <c:if test="${detail.onboarding.statut != 'CLOTURE' and detail.onboarding.statut != 'ANNULE'}"><form method="post" action="<c:url value='/rh/onboardings/${detail.onboarding.id}/annuler'/>" class="stack"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="text" name="motif" placeholder="Motif d'annulation" required><button class="btn btn-danger btn-block" type="submit">Annuler l'onboarding</button></form></c:if>
        <a class="btn btn-block" href="<c:url value='/rh/documents/nouveau'><c:param name='idEmploye' value='${detail.onboarding.idEmploye}'/><c:param name='idContrat' value='${detail.onboarding.idContrat}'/></c:url>">Déposer un document</a>
      </div></div>
    </div>
  </div>
</t:layout>
