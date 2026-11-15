<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Congés et absences" actif="conges-rh">
  <div class="page-head"><div><h1>Congés et absences</h1><p>Validation finale, absences et justificatifs, soldes.</p></div></div>
  <f:flash/>

  <div class="card" style="margin-bottom:16px"><div class="card-head"><h2>Demandes à valider (<c:out value="${demandes.size()}"/>)</h2></div>
    <c:choose><c:when test="${empty demandes}"><p class="muted">Aucune demande en attente du RH.</p></c:when><c:otherwise>
      <c:forEach var="d" items="${demandes}">
        <div class="doc-row" style="align-items:flex-start"><div class="grow">
          <b><c:out value="${d.employe.nomComplet}"/></b> — <c:out value="${d.type.libelle}"/> · <c:out value="${d.periodeLibelle}"/> (<c:out value="${d.nbJoursFr}"/>) <span class="badge <c:out value='${d.statutBadge}'/>"><c:out value="${d.statutLibelle}"/></span><c:if test="${d.escaladee}"> <span class="badge badge-bad">escaladée</span></c:if>
          <small><c:out value="${d.reference}"/><c:if test="${not empty d.valideurManager}"> · validée par <c:out value="${d.valideurManager.nomComplet}"/></c:if><c:if test="${not empty d.commentaireManager}"> : « <c:out value="${d.commentaireManager}"/> »</c:if></small>
          <c:if test="${d.type.justificatifObligatoire}"><small class="${empty d.idJustificatif ? 'late' : ''}">Justificatif : <c:out value="${empty d.idJustificatif ? 'manquant (RG-TC03)' : 'déposé, vérifié à la validation'}"/><c:if test="${not empty d.idJustificatif}"> — <a href="<c:url value='/rh/documents/${d.idJustificatif}/telecharger'/>">voir</a></c:if></small></c:if>
          <form method="post" action="<c:url value='/rh/conges/demandes/${d.id}/${d.statut == "SOUMISE" ? "valider-manager" : "valider"}'/>" style="display:flex;gap:6px;margin-top:6px;flex-wrap:wrap">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="text" name="commentaire" placeholder="Commentaire / motif du refus">
            <button class="btn btn-sm btn-ok" type="submit"><c:out value="${d.statut == 'SOUMISE' ? 'Valider l étape manager' : 'Valider'}"/></button>
            <button class="btn btn-sm btn-danger" type="submit" formaction="<c:url value='/rh/conges/demandes/${d.id}/refuser'/>">Refuser</button>
          </form>
        </div></div>
      </c:forEach>
    </c:otherwise></c:choose>
  </div>

  <div class="detail-grid">
    <div class="stack">
      <div class="card"><div class="card-head"><h2>Absences à décider (<c:out value="${absences.size()}"/>)</h2></div>
        <c:choose><c:when test="${empty absences}"><p class="muted">Aucune absence en attente.</p></c:when><c:otherwise>
          <c:forEach var="a" items="${absences}">
            <div class="doc-row" style="align-items:flex-start"><div class="grow">
              <b><c:out value="${a.employe.nomComplet}"/></b> — <c:out value="${a.type.libelle}"/> · <c:out value="${a.periodeLibelle}"/> <span class="badge <c:out value='${a.statutBadge}'/>"><c:out value="${a.statutLibelle}"/></span>
              <small><c:out value="${a.origineLibelle}"/><c:if test="${not empty a.commentaire}"> · <c:out value="${a.commentaire}"/></c:if></small>
              <small>Justificatif : <c:choose><c:when test="${empty a.idJustificatif}"><span class="${a.type.justificatifObligatoire ? 'late' : ''}">aucun<c:if test="${a.type.justificatifObligatoire}"> (attendu avant le <c:out value="${a.dateLimiteFr}"/>)</c:if></span></c:when><c:otherwise><a href="<c:url value='/rh/documents/${a.idJustificatif}/telecharger'/>">voir</a></c:otherwise></c:choose></small>
              <form method="post" action="<c:url value='/rh/conges/absences/${a.id}/decider'/>" class="stack" style="margin-top:6px">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <div style="display:flex;gap:6px;flex-wrap:wrap"><select name="decision"><option value="JUSTIFIER">Justifiée</option><option value="INJUSTIFIEE">Injustifiée</option><option value="CONVERTIR">Convertir en congé payé</option><option value="ANNULER">Annuler (erreur)</option></select>
                  <input type="text" name="commentaire" placeholder="Commentaire / motif"><label class="small"><input type="checkbox" name="accordSalarie" value="true"> accord du salarié (conversion)</label>
                  <button class="btn btn-sm btn-primary" type="submit">Décider</button></div>
              </form>
            </div></div>
          </c:forEach>
        </c:otherwise></c:choose>
      </div>

      <div class="card"><div class="card-head"><h2>Congés validés et en cours</h2></div>
        <c:choose><c:when test="${empty enCours}"><p class="muted">Aucun.</p></c:when><c:otherwise>
          <div class="table-wrap"><table><thead><tr><th>Salarié</th><th>Congé</th><th>Période</th><th>Statut</th><th>Interrompre</th></tr></thead><tbody>
            <c:forEach var="d" items="${enCours}"><tr><td><c:out value="${d.employe.nomComplet}"/></td><td><c:out value="${d.type.libelle}"/></td><td><c:out value="${d.periodeLibelle}"/></td><td><span class="badge <c:out value='${d.statutBadge}'/>"><c:out value="${d.statutLibelle}"/></span></td>
              <td><c:if test="${d.statut == 'EN_COURS'}"><form method="post" action="<c:url value='/rh/conges/demandes/${d.id}/interrompre'/>" style="display:flex;gap:4px;flex-wrap:wrap"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="date" name="dernierJour" title="Dernier jour de congé" required><input type="text" name="motif" placeholder="Motif" required style="width:110px"><button class="btn btn-sm" type="submit">OK</button></form></c:if></td></tr></c:forEach>
          </tbody></table></div>
        </c:otherwise></c:choose>
      </div>

      <div class="card"><div class="card-head"><h2>Soldes</h2></div>
        <div class="table-wrap"><table><thead><tr><th>Salarié</th><th>Congés payés (dispo / acquis / pris / réservé)</th><th>Permissions</th></tr></thead><tbody>
          <c:forEach var="e" items="${soldes}"><tr><td><c:out value="${e.key.nomComplet}"/></td>
            <td><b><c:out value="${e.value[0].disponibleFr}"/></b> <span class="small muted">/ <c:out value="${e.value[0].acquisFr}"/> / <c:out value="${e.value[0].prisFr}"/> / <c:out value="${e.value[0].reserveFr}"/></span></td>
            <td><b><c:out value="${e.value[1].disponibleFr}"/></b></td></tr></c:forEach>
        </tbody></table></div>
      </div>
    </div>

    <div class="stack">
      <div class="card"><div class="card-head"><h2>Saisir une absence</h2></div>
        <form method="post" action="<c:url value='/rh/conges/absences'/>" enctype="multipart/form-data" class="stack">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
          <div class="field"><label>Salarié *</label><select name="idEmploye" required><option value="">Choisir…</option><c:forEach var="e" items="${employes}"><option value="${e.id}"><c:out value="${e.nomComplet}"/></option></c:forEach></select></div>
          <div class="field"><label>Nature *</label><select name="idType"><c:forEach var="t" items="${typesAbsences}"><option value="${t.id}"><c:out value="${t.libelle}"/></option></c:forEach></select></div>
          <div class="form-grid"><div class="field"><label>Du *</label><input type="date" name="dateDebut" required></div><div class="field"><label>Au *</label><input type="date" name="dateFin" required></div></div>
          <div class="field"><label>Commentaire</label><input type="text" name="commentaire"></div>
          <div class="field"><label>Justificatif</label><input type="file" name="justificatif"></div>
          <button class="btn btn-primary" type="submit">Enregistrer</button>
        </form>
      </div>
      <div class="card"><div class="card-head"><h2>Ajuster un solde</h2></div>
        <form method="post" action="<c:url value='/rh/conges/soldes/ajuster'/>" class="stack">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
          <div class="field"><label>Salarié *</label><select name="idEmploye" required><option value="">Choisir…</option><c:forEach var="e" items="${employes}"><option value="${e.id}"><c:out value="${e.nomComplet}"/></option></c:forEach></select></div>
          <div class="form-grid"><div class="field"><label>Solde</label><select name="typeSolde"><option value="CONGE_PAYE">Congés payés</option><option value="PERMISSION">Permissions</option></select></div>
            <div class="field"><label>Jours (+/−) *</label><input type="number" step="0.5" name="jours" required></div></div>
          <div class="field"><label>Motif *</label><input type="text" name="motif" required placeholder="Reprise d'un solde antérieur…"></div>
          <span class="hint">Tracé comme mouvement AJUSTEMENT (RG-SC04).</span>
          <button class="btn btn-sm" type="submit">Ajuster</button>
        </form>
      </div>
      <div class="card"><div class="card-head"><h2>Types paramétrés</h2></div>
        <div class="table-wrap"><table style="font-size:.82rem"><thead><tr><th>Type</th><th>Solde</th><th>Rému.</th><th>Validation</th></tr></thead><tbody>
          <c:forEach var="t" items="${types}"><tr><td><c:out value="${t.libelle}"/><c:if test="${t.justificatifObligatoire}"> <span class="badge badge-info">justif.</span></c:if></td><td><c:out value="${empty t.typeSolde ? '—' : t.typeSolde}"/></td><td><c:out value="${t.remunerationLibelle}"/></td><td><c:out value="${t.validationManager ? 'manager + RH' : 'RH'}"/></td></tr></c:forEach>
        </tbody></table></div>
      </div>
    </div>
  </div>
</t:layout>
