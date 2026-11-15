<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Mon dossier" actif="mon-dossier">
  <c:choose>
  <c:when test="${sansEmploye}">
    <div class="page-head"><div><h1>Mon dossier</h1></div></div>
    <div class="card empty-state"><b>Aucun dossier</b>Votre compte n'est rattaché à aucun salarié.</div>
  </c:when>
  <c:otherwise>
  <div class="page-head">
    <div><h1>Mon dossier</h1><p><c:out value="${detail.salarie.matricule}"/> · <c:out value="${detail.salarie.poste}"/> · <c:out value="${detail.salarie.departement}"/></p></div>
    <span class="badge <c:out value='${detail.salarie.statutBadge}'/>"><c:out value="${detail.salarie.statutLibelle}"/></span>
  </div>
  <f:flash/>
  <c:if test="${detail.dossierIncomplet}"><div class="alert alert-warn" style="margin-bottom:16px">Votre dossier est incomplet : <c:out value="${detail.nombrePiecesBloquantes}"/> pièce(s) obligatoire(s) à fournir ou à renouveler (voir ci-dessous).</div></c:if>

  <div class="stat-grid" style="margin-bottom:16px">
    <div class="stat-card"><span class="stat-label">Ancienneté</span><span class="stat-value" style="font-size:1.25rem"><c:out value="${detail.anciennete}"/></span><span class="stat-note">Embauche le <c:out value="${detail.salarie.dateEmbaucheFr}"/></span></div>
    <div class="stat-card"><span class="stat-label">Contrat</span><span class="stat-value" style="font-size:1.25rem"><c:out value="${empty detail.contratEnVigueur ? '—' : detail.contratEnVigueur.typeContrat}"/></span><span class="stat-note"><c:if test="${not empty detail.contratEnVigueur}"><c:out value="${detail.contratEnVigueur.reference}"/><c:if test="${not empty detail.contratEnVigueur.dateFin}"> · fin <c:out value="${detail.contratEnVigueur.dateFinFr}"/></c:if></c:if></span></div>
    <div class="stat-card"><span class="stat-label">Salaire de base</span><span class="stat-value" style="font-size:1.25rem"><c:out value="${empty detail.salaireEnVigueur ? '—' : detail.salaireEnVigueur.salaireFr}"/></span><span class="stat-note">brut mensuel</span></div>
    <div class="stat-card ${detail.dossierIncomplet ? 'warn' : ''}"><span class="stat-label">Pièces obligatoires</span><span class="stat-value"><c:out value="${detail.pieces.size() - detail.nombrePiecesBloquantes}"/>/<c:out value="${detail.pieces.size()}"/></span><span class="stat-note"><c:out value="${detail.dossierIncomplet ? 'à compléter' : 'complet'}"/></span></div>
  </div>

  <div class="detail-grid">
    <div class="stack">
      <div class="card"><div class="card-head"><h2>Mes informations</h2></div>
        <dl class="meta-list">
          <div><dt>Nom complet</dt><dd><c:out value="${detail.salarie.nomComplet}"/></dd></div>
          <div><dt>Email</dt><dd><c:out value="${detail.salarie.email}"/></dd></div>
          <div><dt>Téléphone</dt><dd><c:out value="${detail.salarie.telephone}"/></dd></div>
          <div><dt>CIN</dt><dd><c:out value="${detail.cin}"/></dd></div>
          <div><dt>Date de naissance</dt><dd><c:out value="${detail.dateNaissanceFr}"/></dd></div>
          <div><dt>Situation familiale</dt><dd><c:out value="${detail.situationFamiliale}"/></dd></div>
          <div><dt>Enfants</dt><dd><c:out value="${detail.nombreEnfants}"/></dd></div>
          <div><dt>Adresse</dt><dd><c:out value="${detail.adresse}"/></dd></div>
          <div><dt>Contact d'urgence</dt><dd><c:out value="${empty detail.contactUrgenceNom ? '—' : detail.contactUrgenceNom}"/> <c:out value="${detail.contactUrgenceTelephone}"/></dd></div>
          <div><dt>N° CNaPS</dt><dd><c:out value="${detail.numeroCnaps}"/></dd></div>
          <div><dt>RIB</dt><dd><c:out value="${detail.rib}"/></dd></div>
        </dl>
        <p class="small muted">Une information est inexacte ? Signalez-la au service RH avec le justificatif : toute modification d'une donnée sensible est validée par le RH.</p>
      </div>

      <div class="card"><div class="card-head"><h2>Mes pièces obligatoires</h2></div>
        <div class="table-wrap"><table><thead><tr><th>Pièce</th><th>État</th><th>Expiration</th><th></th></tr></thead><tbody>
          <c:forEach var="p" items="${detail.pieces}"><tr><td><c:out value="${p.libelle}"/></td><td><span class="badge <c:out value='${p.etatBadge}'/>"><c:out value="${p.etatLibelle}"/></span></td><td><c:out value="${p.dateExpirationFr}"/></td>
            <td class="right"><c:if test="${p.etat != 'VALIDE' and p.etat != 'EN_VERIFICATION'}"><a class="btn btn-sm btn-primary" href="<c:url value='/mon-dossier'><c:param name='piece' value='${p.idType}'/></c:url>#deposer">Déposer</a></c:if></td></tr></c:forEach>
        </tbody></table></div>
      </div>

      <div class="card" id="deposer"><div class="card-head"><h2>Déposer une pièce</h2></div>
        <form method="post" action="<c:url value='/mon-dossier/documents'/>" enctype="multipart/form-data">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
          <div class="form-grid">
            <div class="field span2"><label for="idTypeAdministratif">Pièce *</label>
              <select id="idTypeAdministratif" name="idTypeAdministratif" required>
                <option value="">Choisir…</option>
                <c:forEach var="t" items="${typesPieces}"><option value="${t.id}" ${pieceChoisie == t.id ? 'selected' : ''}><c:out value="${t.libelle}"/></option></c:forEach>
              </select>
            </div>
            <div class="field"><label for="dateEmission">Date d'émission</label><input id="dateEmission" type="date" name="dateEmission"></div>
            <div class="field"><label for="dateExpiration">Date d'expiration</label><input id="dateExpiration" type="date" name="dateExpiration"><span class="hint">Obligatoire pour une pièce à durée de validité, sauf si elle se calcule depuis la date d'émission.</span></div>
            <div class="field span2"><label for="fichier">Fichier *</label><input id="fichier" type="file" name="fichier" required accept=".pdf,.png,.jpg,.jpeg,.doc,.docx"><span class="hint">PDF, image ou Word, 10 Mo maximum.</span></div>
            <div class="field span2"><label for="commentaire">Commentaire</label><textarea id="commentaire" name="commentaire" rows="2"></textarea></div>
          </div>
          <div class="form-pied"><button class="btn btn-primary" type="submit">Déposer</button></div>
        </form>
      </div>

      <div class="card"><div class="card-head"><h2>Mes documents</h2></div>
        <c:choose><c:when test="${empty detail.documents}"><p class="muted">Aucun document.</p></c:when><c:otherwise><c:forEach var="d" items="${detail.documents}"><div class="doc-row"><div class="doc-ico">DOC</div><div class="grow"><b><c:out value="${d.titre}"/></b><small><c:out value="${d.typeLibelle}"/> · <c:out value="${d.dateDepotFr}"/><c:if test="${d.dateExpirationFr != '—'}"> · expire le <c:out value="${d.dateExpirationFr}"/></c:if></small><c:if test="${not empty d.motifRejet}"><small class="late">Rejeté : <c:out value="${d.motifRejet}"/></small></c:if></div><span class="badge <c:out value='${d.statutBadge}'/>"><c:out value="${d.statutLibelle}"/></span><a class="btn btn-sm" href="<c:url value='/mon-dossier/documents/${d.id}'/>">Télécharger</a></div></c:forEach></c:otherwise></c:choose>
      </div>
    </div>

    <div class="stack">
      <div class="card"><div class="card-head"><h2>Mes contrats</h2></div>
        <c:choose><c:when test="${empty detail.contrats}"><p class="muted">Aucun contrat.</p></c:when><c:otherwise>
          <c:forEach var="c" items="${detail.contrats}"><div class="doc-row"><div class="grow"><b><c:out value="${c.reference}"/></b> — <c:out value="${c.typeContrat}"/><small><c:out value="${c.typeOperationLibelle}"/> · <c:out value="${c.dateDebutFr}"/> → <c:out value="${c.dateFinFr}"/></small></div><span class="badge <c:out value='${c.statutBadge}'/>"><c:out value="${c.statutWorkflow}"/></span></div></c:forEach>
        </c:otherwise></c:choose>
      </div>
      <div class="card"><div class="card-head"><h2>Mes affectations</h2></div>
        <c:choose><c:when test="${empty detail.affectations}"><p class="muted">Aucune affectation.</p></c:when><c:otherwise>
          <c:forEach var="a" items="${detail.affectations}"><div class="doc-row"><div class="grow"><b><c:out value="${a.poste}"/></b> — <c:out value="${a.departement}"/><small>Responsable : <c:out value="${a.responsableNom}"/> · <c:out value="${a.periodeLibelle}"/></small></div><span class="badge <c:out value='${a.statutBadge}'/>"><c:out value="${a.statutLibelle}"/></span></div></c:forEach>
        </c:otherwise></c:choose>
      </div>
      <c:if test="${not empty detail.historiqueSalaires}">
      <div class="card"><div class="card-head"><h2>Mon salaire</h2></div>
        <ul class="timeline compact"><c:forEach var="h" items="${detail.historiqueSalaires}"><li><time><c:out value="${h.dateEffetFr}"/> → <c:out value="${h.dateFinFr}"/></time><br><b><c:out value="${h.salaireFr}"/></b> <span class="small muted"><c:out value="${h.motifLibelle}"/></span></li></c:forEach></ul>
      </div>
      </c:if>
      <div class="card"><div class="card-head"><h2>Mon parcours</h2></div>
        <c:choose><c:when test="${empty detail.carriere}"><p class="muted">Aucun événement.</p></c:when><c:otherwise>
          <ul class="timeline compact"><c:forEach var="e" items="${detail.carriere}"><li><time><c:out value="${e.dateEffetFr}"/></time><br><span class="badge <c:out value='${e.typeBadge}'/>"><c:out value="${e.typeLibelle}"/></span> <c:out value="${e.libelle}"/></li></c:forEach></ul>
        </c:otherwise></c:choose>
      </div>
    </div>
  </div>
  </c:otherwise>
  </c:choose>
</t:layout>
