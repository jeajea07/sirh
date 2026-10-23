<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout titre="${titrePage}" actif="candidature-public">
  <div class="page-head">
    <div><h1><c:out value="${titrePage}"/></h1><p>Présentez votre profil à notre équipe de recrutement.</p></div>
  </div>

  <c:if test="${not empty titreOffre}"><div class="alert alert-info" style="margin-bottom:16px">Votre candidature concerne l'offre : <b><c:out value="${titreOffre}"/></b></div></c:if>
  <c:if test="${not empty erreur}"><div class="alert alert-bad" style="margin-bottom:16px"><c:out value="${erreur}"/></div></c:if>

  <form:form modelAttribute="candidatureForm" method="post" enctype="multipart/form-data" cssClass="stack" style="max-width:940px">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
    <c:if test="${not empty offreId}"><input type="hidden" name="offreId" value="<c:out value='${offreId}'/>"/></c:if>

    <section class="card stack">
      <div><h2>Vos coordonnées</h2><p class="muted">Aucun compte n'est nécessaire pour envoyer une candidature.</p></div>
      <div class="grid g2">
        <div class="field"><label for="nom">Nom</label><form:input path="nom" id="nom" maxlength="100" required="true"/><form:errors path="nom" cssClass="err"/></div>
        <div class="field"><label for="prenom">Prénom</label><form:input path="prenom" id="prenom" maxlength="100" required="true"/><form:errors path="prenom" cssClass="err"/></div>
        <div class="field"><label for="email">E-mail</label><form:input path="email" id="email" type="email" maxlength="150" required="true"/><form:errors path="email" cssClass="err"/></div>
        <div class="field"><label for="telephone">Téléphone</label><form:input path="telephone" id="telephone" maxlength="30"/></div>
      </div>
      <div class="field"><label for="adresse">Adresse</label><form:input path="adresse" id="adresse" maxlength="255"/></div>
    </section>

    <section class="card stack">
      <div><h2>Votre profil et votre CV</h2><p class="muted">Le CV doit être au format PDF ou DOCX et ne pas dépasser 5 Mo.</p></div>
      <div class="grid g2">
        <div class="field"><label for="niveauEtude">Niveau d'étude</label><form:select path="niveauEtude" id="niveauEtude"><form:option value="" label="Non renseigné"/><form:options items="${niveaux}"/></form:select></div>
        <div class="field"><label for="anneesExperience">Années d'expérience</label><form:input path="anneesExperience" id="anneesExperience" type="number" min="0"/></div>
        <div class="field"><label for="disponibilite">Disponibilité à partir du</label><form:input path="disponibilite" id="disponibilite" type="date"/></div>
        <div class="field"><label for="langues">Langues</label><form:input path="langues" id="langues" maxlength="300"/></div>
      </div>
      <div class="field"><label for="formations">Formations</label><form:textarea path="formations" id="formations" rows="4"/></div>
      <div class="field"><label for="experiences">Expériences</label><form:textarea path="experiences" id="experiences" rows="4"/></div>
      <div class="field"><label>Compétences</label><div id="competences" class="stack"><input type="text" name="competences" maxlength="100" aria-label="Compétence"/></div><button class="btn btn-sm" type="button" onclick="ajouterCompetence()">Ajouter une compétence</button></div>
      <div class="field"><label for="fichierCv">CV (PDF ou DOCX, 5 Mo maximum)</label><form:input path="fichierCv" id="fichierCv" type="file" accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document" required="true"/><form:errors path="fichierCv" cssClass="err"/></div>
    </section>
    <div class="row wrap"><button class="btn btn-primary" type="submit">Envoyer ma candidature</button><a class="btn" href="<c:url value='/offres'/>">Annuler</a></div>
  </form:form>
  <script>
  function ajouterCompetence() {
    const input = document.createElement('input');
    input.type = 'text'; input.name = 'competences'; input.maxLength = 100; input.setAttribute('aria-label', 'Compétence');
    document.getElementById('competences').appendChild(input);
  }
  </script>
</t:layout>
