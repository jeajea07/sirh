<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html><html lang="fr"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title><c:out value="${titrePage}"/> - SIRH</title>
<style>body{font-family:system-ui,sans-serif;max-width:900px;margin:2rem auto;padding:0 1rem;color:#243447}label{display:block;margin:.8rem 0 .25rem;font-weight:600}input,select,textarea{box-sizing:border-box;width:100%;padding:.55rem;border:1px solid #bdc6d2;border-radius:4px}.grille{display:grid;grid-template-columns:1fr 1fr;gap:0 1rem}.erreur{color:#a32222}.carte{padding:1rem;border:1px solid #d8dee8;border-radius:8px;margin:1rem 0}.btn{padding:.65rem 1rem;background:#12569b;color:white;border:0;border-radius:5px}</style></head><body>
<header><a href="${pageContext.request.contextPath}/offres">Retour aux offres</a></header><main>
<h1><c:out value="${titrePage}"/></h1>
<c:if test="${not empty titreOffre}"><p>Offre : <c:out value="${titreOffre}"/></p></c:if>
<c:if test="${not empty erreur}"><p class="erreur"><c:out value="${erreur}"/></p></c:if>
<form:form modelAttribute="candidatureForm" method="post" enctype="multipart/form-data">
  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
  <c:if test="${not empty offreId}"><input type="hidden" name="offreId" value="<c:out value='${offreId}'/>"></c:if>
  <section class="carte"><h2>Vos coordonnées</h2><div class="grille">
    <div><label for="nom">Nom</label><form:input path="nom" id="nom" maxlength="100" required="true"/><form:errors path="nom" cssClass="erreur"/></div>
    <div><label for="prenom">Prénom</label><form:input path="prenom" id="prenom" maxlength="100" required="true"/><form:errors path="prenom" cssClass="erreur"/></div>
    <div><label for="email">E-mail</label><form:input path="email" id="email" type="email" maxlength="150" required="true"/><form:errors path="email" cssClass="erreur"/></div>
    <div><label for="telephone">Téléphone</label><form:input path="telephone" id="telephone" maxlength="30"/></div>
  </div><label for="adresse">Adresse</label><form:input path="adresse" id="adresse" maxlength="255"/></section>
  <section class="carte"><h2>Votre CV numérique</h2><div class="grille">
    <div><label for="niveauEtude">Niveau d'étude</label><form:select path="niveauEtude" id="niveauEtude"><form:option value="" label="Non renseigné"/><form:options items="${niveaux}"/></form:select></div>
    <div><label for="anneesExperience">Années d'expérience</label><form:input path="anneesExperience" id="anneesExperience" type="number" min="0"/></div>
    <div><label for="disponibilite">Disponibilité à partir du</label><form:input path="disponibilite" id="disponibilite" type="date"/></div>
    <div><label for="langues">Langues</label><form:input path="langues" id="langues" maxlength="300"/></div>
  </div><label for="formations">Formations</label><form:textarea path="formations" id="formations" rows="4"/>
  <label for="experiences">Expériences</label><form:textarea path="experiences" id="experiences" rows="4"/>
  <label>Compétences</label><div id="competences"><input type="text" name="competences" maxlength="100"></div>
  <button type="button" onclick="ajouterCompetence()">Ajouter une compétence</button>
  <label for="fichierCv">CV (PDF ou DOCX, 5 Mo maximum)</label><form:input path="fichierCv" id="fichierCv" type="file" accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document" required="true"/><form:errors path="fichierCv" cssClass="erreur"/>
  </section>
  <button class="btn" type="submit">Envoyer ma candidature</button>
</form:form></main>
<script>function ajouterCompetence(){const input=document.createElement('input');input.type='text';input.name='competences';input.maxLength=100;document.getElementById('competences').appendChild(input);}</script>
</body></html>
