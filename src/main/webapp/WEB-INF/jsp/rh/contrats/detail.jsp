<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Contrat ${detail.contrat.reference}" actif="contrats">
  <div class="page-head">
    <div><h1>Contrat <c:out value="${detail.contrat.reference}"/></h1><p><c:out value="${detail.contrat.employeNom}"/> — <c:out value="${detail.contrat.typeContratLibelle}"/></p></div>
    <div class="page-actions">
      <a class="btn" href="<c:url value='/rh/contrats'/>">Retour</a>
      <c:if test="${detail.contrat.statutWorkflow == 'BROUILLON'}"><a class="btn" href="<c:url value='/rh/contrats/${detail.contrat.id}/modifier'/>">Modifier</a></c:if>
    </div>
  </div>
  <f:flash/>

  <div class="detail-grid">
    <div class="stack">
      <div class="card">
        <div class="card-head"><h2>Informations du contrat</h2><span class="badge <c:out value='${detail.contrat.statutBadge}'/>"><c:out value="${detail.contrat.statutLibelle}"/></span></div>
        <dl class="meta-list">
          <div><dt>Salarié</dt><dd><a href="<c:url value='/rh/salaries/${detail.contrat.idEmploye}'/>"><c:out value="${detail.contrat.employeNom}"/></a></dd></div>
          <div><dt>Matricule</dt><dd><c:out value="${detail.contrat.employeMatricule}"/></dd></div>
          <div><dt>Type</dt><dd><c:out value="${detail.contrat.typeContratLibelle}"/></dd></div>
          <div><dt>Opération</dt><dd><c:out value="${detail.contrat.typeOperationLibelle}"/></dd></div>
          <div><dt>Date de début</dt><dd><c:out value="${detail.contrat.dateDebutFr}"/></dd></div>
          <div><dt>Date de fin</dt><dd><c:out value="${detail.contrat.dateFinFr}"/></dd></div>
          <div><dt>Signature</dt><dd><c:out value="${detail.contrat.dateSignatureFr}"/></dd></div>
          <div><dt>Salaire</dt><dd style="font-variant-numeric:tabular-nums"><c:out value="${detail.contrat.salaireFr}"/></dd></div>
          <div><dt>Fonction</dt><dd><c:out value="${detail.contrat.fonction}"/></dd></div>
          <div><dt>Catégorie</dt><dd><c:out value="${detail.contrat.categorieProfessionnelle}"/></dd></div>
          <div><dt>Statut contractuel</dt><dd><c:out value="${detail.contrat.statutContractuelLibelle}"/></dd></div>
          <div><dt>Version</dt><dd><c:out value="${detail.contrat.version}"/></dd></div>
          <c:if test="${not empty detail.contrat.contratPrecedentReference}"><div><dt>Contrat précédent</dt><dd><c:out value="${detail.contrat.contratPrecedentReference}"/></dd></div></c:if>
        </dl>
        <c:if test="${not empty detail.contrat.commentaire}"><p class="muted" style="margin-top:14px;white-space:pre-line"><c:out value="${detail.contrat.commentaire}"/></p></c:if>
      </div>

      <div class="card">
        <div class="card-head"><h2>Signatures</h2></div>
        <c:choose>
          <c:when test="${empty detail.signatures}"><p class="muted">Aucune signature enregistrée.</p></c:when>
          <c:otherwise>
            <div class="table-wrap"><table><thead><tr><th>Partie</th><th>Signataire</th><th>Date</th><th>Statut</th></tr></thead><tbody>
              <c:forEach var="s" items="${detail.signatures}"><tr><td><c:out value="${s.partieLibelle}"/></td><td><c:out value="${s.nomSignataire}"/></td><td><c:out value="${s.dateSignatureFr}"/></td><td><span class="badge <c:out value='${s.statutBadge}'/>"><c:out value="${s.statutLibelle}"/></span></td></tr></c:forEach>
            </tbody></table></div>
          </c:otherwise>
        </c:choose>
        <c:if test="${detail.peutSigner}">
          <form method="post" action="<c:url value='/rh/contrats/${detail.contrat.id}/signer'/>" class="form-grid" style="margin-top:16px">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <div class="field"><label for="partie">Partie *</label><select id="partie" name="partie" required><option value="">Choisir…</option><option value="SALARIE">Salarié</option><option value="ENTREPRISE">Entreprise</option></select></div>
            <div class="field"><label for="nomSignataire">Nom du signataire *</label><input id="nomSignataire" type="text" name="nomSignataire" required></div>
            <div class="field"><label for="dateSignatureAction">Date de signature *</label><input id="dateSignatureAction" type="date" name="dateSignature" required></div>
            <div class="field" style="justify-content:flex-end"><button class="btn btn-primary" type="submit">Enregistrer la signature</button></div>
          </form>
        </c:if>
      </div>

      <div class="card">
        <div class="card-head"><h2>Documents</h2><a class="btn btn-sm" href="<c:url value='/rh/documents/nouveau'><c:param name='idEmploye' value='${detail.contrat.idEmploye}'/><c:param name='idContrat' value='${detail.contrat.id}'/></c:url>">Ajouter</a></div>
        <c:choose>
          <c:when test="${empty detail.documents}"><p class="muted">Aucun document rattaché.</p></c:when>
          <c:otherwise>
            <c:forEach var="d" items="${detail.documents}">
              <div class="doc-row">
                <div class="doc-ico">DOC</div>
                <div class="grow"><b><c:out value="${d.titre}"/></b><small><c:out value="${d.typeLibelle}"/> · v<c:out value="${d.version}"/> · <c:out value="${d.tailleFr}"/></small></div>
                <span class="badge <c:out value='${d.statutBadge}'/>"><c:out value="${d.statutLibelle}"/></span>
                <a class="btn btn-sm" href="<c:url value='/rh/documents/${d.id}/telecharger'/>">Télécharger</a>
              </div>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </div>

      <c:if test="${not empty detail.avenants}">
        <div class="card">
          <div class="card-head"><h2>Avenants et renouvellements</h2></div>
          <div class="table-wrap"><table><thead><tr><th>Référence</th><th>Opération</th><th>Début</th><th>Fin</th><th>Statut</th><th></th></tr></thead><tbody>
            <c:forEach var="a" items="${detail.avenants}"><tr><td><c:out value="${a.reference}"/></td><td><c:out value="${a.typeOperationLibelle}"/></td><td><c:out value="${a.dateDebutFr}"/></td><td><c:out value="${a.dateFinFr}"/></td><td><span class="badge <c:out value='${a.statutBadge}'/>"><c:out value="${a.statutWorkflow}"/></span></td><td class="right"><a class="btn btn-sm" href="<c:url value='/rh/contrats/${a.id}'/>">Ouvrir</a></td></tr></c:forEach>
          </tbody></table></div>
        </div>
      </c:if>
    </div>

    <div class="stack">
      <div class="card">
        <div class="card-head"><h2>Actions</h2></div>
        <div class="stack">
          <c:if test="${detail.peutSoumettre}">
            <form method="post" action="<c:url value='/rh/contrats/${detail.contrat.id}/soumettre'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-primary btn-block" type="submit">Soumettre à validation</button></form>
          </c:if>
          <c:if test="${detail.peutValider}">
            <form method="post" action="<c:url value='/rh/contrats/${detail.contrat.id}/valider'/>" class="stack"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><textarea name="commentaire" placeholder="Commentaire de validation (facultatif)"></textarea><button class="btn btn-ok btn-block" type="submit">Valider</button></form>
            <form method="post" action="<c:url value='/rh/contrats/${detail.contrat.id}/renvoyer'/>" class="stack"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><textarea name="commentaire" placeholder="Motif du renvoi" required></textarea><button class="btn btn-danger btn-block" type="submit">Renvoyer au rédacteur</button></form>
          </c:if>
          <c:if test="${detail.peutActiver}">
            <form method="post" action="<c:url value='/rh/contrats/${detail.contrat.id}/activer'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-ok btn-block" type="submit">Activer le contrat</button></form>
          </c:if>
          <c:if test="${detail.peutCreerAvenant}"><a class="btn btn-block" href="<c:url value='/rh/contrats/${detail.contrat.id}/avenant'/>">Créer un avenant</a></c:if>
          <c:if test="${detail.peutRenouveler}"><a class="btn btn-block" href="<c:url value='/rh/contrats/${detail.contrat.id}/renouveler'/>">Renouveler</a></c:if>
          <c:if test="${detail.peutTerminer}">
            <form method="post" action="<c:url value='/rh/contrats/${detail.contrat.id}/terminer'/>" class="stack"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="date" name="dateFin" required><textarea name="motif" placeholder="Motif de fin de contrat" required></textarea><button class="btn btn-danger btn-block" type="submit">Terminer le contrat</button></form>
          </c:if>
          <c:if test="${detail.peutAnnuler}">
            <form method="post" action="<c:url value='/rh/contrats/${detail.contrat.id}/annuler'/>" class="stack"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><textarea name="motif" placeholder="Motif d'annulation" required></textarea><button class="btn btn-danger btn-block" type="submit">Annuler le contrat</button></form>
          </c:if>
          <c:if test="${not detail.peutSoumettre and not detail.peutValider and not detail.peutActiver and not detail.peutTerminer and not detail.peutAnnuler}"><p class="muted">Aucune action disponible pour ce statut.</p></c:if>
        </div>
      </div>

      <c:if test="${not empty detail.validations}">
        <div class="card">
          <div class="card-head"><h2>Circuit de validation</h2></div>
          <ul class="timeline compact">
            <c:forEach var="v" items="${detail.validations}">
              <li><b><c:out value="${v.etapeLibelle}"/></b> <span class="badge <c:out value='${v.decisionBadge}'/>"><c:out value="${v.decisionLibelle}"/></span><br><time><c:out value="${v.dateReceptionFr}"/></time><c:if test="${not empty v.validateurNom}"><div class="small"><c:out value="${v.validateurNom}"/></div></c:if><c:if test="${not empty v.commentaire}"><div class="small muted"><c:out value="${v.commentaire}"/></div></c:if></li>
            </c:forEach>
          </ul>
        </div>
      </c:if>

      <c:if test="${not empty detail.historique}">
        <div class="card">
          <div class="card-head"><h2>Historique</h2></div>
          <ul class="timeline compact">
            <c:forEach var="h" items="${detail.historique}">
              <li><b><c:out value="${h.action}"/></b><br><time><c:out value="${h.dateEvenementFr}"/> — <c:out value="${h.auteurNom}"/></time><c:if test="${not empty h.commentaire}"><div class="small muted"><c:out value="${h.commentaire}"/></div></c:if></li>
            </c:forEach>
          </ul>
        </div>
      </c:if>
    </div>
  </div>
</t:layout>
