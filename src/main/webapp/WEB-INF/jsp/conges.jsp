<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Mes congés" actif="conges">
  <c:choose>
  <c:when test="${sansEmploye}"><div class="page-head"><div><h1>Mes congés</h1></div></div><div class="card empty-state"><b>Aucun salarié</b>Votre compte n'est rattaché à aucun salarié.</div></c:when>
  <c:otherwise>
  <div class="page-head"><div><h1>Mes congés</h1><p>Validation : <c:out value="${empty manager ? 'RH' : manager.nomComplet}"/> puis RH.</p></div></div>
  <f:flash/>
  <div class="stat-grid" style="margin-bottom:16px">
    <c:forEach var="s" items="${soldes}">
      <div class="stat-card"><span class="stat-label"><c:out value="${s.libelle}"/></span><span class="stat-value"><c:out value="${s.disponibleFr}"/></span><span class="stat-note">acquis <c:out value="${s.acquisFr}"/> · pris <c:out value="${s.prisFr}"/> · réservé <c:out value="${s.reserveFr}"/></span></div>
    </c:forEach>
  </div>
  <div class="detail-grid">
    <div class="stack">
      <div class="card"><div class="card-head"><h2>Nouvelle demande de congé</h2></div>
        <form method="post" action="<c:url value='/conges/demandes'/>" enctype="multipart/form-data">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
          <div class="form-grid">
            <div class="field span2"><label for="idType">Type *</label><select id="idType" name="idType" required><c:forEach var="t" items="${typesConges}"><option value="${t.id}"><c:out value="${t.libelle}"/><c:if test="${t.delaiPrevenanceJours > 0}"> — prévenance <c:out value="${t.delaiPrevenanceJours}"/> j</c:if><c:if test="${t.justificatifObligatoire}"> — justificatif</c:if></option></c:forEach></select></div>
            <div class="field"><label for="dateDebut">Du *</label><input id="dateDebut" type="date" name="dateDebut" min="${aujourdhui}" required></div>
            <div class="field"><label for="demiJourneeDebut">À partir du</label><select id="demiJourneeDebut" name="demiJourneeDebut"><option value="MATIN">matin</option><option value="APRES_MIDI">après-midi</option></select></div>
            <div class="field"><label for="dateFin">Au *</label><input id="dateFin" type="date" name="dateFin" min="${aujourdhui}" required></div>
            <div class="field"><label for="demiJourneeFin">Jusqu'au</label><select id="demiJourneeFin" name="demiJourneeFin"><option value="APRES_MIDI">soir</option><option value="MATIN">midi</option></select></div>
            <div class="field span2"><label for="motif">Motif</label><input id="motif" type="text" name="motif"></div>
            <div class="field span2"><label for="justificatif">Justificatif</label><input id="justificatif" type="file" name="justificatif" accept=".pdf,.png,.jpg,.jpeg,.doc,.docx"><span class="hint">Exigé pour une permission, la maternité ou la paternité ; il peut aussi être ajouté plus tard.</span></div>
          </div>
          <p class="small muted">Le nombre de jours est calculé automatiquement ; les jours sont réservés dès l'envoi.</p>
          <div class="form-pied"><button class="btn btn-primary" type="submit">Envoyer la demande</button></div>
        </form>
      </div>

      <div class="card"><div class="card-head"><h2>Mes demandes</h2></div>
        <c:choose><c:when test="${empty demandes}"><p class="muted">Aucune demande.</p></c:when><c:otherwise>
          <c:forEach var="d" items="${demandes}">
            <div class="doc-row" style="align-items:flex-start"><div class="grow"><b><c:out value="${d.type.libelle}"/></b> — <c:out value="${d.periodeLibelle}"/> (<c:out value="${d.nbJoursFr}"/>) <span class="badge <c:out value='${d.statutBadge}'/>"><c:out value="${d.statutLibelle}"/></span>
              <small><c:out value="${d.reference}"/><c:if test="${empty d.idJustificatif and d.type.justificatifObligatoire}"> · <span class="late">justificatif manquant</span></c:if></small>
              <c:if test="${not empty d.motifRefus}"><small class="late">Refus : <c:out value="${d.motifRefus}"/></small></c:if>
              <c:if test="${not empty d.motifAnnulation}"><small><c:out value="${d.motifAnnulation}"/></small></c:if>
              <div style="display:flex;gap:6px;margin-top:6px;flex-wrap:wrap">
                <c:if test="${d.annulableParSalarie}"><form method="post" action="<c:url value='/conges/demandes/${d.id}/annuler'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-sm" type="submit">Annuler</button></form></c:if>
                <c:if test="${(d.statut == 'SOUMISE' or d.statut == 'VALIDEE_MANAGER') and d.type.justificatifObligatoire}"><form method="post" action="<c:url value='/conges/demandes/${d.id}/justificatif'/>" enctype="multipart/form-data" style="display:flex;gap:6px"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="file" name="fichier" required><button class="btn btn-sm" type="submit">Ajouter le justificatif</button></form></c:if>
              </div>
            </div></div>
          </c:forEach>
        </c:otherwise></c:choose>
      </div>
    </div>

    <div class="stack">
      <div class="card"><div class="card-head"><h2>Déclarer une absence</h2></div>
        <form method="post" action="<c:url value='/conges/absences'/>" enctype="multipart/form-data" class="stack">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
          <div class="field"><label for="idTypeAbs">Nature *</label><select id="idTypeAbs" name="idType" required><c:forEach var="t" items="${typesAbsences}"><option value="${t.id}"><c:out value="${t.libelle}"/></option></c:forEach></select></div>
          <div class="form-grid"><div class="field"><label>Du *</label><input type="date" name="dateDebut" required></div><div class="field"><label>Au *</label><input type="date" name="dateFin" required></div></div>
          <div class="field"><label>Commentaire</label><input type="text" name="commentaire"></div>
          <div class="field"><label>Justificatif (certificat médical…)</label><input type="file" name="justificatif" accept=".pdf,.png,.jpg,.jpeg"><span class="hint">Une maladie doit être justifiée sous 2 jours. Votre manager ne voit pas le motif médical.</span></div>
          <button class="btn btn-primary" type="submit">Déclarer</button>
        </form>
      </div>
      <div class="card"><div class="card-head"><h2>Mes absences</h2></div>
        <c:choose><c:when test="${empty absences}"><p class="muted">Aucune absence.</p></c:when><c:otherwise>
          <c:forEach var="a" items="${absences}">
            <div class="doc-row" style="align-items:flex-start"><div class="grow"><b><c:out value="${a.type.libelle}"/></b> — <c:out value="${a.periodeLibelle}"/> <span class="badge <c:out value='${a.statutBadge}'/>"><c:out value="${a.statutLibelle}"/></span>
              <c:if test="${a.enAttente and empty a.idJustificatif and a.type.justificatifObligatoire}"><small class="late">Justificatif attendu avant le <c:out value="${a.dateLimiteFr}"/></small>
                <form method="post" action="<c:url value='/conges/absences/${a.id}/justificatif'/>" enctype="multipart/form-data" style="display:flex;gap:6px;margin-top:6px"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="file" name="fichier" required><button class="btn btn-sm" type="submit">Déposer</button></form></c:if>
              <c:if test="${not empty a.decisionCommentaire}"><small><c:out value="${a.decisionCommentaire}"/></small></c:if>
            </div></div>
          </c:forEach>
        </c:otherwise></c:choose>
      </div>
      <div class="card"><div class="card-head"><h2>Mouvements de solde</h2></div>
        <c:choose><c:when test="${empty mouvements}"><p class="muted">Aucun mouvement.</p></c:when><c:otherwise>
          <ul class="timeline compact"><c:forEach var="m" items="${mouvements}"><li><time><c:out value="${m.dateFr}"/></time><br><b><c:out value="${m.jours}"/> j</b> <c:out value="${m.typeSolde == 'CONGE_PAYE' ? 'CP' : 'permission'}"/> — <c:out value="${m.typeLibelle}"/><c:if test="${not empty m.motif}"><div class="small muted"><c:out value="${m.motif}"/></div></c:if></li></c:forEach></ul>
        </c:otherwise></c:choose>
      </div>
    </div>
  </div>
  </c:otherwise>
  </c:choose>
</t:layout>
