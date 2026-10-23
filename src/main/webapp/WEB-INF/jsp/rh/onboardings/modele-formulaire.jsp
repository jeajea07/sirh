<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="${empty form.id ? 'Nouveau modèle' : 'Modifier le modèle'}" actif="modeles-onboarding">
  <div class="page-head"><div><h1><c:out value="${empty form.id ? 'Nouveau modèle' : 'Modifier le modèle'}"/></h1><p>Les tâches, rôles et dépendances définissent le parcours généré.</p></div><a class="btn" href="<c:url value='/rh/onboardings/modeles'/>">Retour</a></div>
  <f:flash/>
  <form method="post" action="<c:url value='/rh/onboardings/modeles'/><c:if test='${not empty form.id}'>/${form.id}</c:if>" class="card">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
    <div class="form-section"><h3>Cadre du modèle</h3><div class="form-grid">
      <div class="field"><label for="nom">Nom *</label><input id="nom" type="text" name="nom" value="<c:out value='${form.nom}'/>" required></div>
      <div class="field"><label for="idPoste">Poste ciblé</label><select id="idPoste" name="idPoste"><option value="">Tous les postes</option><c:forEach var="p" items="${postes}"><option value="${p.id}" ${form.idPoste == p.id ? 'selected' : ''}><c:out value="${p.designation}"/></option></c:forEach></select></div>
      <div class="field"><label for="idDepartement">Département ciblé</label><select id="idDepartement" name="idDepartement"><option value="">Tous les départements</option><c:forEach var="d" items="${departements}"><option value="${d.id}" ${form.idDepartement == d.id ? 'selected' : ''}><c:out value="${d.designation}"/></option></c:forEach></select></div>
      <div class="field span2"><label for="description">Description</label><textarea id="description" name="description"><c:out value="${form.description}"/></textarea></div>
    </div></div>
    <div class="form-section">
      <div class="card-head"><h3>Tâches</h3><button class="btn btn-sm" type="button" data-ajouter-tache>Ajouter une tâche</button></div>
      <div data-taches>
        <c:forEach var="t" items="${form.taches}" varStatus="st">
          <div data-tache="${st.index}" class="card" style="margin-bottom:12px;background:var(--bg)">
            <div class="card-head"><b data-numero>Tâche ${st.index + 1}</b><button class="btn btn-sm btn-danger" type="button" data-supprimer-tache>Supprimer</button></div>
            <input type="hidden" name="taches[${st.index}].id" value="${t.id}">
            <div class="form-grid">
              <div class="field span2"><label>Titre *</label><input type="text" name="taches[${st.index}].titre" value="<c:out value='${t.titre}'/>" required></div>
              <div class="field span2"><label>Description</label><textarea name="taches[${st.index}].description"><c:out value="${t.description}"/></textarea></div>
              <div class="field"><label>Rôle responsable</label><select name="taches[${st.index}].roleResponsable"><option value="RH" ${t.roleResponsable == 'RH' ? 'selected' : ''}>RH</option><option value="RESPONSABLE" ${t.roleResponsable == 'RESPONSABLE' ? 'selected' : ''}>Responsable de département</option><option value="FINANCE" ${t.roleResponsable == 'FINANCE' ? 'selected' : ''}>Finance</option><option value="DG" ${t.roleResponsable == 'DG' ? 'selected' : ''}>Direction générale</option><option value="INFORMATIQUE" ${t.roleResponsable == 'INFORMATIQUE' ? 'selected' : ''}>Informatique</option><option value="SERVICES_GENERAUX" ${t.roleResponsable == 'SERVICES_GENERAUX' ? 'selected' : ''}>Services généraux</option><option value="PAIE" ${t.roleResponsable == 'PAIE' ? 'selected' : ''}>Paie</option><option value="SANTE" ${t.roleResponsable == 'SANTE' ? 'selected' : ''}>Santé</option></select></div>
              <div class="field"><label>Condition d'activation</label><select name="taches[${st.index}].conditionActivation"><option value="TOUJOURS" ${t.conditionActivation == 'TOUJOURS' ? 'selected' : ''}>Toujours</option><option value="TYPE_CDI" ${t.conditionActivation == 'TYPE_CDI' ? 'selected' : ''}>Contrat CDI</option><option value="TYPE_CDD" ${t.conditionActivation == 'TYPE_CDD' ? 'selected' : ''}>Contrat CDD</option><option value="TYPE_STAGE" ${t.conditionActivation == 'TYPE_STAGE' ? 'selected' : ''}>Stage</option><option value="TYPE_INTERIM" ${t.conditionActivation == 'TYPE_INTERIM' ? 'selected' : ''}>Intérim</option><option value="TYPE_CONSULTANT" ${t.conditionActivation == 'TYPE_CONSULTANT' ? 'selected' : ''}>Consultant</option></select></div>
              <div class="field"><label>Délai indicatif (jours)</label><input type="number" min="0" name="taches[${st.index}].delaiIndicatifJours" value="${t.delaiIndicatifJours}"></div>
              <div class="field"><label>Ordre</label><input type="number" min="0" name="taches[${st.index}].ordre" value="${t.ordre}"></div>
              <div class="field"><label>Dépend de</label><select name="taches[${st.index}].dependDeIndex" data-dependance><option value="">Aucune dépendance</option><c:forEach var="autre" items="${form.taches}" varStatus="sa"><c:if test="${sa.index != st.index}"><option value="${sa.index}" ${t.dependDeIndex == sa.index ? 'selected' : ''}>${sa.index + 1}. <c:out value="${autre.titre}"/></option></c:if></c:forEach></select></div>
              <div class="field" style="justify-content:center"><label class="check"><input type="checkbox" name="taches[${st.index}].obligatoire" ${t.obligatoire ? 'checked' : ''}> Obligatoire</label><label class="check"><input type="checkbox" name="taches[${st.index}].bloquante" ${t.bloquante ? 'checked' : ''}> Bloquante</label></div>
            </div>
          </div>
        </c:forEach>
      </div>
    </div>
    <div class="form-pied"><a class="btn" href="<c:url value='/rh/onboardings/modeles'/>">Annuler</a><button class="btn btn-primary" type="submit">Enregistrer le modèle</button></div>
  </form>
</t:layout>
