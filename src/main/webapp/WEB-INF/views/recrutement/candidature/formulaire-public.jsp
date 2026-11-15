<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:include page="../vitrine/fragments/entete.jsp"><jsp:param name="titre" value="${titrePage}"/><jsp:param name="actif" value="${empty offreId ? 'spontanee' : 'offres'}"/></jsp:include>

<section class="v-hero">
  <div class="v-wrap">
    <c:choose>
      <c:when test="${not empty offreId}"><a class="v-back" href="<c:url value='/offres/${offreId}'/>">← Retour à l'offre</a></c:when>
      <c:otherwise><a class="v-back" href="<c:url value='/offres'/>">← Toutes les offres</a></c:otherwise>
    </c:choose>
    <div class="v-kicker" style="margin-top:14px"><c:out value="${empty offreId ? 'Candidature spontanée' : 'Candidature'}"/></div>
    <h1><c:choose><c:when test="${not empty titreOffre}"><c:out value="${titreOffre}"/></c:when><c:otherwise><c:out value="${titrePage}"/></c:otherwise></c:choose></h1>
    <p>Les champs marqués d'un * sont obligatoires. Votre dossier sera étudié par notre équipe RH.</p>
  </div>
</section>

<main class="v-main">
  <div class="v-wrap">
    <c:if test="${not empty erreur}"><div class="alert alert-bad" style="margin-bottom:16px"><c:out value="${erreur}"/></div></c:if>

    <form:form cssClass="v-form" modelAttribute="candidatureForm" method="post" enctype="multipart/form-data">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
      <c:if test="${not empty offreId}"><input type="hidden" name="offreId" value="<c:out value='${offreId}'/>"></c:if>

      <section class="card">
        <h2>1. Vos coordonnées</h2>
        <div class="form-grid">
          <div class="field"><label for="nom">Nom *</label><form:input path="nom" id="nom" maxlength="100" required="true"/><form:errors path="nom" cssClass="err"/></div>
          <div class="field"><label for="prenom">Prénom *</label><form:input path="prenom" id="prenom" maxlength="100" required="true"/><form:errors path="prenom" cssClass="err"/></div>
          <div class="field"><label for="email">E-mail *</label><form:input path="email" id="email" type="email" maxlength="150" required="true"/><form:errors path="email" cssClass="err"/></div>
          <div class="field"><label for="telephone">Téléphone</label><form:input path="telephone" id="telephone" maxlength="30"/><form:errors path="telephone" cssClass="err"/></div>
          <div class="field span2"><label for="adresse">Adresse</label><form:input path="adresse" id="adresse" maxlength="255"/></div>
        </div>
      </section>

      <section class="card">
        <h2>2. Votre profil</h2>
        <div class="form-grid">
          <div class="field"><label for="niveauEtude">Niveau d'étude</label><form:select path="niveauEtude" id="niveauEtude"><form:option value="" label="Non renseigné"/><form:options items="${niveaux}"/></form:select></div>
          <div class="field"><label for="anneesExperience">Années d'expérience</label><form:input path="anneesExperience" id="anneesExperience" type="number" min="0"/><form:errors path="anneesExperience" cssClass="err"/></div>
          <div class="field"><label for="disponibilite">Disponible à partir du</label><form:input path="disponibilite" id="disponibilite" type="date"/></div>
          <div class="field"><label for="langues">Langues</label><form:input path="langues" id="langues" maxlength="300" placeholder="Ex. Malagasy, Français, Anglais"/></div>
          <div class="field span2"><label for="formations">Formations</label><form:textarea path="formations" id="formations" rows="3"/></div>
          <div class="field span2"><label for="experiences">Expériences professionnelles</label><form:textarea path="experiences" id="experiences" rows="4"/></div>
          <div class="field span2">
            <label for="competence-1">Compétences</label>
            <div id="competences" class="v-competences"><input id="competence-1" type="text" name="competences" maxlength="100" placeholder="Ex. Java"></div>
            <div><button class="btn btn-sm" type="button" onclick="ajouterCompetence()">+ Ajouter une compétence</button></div>
          </div>
        </div>
      </section>

      <section class="card">
        <h2>3. Votre CV *</h2>
        <div class="field">
          <label for="fichierCv">Fichier PDF ou DOCX (5 Mo maximum)</label>
          <form:input path="fichierCv" id="fichierCv" type="file" accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document" required="true"/>
          <form:errors path="fichierCv" cssClass="err"/>
        </div>
      </section>

      <div class="v-form-actions">
        <a class="btn" href="<c:url value='/offres'/>">Annuler</a>
        <button class="btn btn-primary" type="submit">Envoyer ma candidature</button>
      </div>
    </form:form>
  </div>
</main>
<script>
function ajouterCompetence() {
  var input = document.createElement('input');
  input.type = 'text'; input.name = 'competences'; input.maxLength = 100; input.placeholder = 'Autre compétence';
  document.getElementById('competences').appendChild(input);
  input.focus();
}
</script>

<jsp:include page="../vitrine/fragments/pied.jsp"/>
