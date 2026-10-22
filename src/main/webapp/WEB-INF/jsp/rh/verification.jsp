<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="d" value="${verification.demande}"/>
<c:set var="ctl" value="${verification.controleSalaire}"/>
<t:layout titre="Vérification RH" actif="demandes-rh">

  <div class="page-head">
    <div>
      <h1>Vérification — demande n° <c:out value="${d.numero}"/> <span class="badge ${d.statutBadge}" style="vertical-align:middle"><c:out value="${d.statutLibelle}"/></span></h1>
      <p><c:out value="${d.poste.designation}"/> — <c:out value="${d.departement.designation}"/>, demandée par
        <strong><c:out value="${verification.demandeur}"/></strong>, reçue le <c:out value="${verification.dateReceptionFr}"/></p>
    </div>
    <a class="btn" href="<c:url value='/rh/demandes'/>">Retour à la liste</a>
  </div>

  <c:if test="${not empty succes}"><div class="alert alert-ok" style="margin-bottom:16px"><c:out value="${succes}"/></div></c:if>
  <c:if test="${not empty erreurs}">
    <div class="alert alert-bad" style="margin-bottom:16px">
      <b>Action impossible :</b>
      <ul><c:forEach var="e" items="${erreurs}"><li><c:out value="${e}"/></li></c:forEach></ul>
    </div>
  </c:if>

  <div class="card" style="margin-bottom:16px">
    <h2 style="margin-bottom:12px">Contrôles automatiques</h2>
    <div class="stack">
      <div class="alert ${ctl.statut == 'DEPASSE' ? 'alert-bad' : (ctl.statut == 'CONFORME' ? 'alert-ok' : 'alert-warn')}">
        <b>Salaire et grille salariale — <c:out value="${ctl.libelle}"/></b><br>
        <c:out value="${ctl.message}"/>
      </div>
      <c:forEach var="a" items="${verification.alertes}">
        <div class="alert alert-warn"><c:out value="${a}"/></div>
      </c:forEach>
      <c:if test="${empty verification.alertes}">
        <div class="alert alert-ok">Critères complets, aucune demande en double, effectif du département respecté.</div>
      </c:if>
    </div>
  </div>

  <form method="post" action="<c:url value='/rh/demandes/${d.id}'/>" class="stack" novalidate>
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
        <p class="muted small" style="margin-bottom:12px">Vous pouvez modifier ces champs avant de valider. Chaque correction est gardée dans l'historique.</p>
        <div class="form-grid">
          <div class="field ${ctl.statut == 'DEPASSE' ? 'error' : ''}" id="champ-salaire">
            <label for="salairePropose">Salaire mensuel proposé (Ar) *</label>
            <input type="number" id="salairePropose" name="salairePropose" min="1" step="any" value="${form.salairePropose}"
                   data-plafond="${ctl.plafondBrut}" data-plafond-fr="${ctl.plafondFr}">
            <span class="hint" id="aide-salaire">
              <c:choose>
                <c:when test="${not empty ctl.plafondBrut}">Maximum selon la grille : <b><c:out value="${ctl.plafondFr}"/></b></c:when>
                <c:otherwise>Aucune grille trouvée pour ce poste.</c:otherwise>
              </c:choose>
            </span>
            <span class="hint hidden" id="erreur-salaire" style="color:var(--bad);font-weight:600"></span>
          </div>

          <c:if test="${verification.dureeRequise}">
            <div class="field">
              <label for="dureeMois">Durée du contrat (en mois) *</label>
              <input type="number" id="dureeMois" name="dureeMois" min="1" value="${form.dureeMois}">
            </div>
          </c:if>

          <div class="field">
            <label for="niveauEtude">Niveau d'étude *</label>
            <select id="niveauEtude" name="niveauEtude">
              <option value="">— Choisir —</option>
              <option value="LICENCE" ${form.niveauEtude == 'LICENCE' ? 'selected' : ''}>Licence</option>
              <option value="MASTER" ${form.niveauEtude == 'MASTER' ? 'selected' : ''}>Master</option>
              <option value="PHD" ${form.niveauEtude == 'PHD' ? 'selected' : ''}>Doctorat (PhD)</option>
            </select>
            <span class="hint">Le niveau d'étude peut changer la ligne de grille appliquée.</span>
          </div>
          <div class="field">
            <label for="anneeExperience">Années d'expérience *</label>
            <input type="number" id="anneeExperience" name="anneeExperience" min="0" value="${form.anneeExperience}">
          </div>
          <div class="field span2">
            <label for="competence">Compétences *</label>
            <textarea id="competence" name="competence" maxlength="500"><c:out value="${form.competence}"/></textarea>
          </div>
          <div class="field span2">
            <label for="missions">Missions principales</label>
            <textarea id="missions" name="missions" maxlength="500"><c:out value="${form.missions}"/></textarea>
          </div>
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
      <a class="btn" href="<c:url value='/rh/demandes'/>">Annuler</a>
      <button class="btn btn-danger" type="submit" name="action" value="rejeter" data-commentaire="requis">Rejeter</button>
      <button class="btn" type="submit" name="action" value="renvoyer" data-commentaire="requis">Renvoyer pour correction</button>
      <button class="btn" type="submit" name="action" value="corriger">Enregistrer les corrections</button>
      <button class="btn btn-ok" type="submit" name="action" value="valider">Valider et envoyer à la Finance</button>
    </div>
  </form>

  <script src="<c:url value='/js/rh.js'/>?v=1"></script>
</t:layout>
