<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Nouvel avenant" actif="contrats">
  <div class="page-head"><div><h1>Nouvel avenant</h1><p>Contrat d'origine : <b><c:out value="${origine.reference}"/></b> — <c:out value="${origine.employeNom}"/></p></div><a class="btn" href="<c:url value='/rh/contrats/${origine.id}'/>">Retour</a></div>
  <f:flash/>
  <form method="post" action="<c:url value='/rh/contrats/${origine.id}/avenant'/>" class="card">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
    <input type="hidden" name="typeOperation" value="AVENANT">
    <div class="form-grid">
      <div class="field"><label for="idTypeContrat">Type de contrat *</label><select id="idTypeContrat" name="idTypeContrat" required><c:forEach var="tc" items="${typesContrat}"><option value="${tc.id}" ${form.idTypeContrat == tc.id ? 'selected' : ''}><c:out value="${tc.libelle}"/></option></c:forEach></select></div>
      <div class="field"><label for="dateDebut">Date d'effet *</label><input id="dateDebut" type="date" name="dateDebut" value="${form.dateDebut}" required></div>
      <div class="field"><label for="dateFin">Date de fin</label><input id="dateFin" type="date" name="dateFin" value="${form.dateFin}"></div>
      <div class="field"><label for="dateSignature">Date de signature</label><input id="dateSignature" type="date" name="dateSignature" value="${form.dateSignature}"></div>
      <div class="field"><label for="fonction">Fonction *</label><input id="fonction" type="text" name="fonction" value="<c:out value='${form.fonction}'/>" required></div>
      <div class="field"><label for="salaire">Salaire brut mensuel (Ar) *</label><input id="salaire" type="number" min="1" step="0.01" name="salaire" value="${form.salaire}" required></div>
      <div class="field"><label for="categorieProfessionnelle">Catégorie</label><input id="categorieProfessionnelle" type="text" name="categorieProfessionnelle" value="<c:out value='${form.categorieProfessionnelle}'/>"></div>
      <div class="field"><label for="motifRecours">Motif</label><input id="motifRecours" type="text" name="motifRecours" value="<c:out value='${form.motifRecours}'/>"></div>
      <div class="field span2"><label for="commentaire">Objet de l'avenant</label><textarea id="commentaire" name="commentaire" required><c:out value="${form.commentaire}"/></textarea></div>
    </div>
    <div class="form-pied"><a class="btn" href="<c:url value='/rh/contrats/${origine.id}'/>">Annuler</a><button class="btn btn-primary" type="submit">Créer l'avenant</button></div>
  </form>
</t:layout>
