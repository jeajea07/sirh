<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="${empty form.id ? 'Nouvelle formalité' : 'Modifier la formalité'}" actif="formalites">
  <div class="page-head"><div><h1><c:out value="${empty form.id ? 'Nouvelle formalité' : 'Modifier la formalité'}"/></h1><p>Déclaration d'embauche, affiliation ou justificatif administratif.</p></div><a class="btn" href="<c:url value='/rh/formalites'/>">Retour</a></div>
  <f:flash/>
  <form method="post" action="<c:url value='/rh/formalites'/><c:if test='${not empty form.id}'>/${form.id}</c:if>" class="card">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
    <div class="form-grid">
      <div class="field span2"><label for="idEmploye">Salarié *</label><select id="idEmploye" name="idEmploye" required><option value="">Choisir…</option><c:forEach var="e" items="${employes}"><option value="${e.id}" ${form.idEmploye == e.id ? 'selected' : ''}><c:out value="${e.nomComplet}"/> — <c:out value="${e.matricule}"/></option></c:forEach></select></div>
      <div class="field"><label for="idOrganisme">Organisme *</label><select id="idOrganisme" name="idOrganisme" required><option value="">Choisir…</option><c:forEach var="o" items="${organismes}"><option value="${o.id}" ${form.idOrganisme == o.id ? 'selected' : ''}><c:out value="${o.nom}"/></option></c:forEach></select></div>
      <div class="field"><label for="typeDeclaration">Type de formalité *</label>
        <select id="typeDeclaration" name="typeDeclaration" required>
          <option value="">Choisir…</option>
          <option value="DECLARATION_EMBAUCHE" ${form.typeDeclaration == 'DECLARATION_EMBAUCHE' ? 'selected' : ''}>Déclaration d'embauche</option>
          <option value="AFFILIATION" ${form.typeDeclaration == 'AFFILIATION' ? 'selected' : ''}>Affiliation</option>
          <option value="COTISATION" ${form.typeDeclaration == 'COTISATION' ? 'selected' : ''}>Cotisation</option>
          <option value="VISITE_MEDICALE" ${form.typeDeclaration == 'VISITE_MEDICALE' ? 'selected' : ''}>Visite médicale</option>
          <option value="ATTESTATION" ${form.typeDeclaration == 'ATTESTATION' ? 'selected' : ''}>Attestation</option>
          <option value="AUTRE" ${form.typeDeclaration == 'AUTRE' ? 'selected' : ''}>Autre</option>
        </select>
      </div>
      <div class="field"><label for="datePreparation">Date de préparation</label><input id="datePreparation" type="date" name="datePreparation" value="${form.datePreparation}"></div>
      <div class="field"><label for="dateEnvoi">Date d'envoi</label><input id="dateEnvoi" type="date" name="dateEnvoi" value="${form.dateEnvoi}"></div>
      <div class="field"><label for="referenceExterne">Référence externe</label><input id="referenceExterne" type="text" name="referenceExterne" value="<c:out value='${form.referenceExterne}'/>"></div>
      <div class="field span2"><label for="idDocumentJustificatif">Justificatif</label><select id="idDocumentJustificatif" name="idDocumentJustificatif"><option value="">Aucun</option><c:forEach var="d" items="${documents}"><c:if test="${d.typeDocument == 'SOCIAL' or d.typeDocument == 'SANTE'}"><option value="${d.id}" ${form.idDocumentJustificatif == d.id ? 'selected' : ''}><c:out value="${d.titre}"/> — <c:out value="${d.typeDocument}"/></option></c:if></c:forEach></select></div>
      <div class="field span2"><label for="commentaire">Commentaire</label><textarea id="commentaire" name="commentaire"><c:out value="${form.commentaire}"/></textarea></div>
    </div>
    <div class="form-pied"><a class="btn" href="<c:url value='/rh/formalites'/>">Annuler</a><button class="btn btn-primary" type="submit">Enregistrer</button></div>
  </form>
</t:layout>
