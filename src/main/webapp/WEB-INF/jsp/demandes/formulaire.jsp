<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="enModification" value="${not empty form.id}"/>
<t:layout titre="${enModification ? 'Modifier la demande' : 'Nouvelle demande'}" actif="${enModification ? 'demandes' : 'nouvelle'}">

  <div class="page-head">
    <div>
      <h1>Demande de besoin en personnel</h1>
      <p>Demandeur : <strong><c:out value="${demandeur.nomComplet}"/></strong> —
        <c:out value="${demandeur.departement.designation}"/><c:if test="${not empty demandeur.poste}">, <c:out value="${demandeur.poste.designation}"/></c:if></p>
    </div>
  </div>

  <c:if test="${not empty erreurs}">
    <div class="alert alert-bad" style="margin-bottom:16px">
      <b>Veuillez corriger ou compléter :</b>
      <ul><c:forEach var="e" items="${erreurs}"><li><c:out value="${e}"/></li></c:forEach></ul>
    </div>
  </c:if>

  <form method="post" action="<c:url value='/demandes'/>" class="stack" novalidate>
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
    <input type="hidden" name="id" value="${form.id}">
    <input type="hidden" name="departementId" value="${form.departementId}">

    <div class="card">
      <h3 style="margin-bottom:12px">Poste à pourvoir</h3>
      <div class="form-grid">
        <div class="field">
          <label for="idPoste">Poste *</label>
          <select id="idPoste" name="idPoste">
            <option value="">— Choisir un poste —</option>
            <c:forEach var="p" items="${postes}">
              <option value="${p.id}" ${form.idPoste == p.id ? 'selected' : ''}><c:out value="${p.designation}"/></option>
            </c:forEach>
          </select>
          <span class="hint">Le poste doit exister dans l'organigramme.</span>
        </div>
        <div class="field">
          <label for="nombrePostes">Nombre de postes *</label>
          <input type="number" id="nombrePostes" name="nombrePostes" min="1" value="${empty form.nombrePostes ? 1 : form.nombrePostes}">
        </div>

        <div class="field">
          <label for="motif">Motif *</label>
          <select id="motif" name="motif">
            <option value="">— Choisir —</option>
            <option value="REMPLACEMENT" ${form.motif == 'REMPLACEMENT' ? 'selected' : ''}>Remplacement</option>
            <option value="RENFORT" ${form.motif == 'RENFORT' ? 'selected' : ''}>Poste existant (renfort)</option>
            <option value="CREATION" ${form.motif == 'CREATION' ? 'selected' : ''}>Création de poste</option>
          </select>
        </div>

        <div class="field">
          <label for="niveauUrgence">Niveau d'urgence *</label>
          <select id="niveauUrgence" name="niveauUrgence">
            <option value="URGENT" ${form.niveauUrgence != 'CRITIQUE' ? 'selected' : ''}>Urgent</option>
            <option value="CRITIQUE" ${form.niveauUrgence == 'CRITIQUE' ? 'selected' : ''}>Critique</option>
          </select>
        </div>

        <div class="field">
          <label for="dateSouhaitee">Date souhaitée de prise de poste *</label>
          <input type="date" id="dateSouhaitee" name="dateSouhaitee" min="${aujourdhui}" value="${form.dateSouhaitee}">
        </div>
      </div>
    </div>

    <div class="card">
      <h3 style="margin-bottom:12px">Contrat et salaire</h3>
      <div class="form-grid">
        <div class="field">
          <label for="idTypeContrat">Type de contrat *</label>
          <select id="idTypeContrat" name="idTypeContrat">
            <option value="">— Choisir —</option>
            <c:forEach var="tc" items="${typesContrat}">
              <option value="${tc.id}" data-code="${tc.code}" data-duree="${tc.avecDuree}" ${form.idTypeContrat == tc.id ? 'selected' : ''}><c:out value="${tc.code}"/> — <c:out value="${tc.libelle}"/></option>
            </c:forEach>
          </select>
        </div>
        <div class="field hidden" id="bloc-duree">
          <label for="dureeMois">Durée du contrat (en mois) *</label>
          <input type="number" id="dureeMois" name="dureeMois" min="1" value="${form.dureeMois}">
        </div>
        <div class="field">
          <label for="salairePropose">Salaire mensuel proposé (Ar) *</label>
          <input type="number" id="salairePropose" name="salairePropose" min="1" step="any" value="${form.salairePropose}">
          <span class="hint">Il sera comparé à la grille salariale par le RH.</span>
        </div>
      </div>
    </div>

    <div class="card">
      <h3 style="margin-bottom:12px">Critères de recrutement</h3>
      <div class="form-grid">
        <div class="field">
          <label for="niveauEtude">Niveau d'étude *</label>
          <select id="niveauEtude" name="niveauEtude">
            <option value="">— Choisir —</option>
            <option value="LICENCE" ${form.niveauEtude == 'LICENCE' ? 'selected' : ''}>Licence</option>
            <option value="MASTER" ${form.niveauEtude == 'MASTER' ? 'selected' : ''}>Master</option>
            <option value="PHD" ${form.niveauEtude == 'PHD' ? 'selected' : ''}>Doctorat (PhD)</option>
          </select>
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

    <div class="card">
      <h3 style="margin-bottom:12px">Justification</h3>
      <div class="field">
        <label for="justification">Pourquoi ce besoin ? *</label>
        <textarea id="justification" name="justification"><c:out value="${form.justification}"/></textarea>
        <span class="hint">Pour une création de poste, détaillez davantage (50 caractères minimum).</span>
      </div>
    </div>

    <div class="form-pied">
      <a class="btn" href="<c:url value='/demandes'/>">Annuler</a>
      <button class="btn" type="submit" name="action" value="brouillon">Enregistrer le brouillon</button>
      <button class="btn btn-primary" type="submit" name="action" value="soumettre">Soumettre la demande</button>
    </div>
  </form>

  <script src="<c:url value='/js/demande.js'/>?v=2"></script>
</t:layout>
