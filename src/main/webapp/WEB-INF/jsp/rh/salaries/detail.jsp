<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Dossier salarié" actif="salaries">
  <div class="page-head">
    <div><h1><c:out value="${detail.salarie.nomComplet}"/></h1><p><c:out value="${detail.salarie.matricule}"/> · <c:out value="${detail.salarie.poste}"/> · <c:out value="${detail.salarie.departement}"/></p></div>
    <div class="page-actions">
      <a class="btn" href="<c:url value='/rh/salaries'/>">Retour</a>
      <a class="btn" href="<c:url value='/rh/salaries/${detail.salarie.id}/modifier'/>">Modifier</a>
    </div>
  </div>
  <f:flash/>
  <div class="stat-grid" style="margin-bottom:16px">
    <div class="stat-card"><span class="stat-label">Statut</span><span class="stat-value" style="font-size:1.25rem"><span class="badge <c:out value='${detail.salarie.statutBadge}'/>"><c:out value="${detail.salarie.statutLibelle}"/></span></span><span class="stat-note">Depuis le <c:out value="${detail.salarie.dateEmbaucheFr}"/></span></div>
    <div class="stat-card"><span class="stat-label">Contrats</span><span class="stat-value"><c:out value="${detail.contrats.size()}"/></span><span class="stat-note">dont contrats en vigueur</span></div>
    <div class="stat-card"><span class="stat-label">Onboardings</span><span class="stat-value"><c:out value="${detail.onboardings.size()}"/></span><span class="stat-note">parcours d'intégration</span></div>
    <div class="stat-card"><span class="stat-label">Documents</span><span class="stat-value"><c:out value="${detail.documents.size()}"/></span><span class="stat-note">pièces au dossier</span></div>
  </div>
  <div class="detail-grid">
    <div class="stack">
      <div class="card"><div class="card-head"><h2>Identité et coordonnées</h2></div>
        <dl class="meta-list">
          <div><dt>Nom complet</dt><dd><c:out value="${detail.salarie.nomComplet}"/></dd></div>
          <div><dt>Matricule</dt><dd><c:out value="${detail.salarie.matricule}"/></dd></div>
          <div><dt>Email</dt><dd><c:out value="${detail.salarie.email}"/></dd></div>
          <div><dt>Téléphone</dt><dd><c:out value="${detail.salarie.telephone}"/></dd></div>
          <div><dt>CIN</dt><dd><c:out value="${detail.cin}"/></dd></div>
          <div><dt>Date de naissance</dt><dd><c:out value="${detail.dateNaissanceFr}"/></dd></div>
          <div><dt>Lieu de naissance</dt><dd><c:out value="${detail.lieuNaissance}"/></dd></div>
          <div><dt>Situation familiale</dt><dd><c:out value="${detail.situationFamiliale}"/></dd></div>
          <div><dt>Enfants</dt><dd><c:out value="${detail.nombreEnfants}"/></dd></div>
          <div><dt>Adresse</dt><dd><c:out value="${detail.adresse}"/></dd></div>
          <div><dt>N° CNaPS</dt><dd><c:out value="${detail.numeroCnaps}"/></dd></div>
          <div><dt>N° OSTIE</dt><dd><c:out value="${detail.numeroOstie}"/></dd></div>
          <div><dt>RIB</dt><dd><c:out value="${detail.rib}"/></dd></div>
          <div><dt>Embauche</dt><dd><c:out value="${detail.salarie.dateEmbaucheFr}"/></dd></div>
        </dl>
      </div>
      <div class="card"><div class="card-head"><h2>Contrats</h2><a class="btn btn-sm" href="<c:url value='/rh/contrats/nouveau'><c:param name='idEmploye' value='${detail.salarie.id}'/></c:url>">Nouveau contrat</a></div>
        <c:choose><c:when test="${empty detail.contrats}"><p class="muted">Aucun contrat.</p></c:when><c:otherwise><div class="table-wrap"><table><thead><tr><th>Référence</th><th>Type</th><th>Période</th><th>Statut</th><th></th></tr></thead><tbody>
          <c:forEach var="c" items="${detail.contrats}"><tr><td><b><c:out value="${c.reference}"/></b></td><td><c:out value="${c.typeContrat}"/></td><td><c:out value="${c.dateDebutFr}"/> → <c:out value="${c.dateFinFr}"/></td><td><span class="badge <c:out value='${c.statutBadge}'/>"><c:out value="${c.statutWorkflow}"/></span></td><td class="right"><a class="btn btn-sm" href="<c:url value='/rh/contrats/${c.id}'/>">Ouvrir</a></td></tr></c:forEach>
        </tbody></table></div></c:otherwise></c:choose>
      </div>
      <div class="card"><div class="card-head"><h2>Affectations</h2><a class="btn btn-sm" href="<c:url value='/rh/affectations/nouveau'><c:param name='idEmploye' value='${detail.salarie.id}'/></c:url>">Nouvelle affectation</a></div>
        <c:choose><c:when test="${empty detail.affectations}"><p class="muted">Aucune affectation.</p></c:when><c:otherwise><div class="table-wrap"><table><thead><tr><th>Poste</th><th>Département</th><th>Période</th><th>Statut</th><th></th></tr></thead><tbody>
          <c:forEach var="a" items="${detail.affectations}"><tr><td><c:out value="${a.poste}"/></td><td><c:out value="${a.departement}"/></td><td><c:out value="${a.periodeLibelle}"/></td><td><span class="badge <c:out value='${a.statutBadge}'/>"><c:out value="${a.statutLibelle}"/></span></td><td class="right"><a class="btn btn-sm" href="<c:url value='/rh/affectations/${a.id}'/>">Ouvrir</a></td></tr></c:forEach>
        </tbody></table></div></c:otherwise></c:choose>
      </div>
      <div class="card"><div class="card-head"><h2>Onboardings</h2><a class="btn btn-sm" href="<c:url value='/rh/onboardings/nouveau'><c:param name='idEmploye' value='${detail.salarie.id}'/></c:url>">Nouveau parcours</a></div>
        <c:choose><c:when test="${empty detail.onboardings}"><p class="muted">Aucun onboarding.</p></c:when><c:otherwise><div class="table-wrap"><table><thead><tr><th>Référence</th><th>Début</th><th>Progression</th><th>Statut</th><th></th></tr></thead><tbody>
          <c:forEach var="o" items="${detail.onboardings}"><tr><td><b><c:out value="${o.reference}"/></b></td><td><c:out value="${o.dateDebutPrevueFr}"/></td><td><div class="progress-cell"><div class="progress-line"><span style="width:${o.progression}%"></span></div><div class="progress-text"><span>${o.progression}%</span><span>${o.tachesTerminees}/${o.nombreTaches}</span></div></div></td><td><span class="badge <c:out value='${o.statutBadge}'/>"><c:out value="${o.statutLibelle}"/></span></td><td class="right"><a class="btn btn-sm" href="<c:url value='/rh/onboardings/${o.id}'/>">Ouvrir</a></td></tr></c:forEach>
        </tbody></table></div></c:otherwise></c:choose>
      </div>
      <div class="card"><div class="card-head"><h2>Documents</h2><a class="btn btn-sm" href="<c:url value='/rh/documents/nouveau'><c:param name='idEmploye' value='${detail.salarie.id}'/></c:url>">Déposer</a></div>
        <c:choose><c:when test="${empty detail.documents}"><p class="muted">Aucun document.</p></c:when><c:otherwise><c:forEach var="d" items="${detail.documents}"><div class="doc-row"><div class="doc-ico">DOC</div><div class="grow"><b><c:out value="${d.titre}"/></b><small><c:out value="${d.typeLibelle}"/> · <c:out value="${d.dateDepotFr}"/></small></div><span class="badge <c:out value='${d.statutBadge}'/>"><c:out value="${d.statutLibelle}"/></span><a class="btn btn-sm" href="<c:url value='/rh/documents/${d.id}/telecharger'/>">Télécharger</a></div></c:forEach></c:otherwise></c:choose>
      </div>
      <div class="card"><div class="card-head"><h2>Formalités sociales</h2><a class="btn btn-sm" href="<c:url value='/rh/formalites/nouvelle'><c:param name='idEmploye' value='${detail.salarie.id}'/></c:url>">Nouvelle déclaration</a></div>
        <c:choose><c:when test="${empty detail.formalites}"><p class="muted">Aucune formalité.</p></c:when><c:otherwise><div class="table-wrap"><table><thead><tr><th>Organisme</th><th>Type</th><th>Statut</th><th>Référence</th><th></th></tr></thead><tbody>
          <c:forEach var="d" items="${detail.formalites}"><tr><td><c:out value="${d.organisme}"/></td><td><c:out value="${d.typeDeclaration}"/></td><td><span class="badge <c:out value='${d.statutBadge}'/>"><c:out value="${d.statutLibelle}"/></span></td><td><c:out value="${d.referenceExterne}"/></td><td class="right"><a class="btn btn-sm" href="<c:url value='/rh/formalites/${d.id}'/>">Ouvrir</a></td></tr></c:forEach>
        </tbody></table></div></c:otherwise></c:choose>
      </div>
    </div>
    <div class="stack">
      <div class="card"><div class="card-head"><h2>Raccourcis</h2></div><div class="stack"><a class="btn btn-block" href="<c:url value='/rh/documents/nouveau'><c:param name='idEmploye' value='${detail.salarie.id}'/></c:url>">Déposer un document</a><a class="btn btn-block" href="<c:url value='/rh/formalites/nouvelle'><c:param name='idEmploye' value='${detail.salarie.id}'/></c:url>">Créer une formalité</a></div></div>
    </div>
  </div>
</t:layout>
