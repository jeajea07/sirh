<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="d" value="${verification.demande}"/>
<c:set var="ctl" value="${verification.controleBudget}"/>
<t:layout titre="Vérification Finance" actif="demandes-fin">

  <div class="page-head">
    <div>
      <h1>Vérification — demande n° <c:out value="${d.numero}"/> <span class="badge ${d.statutBadge}" style="vertical-align:middle"><c:out value="${d.statutLibelle}"/></span></h1>
      <p><c:out value="${d.poste.designation}"/> — <c:out value="${d.departement.designation}"/>, demandée par
        <strong><c:out value="${verification.demandeur}"/></strong>, reçue le <c:out value="${verification.dateReceptionFr}"/></p>
    </div>
    <a class="btn" href="<c:url value='/finance/demandes'/>">Retour à la liste</a>
  </div>

  <c:if test="${not empty succes}"><div class="alert alert-ok" style="margin-bottom:16px"><c:out value="${succes}"/></div></c:if>
  <c:if test="${not empty erreurs}">
    <div class="alert alert-bad" style="margin-bottom:16px">
      <b>Action impossible :</b>
      <ul><c:forEach var="e" items="${erreurs}"><li><c:out value="${e}"/></li></c:forEach></ul>
    </div>
  </c:if>

  <div class="card" style="margin-bottom:16px">
    <h2 style="margin-bottom:12px">Contrôle du budget — <c:out value="${ctl.annee}"/></h2>
    <div class="alert ${ctl.alerte}">
      <b><c:out value="${ctl.libelle}"/></b><br>
      <c:out value="${ctl.message}"/>
    </div>
    <c:if test="${ctl.avecBudget}">
      <dl class="kv" style="margin-top:12px">
        <dt>Budget alloué</dt><dd><c:out value="${ctl.alloueFr}"/></dd>
        <dt>Déjà engagé</dt><dd><c:out value="${ctl.engageFr}"/></dd>
        <dt>Disponible</dt><dd><c:out value="${ctl.disponibleFr}"/></dd>
        <c:if test="${ctl.statut != 'ALLEGE'}">
          <dt>Coût annuel de cette demande</dt><dd><c:out value="${ctl.coutFr}"/> <span class="muted small">(salaire × 12 × <c:out value="${d.nombrePostes}"/> poste(s))</span></dd>
        </c:if>
        <c:if test="${ctl.statut == 'DEPASSE'}"><dt>Montant manquant</dt><dd><b><c:out value="${ctl.manqueFr}"/></b></dd></c:if>
      </dl>
    </c:if>
    <p class="muted small" style="margin-top:12px">Le budget engagé comprend les demandes du département déjà validées par la Finance. Les remplacements ne sont pas comptés.</p>
  </div>

  <form method="post" action="<c:url value='/finance/demandes/${d.id}'/>" class="stack" novalidate>
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

    <div class="grid g-32" style="grid-template-columns:2fr 3fr">
      <div class="card">
        <h2 style="margin-bottom:12px">Détail de la demande</h2>
        <dl class="kv">
          <dt>Département</dt><dd><c:out value="${d.departement.designation}"/></dd>
          <dt>Poste</dt><dd><c:out value="${d.poste.designation}"/></dd>
          <dt>Nombre de postes</dt><dd><c:out value="${d.nombrePostes}"/></dd>
          <dt>Motif</dt><dd><c:out value="${d.motifLibelle}"/></dd>
          <dt>Urgence</dt><dd><c:out value="${d.urgenceLibelle}"/></dd>
          <dt>Contrat</dt><dd><c:out value="${d.typeContrat.code}"/></dd>
          <dt>Date souhaitée</dt><dd><c:out value="${d.dateSouhaiteeFr}"/></dd>
        </dl>
        <c:if test="${not empty d.justification}">
          <h3 style="margin:16px 0 6px">Justification</h3>
          <p class="demande-texte"><c:out value="${d.justification}"/></p>
        </c:if>
      </div>

      <div class="card">
        <h2 style="margin-bottom:4px">Corrections possibles</h2>
        <p class="muted small" style="margin-bottom:12px">Vous pouvez corriger le salaire ou la durée avant de valider. Chaque correction est gardée dans l'historique, et le RH est prévenu d'un changement de salaire.</p>
        <div class="form-grid">
          <div class="field">
            <label for="salairePropose">Salaire mensuel proposé (Ar) *</label>
            <input type="number" id="salairePropose" name="salairePropose" min="1" step="any" value="${form.salairePropose}">
          </div>
          <c:if test="${verification.dureeRequise}">
            <div class="field">
              <label for="dureeMois">Durée du contrat (en mois) *</label>
              <input type="number" id="dureeMois" name="dureeMois" min="1" value="${form.dureeMois}">
            </div>
          </c:if>
        </div>
      </div>
    </div>

    <div class="card">
      <h2 style="margin-bottom:12px">Décision</h2>
      <div class="field">
        <label for="commentaire">Commentaire</label>
        <textarea id="commentaire" name="commentaire"><c:out value="${form.commentaire}"/></textarea>
        <span class="hint">Obligatoire pour renvoyer la demande ou la rejeter.</span>
      </div>
    </div>

    <div class="form-pied">
      <a class="btn" href="<c:url value='/finance/demandes'/>">Annuler</a>
      <button class="btn btn-danger" type="submit" name="action" value="rejeter" data-commentaire="requis">Rejeter</button>
      <button class="btn" type="submit" name="action" value="renvoyer" data-commentaire="requis">Renvoyer pour correction</button>
      <button class="btn" type="submit" name="action" value="corriger">Enregistrer les corrections</button>
      <button class="btn btn-ok" type="submit" name="action" value="valider">Valider et envoyer à la DG</button>
    </div>
  </form>

  <script src="<c:url value='/js/rh.js'/>?v=2"></script>
</t:layout>
