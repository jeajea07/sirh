<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Dossier salarié" actif="salaries">
  <c:set var="statut" value="${detail.statutAdministratif}"/>
  <div class="page-head">
    <div><h1><c:out value="${detail.salarie.nomComplet}"/> <span class="badge <c:out value='${detail.salarie.statutBadge}'/>"><c:out value="${detail.salarie.statutLibelle}"/></span></h1><p><c:out value="${detail.salarie.matricule}"/> · <c:out value="${detail.salarie.poste}"/> · <c:out value="${detail.salarie.departement}"/></p></div>
    <div class="page-actions">
      <a class="btn" href="<c:url value='/rh/salaries'/>">Retour</a>
      <c:if test="${not detail.sorti}"><a class="btn" href="<c:url value='/rh/salaries/${detail.salarie.id}/modifier'/>">Modifier</a></c:if>
    </div>
  </div>
  <f:flash/>
  <c:if test="${detail.sorti}"><div class="alert alert-info" style="margin-bottom:16px">Salarié sorti le <c:out value="${detail.dateSortieFr}"/> (<c:out value="${detail.motifSortie}"/>). Le dossier est archivé en lecture seule.</div></c:if>
  <c:if test="${not empty detail.alertes and not detail.sorti}"><div class="alert alert-warn" style="margin-bottom:16px"><c:forEach var="a" items="${detail.alertes}"><div><c:out value="${a}"/></div></c:forEach></div></c:if>

  <%-- Situation actuelle, calculée à partir des périodes en vigueur (Bloc 5 §6.2) --%>
  <div class="stat-grid" style="margin-bottom:16px">
    <div class="stat-card"><span class="stat-label">Ancienneté</span><span class="stat-value" style="font-size:1.25rem"><c:out value="${detail.anciennete}"/></span><span class="stat-note">Embauche le <c:out value="${detail.salarie.dateEmbaucheFr}"/></span></div>
    <div class="stat-card"><span class="stat-label">Contrat en vigueur</span>
      <c:choose><c:when test="${empty detail.contratEnVigueur}"><span class="stat-value" style="font-size:1.25rem">—</span><span class="stat-note">aucun</span></c:when>
      <c:otherwise><span class="stat-value" style="font-size:1.25rem"><c:out value="${detail.contratEnVigueur.typeContrat}"/></span><span class="stat-note"><c:out value="${detail.contratEnVigueur.reference}"/><c:if test="${not empty detail.contratEnVigueur.dateFin}"> · fin <c:out value="${detail.contratEnVigueur.dateFinFr}"/></c:if></span></c:otherwise></c:choose>
    </div>
    <div class="stat-card"><span class="stat-label">Salaire de base</span><span class="stat-value" style="font-size:1.25rem"><c:out value="${empty detail.salaireEnVigueur ? '—' : detail.salaireEnVigueur.salaireFr}"/></span><span class="stat-note"><c:out value="${empty detail.salaireEnVigueur ? 'aucun salaire en vigueur' : 'depuis le '.concat(detail.salaireEnVigueur.dateEffetFr)}"/></span></div>
    <div class="stat-card ${detail.dossierIncomplet ? 'warn' : ''}"><span class="stat-label">Pièces obligatoires</span><span class="stat-value"><c:out value="${detail.pieces.size() - detail.nombrePiecesBloquantes}"/>/<c:out value="${detail.pieces.size()}"/></span><span class="stat-note"><c:out value="${detail.dossierIncomplet ? 'dossier incomplet' : 'dossier complet'}"/></span></div>
  </div>

  <div class="detail-grid">
    <div class="stack">
      <div class="card"><div class="card-head"><h2>Identité et coordonnées</h2></div>
        <dl class="meta-list">
          <div><dt>Nom complet</dt><dd><c:out value="${detail.salarie.nomComplet}"/></dd></div>
          <div><dt>Matricule</dt><dd><c:out value="${detail.salarie.matricule}"/></dd></div>
          <div><dt>Sexe</dt><dd><c:out value="${detail.sexeLibelle}"/></dd></div>
          <div><dt>Nationalité</dt><dd><c:out value="${empty detail.nationalite ? '—' : detail.nationalite}"/></dd></div>
          <div><dt>Email</dt><dd><c:out value="${detail.salarie.email}"/></dd></div>
          <div><dt>Téléphone</dt><dd><c:out value="${detail.salarie.telephone}"/></dd></div>
          <div><dt>CIN</dt><dd><c:out value="${detail.cin}"/></dd></div>
          <div><dt>Date de naissance</dt><dd><c:out value="${detail.dateNaissanceFr}"/></dd></div>
          <div><dt>Lieu de naissance</dt><dd><c:out value="${detail.lieuNaissance}"/></dd></div>
          <div><dt>Situation familiale</dt><dd><c:out value="${detail.situationFamiliale}"/></dd></div>
          <div><dt>Enfants</dt><dd><c:out value="${detail.nombreEnfants}"/></dd></div>
          <div><dt>Adresse</dt><dd><c:out value="${detail.adresse}"/></dd></div>
          <div><dt>Contact d'urgence</dt><dd><c:out value="${empty detail.contactUrgenceNom ? '—' : detail.contactUrgenceNom}"/> <c:out value="${detail.contactUrgenceTelephone}"/></dd></div>
          <div><dt>N° CNaPS</dt><dd><c:out value="${detail.numeroCnaps}"/></dd></div>
          <div><dt>N° OSTIE</dt><dd><c:out value="${detail.numeroOstie}"/></dd></div>
          <div><dt>RIB</dt><dd><c:out value="${detail.rib}"/></dd></div>
        </dl>
      </div>

      <div class="card"><div class="card-head"><h2>Contrats</h2><c:if test="${not detail.sorti}"><a class="btn btn-sm" href="<c:url value='/rh/contrats/nouveau'><c:param name='idEmploye' value='${detail.salarie.id}'/></c:url>">Nouveau contrat</a></c:if></div>
        <c:choose><c:when test="${empty detail.contrats}"><p class="muted">Aucun contrat.</p></c:when><c:otherwise><div class="table-wrap"><table><thead><tr><th>Référence</th><th>Type</th><th>Opération</th><th>Période</th><th>Statut</th><th></th></tr></thead><tbody>
          <c:forEach var="c" items="${detail.contrats}"><tr><td><b><c:out value="${c.reference}"/></b></td><td><c:out value="${c.typeContrat}"/></td><td><c:out value="${c.typeOperationLibelle}"/></td><td><c:out value="${c.dateDebutFr}"/> → <c:out value="${c.dateFinFr}"/></td><td><span class="badge <c:out value='${c.statutBadge}'/>"><c:out value="${c.statutWorkflow}"/></span></td><td class="right"><a class="btn btn-sm" href="<c:url value='/rh/contrats/${c.id}'/>">Ouvrir</a></td></tr></c:forEach>
        </tbody></table></div></c:otherwise></c:choose>
      </div>

      <div class="card"><div class="card-head"><h2>Affectations</h2><c:if test="${not detail.sorti}"><a class="btn btn-sm" href="<c:url value='/rh/affectations/nouveau'><c:param name='idEmploye' value='${detail.salarie.id}'/></c:url>">Changer d'affectation</a></c:if></div>
        <c:choose><c:when test="${empty detail.affectations}"><p class="muted">Aucune affectation.</p></c:when><c:otherwise><div class="table-wrap"><table><thead><tr><th>Poste</th><th>Département</th><th>Responsable</th><th>Période</th><th>Statut</th><th></th></tr></thead><tbody>
          <c:forEach var="a" items="${detail.affectations}"><tr><td><c:out value="${a.poste}"/></td><td><c:out value="${a.departement}"/></td><td><c:out value="${a.responsableNom}"/></td><td><c:out value="${a.periodeLibelle}"/></td><td><span class="badge <c:out value='${a.statutBadge}'/>"><c:out value="${a.statutLibelle}"/></span></td><td class="right"><a class="btn btn-sm" href="<c:url value='/rh/affectations/${a.id}'/>">Ouvrir</a></td></tr></c:forEach>
        </tbody></table></div></c:otherwise></c:choose>
      </div>

      <div class="card"><div class="card-head"><h2>Historique de salaire</h2></div>
        <c:choose><c:when test="${empty detail.historiqueSalaires}"><p class="muted">Aucun salaire enregistré : le premier salaire est repris du contrat à son entrée en vigueur.</p></c:when><c:otherwise><div class="table-wrap"><table><thead><tr><th>Salaire de base</th><th>Période</th><th>Motif</th><th>Commentaire</th></tr></thead><tbody>
          <c:forEach var="h" items="${detail.historiqueSalaires}"><tr><td><b><c:out value="${h.salaireFr}"/></b></td><td><c:out value="${h.dateEffetFr}"/> → <c:out value="${h.dateFinFr}"/></td><td><span class="badge ${h.enCours ? 'badge-ok' : ''}"><c:out value="${h.motifLibelle}"/></span></td><td class="small"><c:out value="${h.commentaire}"/></td></tr></c:forEach>
        </tbody></table></div></c:otherwise></c:choose>
      </div>

      <div class="card"><div class="card-head"><h2>Pièces obligatoires</h2></div>
        <div class="table-wrap"><table><thead><tr><th>Pièce</th><th>État</th><th>Expiration</th><th></th></tr></thead><tbody>
          <c:forEach var="p" items="${detail.pieces}"><tr><td><c:out value="${p.libelle}"/></td><td><span class="badge <c:out value='${p.etatBadge}'/>"><c:out value="${p.etatLibelle}"/></span></td><td><c:out value="${p.dateExpirationFr}"/></td><td class="right">
            <c:if test="${not empty p.idDocument}"><a class="btn btn-sm" href="<c:url value='/rh/documents/${p.idDocument}/telecharger'/>">Voir</a></c:if>
            <c:if test="${not detail.sorti and p.etat != 'VALIDE' and p.etat != 'EN_VERIFICATION'}"><a class="btn btn-sm btn-primary" href="<c:url value='/rh/documents/nouveau'><c:param name='idEmploye' value='${detail.salarie.id}'/><c:param name='idTypeAdministratif' value='${p.idType}'/></c:url>">Déposer</a></c:if>
          </td></tr></c:forEach>
        </tbody></table></div>
      </div>

      <div class="card"><div class="card-head"><h2>Documents</h2><c:if test="${not detail.sorti}"><a class="btn btn-sm" href="<c:url value='/rh/documents/nouveau'><c:param name='idEmploye' value='${detail.salarie.id}'/></c:url>">Déposer</a></c:if></div>
        <c:choose><c:when test="${empty detail.documents}"><p class="muted">Aucun document.</p></c:when><c:otherwise><c:forEach var="d" items="${detail.documents}"><div class="doc-row"><div class="doc-ico">DOC</div><div class="grow"><b><c:out value="${d.titre}"/></b> <span class="muted small">v<c:out value="${d.version}"/></span><small><c:out value="${d.typeLibelle}"/> · déposé le <c:out value="${d.dateDepotFr}"/><c:if test="${d.dateExpirationFr != '—'}"> · expire le <c:out value="${d.dateExpirationFr}"/></c:if></small></div><span class="badge <c:out value='${d.statutBadge}'/>"><c:out value="${d.statutLibelle}"/></span><a class="btn btn-sm" href="<c:url value='/rh/documents/${d.id}/telecharger'/>">Télécharger</a></div></c:forEach></c:otherwise></c:choose>
      </div>

      <div class="card"><div class="card-head"><h2>Onboardings</h2><c:if test="${not detail.sorti}"><a class="btn btn-sm" href="<c:url value='/rh/onboardings/nouveau'><c:param name='idEmploye' value='${detail.salarie.id}'/></c:url>">Nouveau parcours</a></c:if></div>
        <c:choose><c:when test="${empty detail.onboardings}"><p class="muted">Aucun onboarding.</p></c:when><c:otherwise><div class="table-wrap"><table><thead><tr><th>Référence</th><th>Début</th><th>Progression</th><th>Statut</th><th></th></tr></thead><tbody>
          <c:forEach var="o" items="${detail.onboardings}"><tr><td><b><c:out value="${o.reference}"/></b></td><td><c:out value="${o.dateDebutPrevueFr}"/></td><td><div class="progress-cell"><div class="progress-line"><span style="width:${o.progression}%"></span></div><div class="progress-text"><span>${o.progression}%</span><span>${o.tachesTerminees}/${o.nombreTaches}</span></div></div></td><td><span class="badge <c:out value='${o.statutBadge}'/>"><c:out value="${o.statutLibelle}"/></span></td><td class="right"><a class="btn btn-sm" href="<c:url value='/rh/onboardings/${o.id}'/>">Ouvrir</a></td></tr></c:forEach>
        </tbody></table></div></c:otherwise></c:choose>
      </div>

      <div class="card"><div class="card-head"><h2>Formalités sociales</h2><c:if test="${not detail.sorti}"><a class="btn btn-sm" href="<c:url value='/rh/formalites/nouvelle'><c:param name='idEmploye' value='${detail.salarie.id}'/></c:url>">Nouvelle déclaration</a></c:if></div>
        <c:choose><c:when test="${empty detail.formalites}"><p class="muted">Aucune formalité.</p></c:when><c:otherwise><div class="table-wrap"><table><thead><tr><th>Organisme</th><th>Type</th><th>Statut</th><th>Référence</th><th></th></tr></thead><tbody>
          <c:forEach var="d" items="${detail.formalites}"><tr><td><c:out value="${d.organisme}"/></td><td><c:out value="${d.typeDeclaration}"/></td><td><span class="badge <c:out value='${d.statutBadge}'/>"><c:out value="${d.statutLibelle}"/></span></td><td><c:out value="${d.referenceExterne}"/></td><td class="right"><a class="btn btn-sm" href="<c:url value='/rh/formalites/${d.id}'/>">Ouvrir</a></td></tr></c:forEach>
        </tbody></table></div></c:otherwise></c:choose>
      </div>
    </div>

    <div class="stack">
      <c:if test="${not detail.sorti}">
      <div class="card"><div class="card-head"><h2>Statut administratif</h2></div>
        <p class="small muted" style="margin-top:0">Statut actuel : <span class="badge <c:out value='${detail.salarie.statutBadge}'/>"><c:out value="${detail.salarie.statutLibelle}"/></span></p>
        <c:if test="${statut == 'EN_PREAVIS'}">
          <dl class="kv small" style="margin-bottom:12px"><dt>Notifiée le</dt><dd><c:out value="${detail.dateNotificationSortieFr}"/></dd><dt>Sortie le</dt><dd><c:out value="${detail.dateSortieFr}"/></dd><dt>Motif</dt><dd><c:out value="${detail.motifSortie}"/></dd></dl>
          <details><summary class="btn btn-sm btn-block">Annuler la sortie</summary>
            <form method="post" action="<c:url value='/rh/salaries/${detail.salarie.id}/sortie/annuler'/>" class="stack" style="margin-top:8px">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
              <div class="field"><label for="motifAnnulation">Motif *</label><input id="motifAnnulation" type="text" name="motif" required></div>
              <button class="btn btn-sm btn-block" type="submit">Confirmer l'annulation</button>
            </form>
          </details>
        </c:if>
        <c:if test="${statut == 'ACTIF'}">
          <details><summary class="btn btn-sm btn-block">Suspendre le contrat</summary>
            <form method="post" action="<c:url value='/rh/salaries/${detail.salarie.id}/suspendre'/>" class="stack" style="margin-top:8px">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
              <div class="field"><label for="motifSuspension">Motif *</label><input id="motifSuspension" type="text" name="motif" placeholder="Congé maternité, maladie longue…" required></div>
              <div class="field"><label for="dateSuspension">À partir du</label><input id="dateSuspension" type="date" name="dateEffet" value="${aujourdhui}"></div>
              <button class="btn btn-sm btn-block" type="submit">Confirmer la suspension</button>
            </form>
          </details>
        </c:if>
        <c:if test="${statut == 'SUSPENDU'}">
          <form method="post" action="<c:url value='/rh/salaries/${detail.salarie.id}/reprendre'/>" class="stack">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <div class="field"><label for="dateReprise">Reprise le</label><input id="dateReprise" type="date" name="dateEffet" value="${aujourdhui}"></div>
            <button class="btn btn-sm btn-ok btn-block" type="submit">Enregistrer la reprise</button>
          </form>
        </c:if>
        <c:if test="${statut != 'EN_PREAVIS'}">
          <details style="margin-top:8px"><summary class="btn btn-sm btn-block">Enregistrer une sortie</summary>
            <form method="post" action="<c:url value='/rh/salaries/${detail.salarie.id}/sortie'/>" class="stack" style="margin-top:8px">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
              <div class="field"><label for="motifSortie">Motif *</label><select id="motifSortie" name="motif" required><option value="">Choisir…</option><c:forEach var="m" items="${motifsSortie}"><option value="${m.key}"><c:out value="${m.value}"/></option></c:forEach></select></div>
              <div class="field"><label for="dateNotification">Notifiée le *</label><input id="dateNotification" type="date" name="dateNotification" value="${aujourdhui}" max="${aujourdhui}" required></div>
              <div class="field"><label for="dateSortie">Date de sortie *</label><input id="dateSortie" type="date" name="dateSortie" required></div>
              <div class="field"><label for="commentaireSortie">Commentaire</label><textarea id="commentaireSortie" name="commentaire" rows="2"></textarea></div>
              <span class="hint">Si la date est atteinte, la sortie est appliquée tout de suite : contrat terminé, affectation clôturée, accès coupés. Sinon le salarié passe « En préavis ».</span>
              <button class="btn btn-sm btn-danger btn-block" type="submit">Enregistrer la sortie</button>
            </form>
          </details>
        </c:if>
      </div>

      <c:if test="${not empty detail.historiqueSalaires}">
      <div class="card"><div class="card-head"><h2>Réviser le salaire</h2></div>
        <form method="post" action="<c:url value='/rh/salaries/${detail.salarie.id}/salaire'/>" class="stack">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
          <div class="field"><label for="salaire">Nouveau salaire de base *</label><input id="salaire" type="number" min="1" step="0.01" name="salaire" required><span class="hint">Dernière ligne : <c:out value="${detail.historiqueSalaires[0].salaireFr}"/> à partir du <c:out value="${detail.historiqueSalaires[0].dateEffetFr}"/></span></div>
          <div class="field"><label for="dateEffetSalaire">Date d'effet *</label><input id="dateEffetSalaire" type="date" name="dateEffet" value="${aujourdhui}" required></div>
          <div class="field"><label for="motifSalaire">Motif *</label><select id="motifSalaire" name="motif" required>
            <option value="AUGMENTATION_INDIVIDUELLE">Augmentation individuelle</option>
            <option value="AUGMENTATION_GENERALE">Augmentation générale</option>
            <option value="PROMOTION">Promotion</option>
            <option value="CORRECTION">Correction d'erreur</option>
          </select></div>
          <div class="field"><label for="commentaireSalaire">Commentaire</label><textarea id="commentaireSalaire" name="commentaire" rows="2"></textarea></div>
          <span class="hint">Une baisse n'est possible que par un avenant signé (ou une correction justifiée). L'ancienne ligne est clôturée, jamais écrasée.</span>
          <button class="btn btn-sm btn-primary btn-block" type="submit">Enregistrer la révision</button>
        </form>
      </div>
      </c:if>
      </c:if>

      <div class="card"><div class="card-head"><h2>Historique de carrière</h2></div>
        <c:choose><c:when test="${empty detail.carriere}"><p class="muted">Aucun événement.</p></c:when><c:otherwise>
          <ul class="timeline compact"><c:forEach var="e" items="${detail.carriere}"><li><time><c:out value="${e.dateEffetFr}"/></time><br><span class="badge <c:out value='${e.typeBadge}'/>"><c:out value="${e.typeLibelle}"/></span> <b><c:out value="${e.libelle}"/></b>
            <c:if test="${not empty e.ancienneValeur or not empty e.nouvelleValeur}"><div class="small muted"><c:out value="${e.ancienneValeur}"/><c:if test="${not empty e.ancienneValeur and not empty e.nouvelleValeur}"> → </c:if><c:out value="${e.nouvelleValeur}"/></div></c:if>
            <div class="small muted">par <c:out value="${e.auteurNom}"/></div></li></c:forEach></ul>
        </c:otherwise></c:choose>
      </div>
    </div>
  </div>
</t:layout>
