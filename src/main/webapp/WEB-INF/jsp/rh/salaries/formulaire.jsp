<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Modifier le salarié" actif="salaries">
  <div class="page-head"><div><h1>Modifier le dossier</h1><p><c:out value="${salarie.nomComplet}"/> — <c:out value="${salarie.matricule}"/></p></div><a class="btn" href="<c:url value='/rh/salaries/${form.id}'/>">Retour</a></div>
  <f:flash/>
  <form method="post" action="<c:url value='/rh/salaries/${form.id}'/>" class="card">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
    <div class="form-section"><h3>Identité</h3><div class="form-grid">
      <div class="field"><label for="nom">Nom *</label><input id="nom" type="text" name="nom" value="<c:out value='${form.nom}'/>" required></div>
      <div class="field"><label for="prenom">Prénom *</label><input id="prenom" type="text" name="prenom" value="<c:out value='${form.prenom}'/>" required></div>
      <div class="field"><label for="email">Email *</label><input id="email" type="email" name="email" value="<c:out value='${form.email}'/>" required></div>
      <div class="field"><label for="telephone">Téléphone</label><input id="telephone" type="tel" name="telephone" value="<c:out value='${form.telephone}'/>"></div>
      <div class="field"><label for="cin">CIN</label><input id="cin" type="text" name="cin" value="<c:out value='${form.cin}'/>"></div>
      <div class="field"><label for="dateNaissance">Date de naissance</label><input id="dateNaissance" type="date" name="dateNaissance" value="${form.dateNaissance}"></div>
      <div class="field"><label for="lieuNaissance">Lieu de naissance</label><input id="lieuNaissance" type="text" name="lieuNaissance" value="<c:out value='${form.lieuNaissance}'/>"></div>
      <div class="field"><label for="situationFamiliale">Situation familiale</label><select id="situationFamiliale" name="situationFamiliale"><option value="">Non renseignée</option><option value="CELIBATAIRE" ${form.situationFamiliale == 'CELIBATAIRE' ? 'selected' : ''}>Célibataire</option><option value="MARIE" ${form.situationFamiliale == 'MARIE' ? 'selected' : ''}>Marié(e)</option><option value="DIVORCE" ${form.situationFamiliale == 'DIVORCE' ? 'selected' : ''}>Divorcé(e)</option><option value="VEUF" ${form.situationFamiliale == 'VEUF' ? 'selected' : ''}>Veuf(ve)</option></select></div>
      <div class="field"><label for="nombreEnfants">Nombre d'enfants</label><input id="nombreEnfants" type="number" min="0" name="nombreEnfants" value="${form.nombreEnfants}"></div>
      <div class="field"><label for="dateEmbauche">Date d'embauche</label><input id="dateEmbauche" type="date" name="dateEmbauche" value="${form.dateEmbauche}"></div>
      <div class="field span2"><label for="adresse">Adresse</label><textarea id="adresse" name="adresse"><c:out value="${form.adresse}"/></textarea></div>
    </div></div>
    <div class="form-section"><h3>Protection sociale et bancaire</h3><div class="form-grid">
      <div class="field"><label for="numeroCnaps">N° CNaPS</label><input id="numeroCnaps" type="text" name="numeroCnaps" value="<c:out value='${form.numeroCnaps}'/>"></div>
      <div class="field"><label for="numeroOstie">N° OSTIE</label><input id="numeroOstie" type="text" name="numeroOstie" value="<c:out value='${form.numeroOstie}'/>"></div>
      <div class="field span2"><label for="rib">RIB / compte bancaire</label><input id="rib" type="text" name="rib" value="<c:out value='${form.rib}'/>"></div>
    </div></div>
    <div class="form-pied"><a class="btn" href="<c:url value='/rh/salaries/${form.id}'/>">Annuler</a><button class="btn btn-primary" type="submit">Enregistrer</button></div>
  </form>
</t:layout>
