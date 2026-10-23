<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="${empty form.id ? 'Nouveau contrat' : 'Modifier le contrat'}" actif="contrats">
  <div class="page-head">
    <div><h1><c:out value="${empty form.id ? 'Nouveau contrat' : 'Modifier le contrat'}"/></h1><p>Les champs marqués d'un astérisque sont obligatoires.</p></div>
    <a class="btn" href="<c:url value='/rh/contrats'/>">Retour</a>
  </div>
  <f:flash/>

  <form method="post" action="<c:url value='/rh/contrats'/>" class="card">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
    <c:if test="${not empty form.id}"><input type="hidden" name="id" value="${form.id}"></c:if>
    <input type="hidden" name="typeOperation" value="<c:out value='${form.typeOperation}'/>">
    <div class="form-grid">
      <div class="field span2"><label for="idEmploye">Salarié *</label>
        <select id="idEmploye" name="idEmploye" required>
          <option value="">Choisir un salarié…</option>
          <c:forEach var="e" items="${employes}"><option value="${e.id}" ${form.idEmploye == e.id ? 'selected' : ''}><c:out value="${e.nomComplet}"/> — <c:out value="${e.matricule}"/></option></c:forEach>
        </select>
      </div>
      <div class="field"><label for="idTypeContrat">Type de contrat *</label>
        <select id="idTypeContrat" name="idTypeContrat" required>
          <option value="">Choisir…</option>
          <c:forEach var="tc" items="${typesContrat}"><option value="${tc.id}" ${form.idTypeContrat == tc.id ? 'selected' : ''}><c:out value="${tc.libelle}"/></option></c:forEach>
        </select>
      </div>
      <div class="field"><label>Opération</label><input class="readonly" type="text" value="<c:out value='${form.typeOperation}'/>" readonly></div>
      <div class="field"><label for="dateDebut">Date de début *</label><input id="dateDebut" type="date" name="dateDebut" value="${form.dateDebut}" required></div>
      <div class="field"><label for="dateFin">Date de fin</label><input id="dateFin" type="date" name="dateFin" value="${form.dateFin}"><span class="hint">Vide pour un CDI.</span></div>
      <div class="field"><label for="dateSignature">Date de signature</label><input id="dateSignature" type="date" name="dateSignature" value="${form.dateSignature}"></div>
      <div class="field"><label for="fonction">Fonction *</label><input id="fonction" type="text" name="fonction" value="<c:out value='${form.fonction}'/>" required></div>
      <div class="field"><label for="categorieProfessionnelle">Catégorie professionnelle</label><input id="categorieProfessionnelle" type="text" name="categorieProfessionnelle" value="<c:out value='${form.categorieProfessionnelle}'/>"></div>
      <div class="field"><label for="salaire">Salaire brut mensuel (Ar) *</label><input id="salaire" type="number" min="1" step="0.01" name="salaire" value="${form.salaire}" required></div>
      <div class="field"><label for="motifRecours">Motif de recours</label><input id="motifRecours" type="text" name="motifRecours" value="<c:out value='${form.motifRecours}'/>"></div>
      <div class="field span2"><label for="commentaire">Commentaire</label><textarea id="commentaire" name="commentaire"><c:out value="${form.commentaire}"/></textarea></div>
    </div>
    <div class="form-pied" style="margin-top:18px">
      <a class="btn" href="<c:url value='/rh/contrats'/>">Annuler</a>
      <button class="btn" type="submit" name="action" value="brouillon">Enregistrer en brouillon</button>
      <button class="btn btn-primary" type="submit" name="action" value="soumettre">Enregistrer et soumettre</button>
    </div>
  </form>
</t:layout>
