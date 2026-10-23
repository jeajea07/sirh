<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Déposer un document" actif="documents">
  <div class="page-head"><div><h1>Déposer un document</h1><p>Formats acceptés : PDF, DOC, DOCX, PNG, JPG et JPEG, 10 Mo maximum.</p></div><a class="btn" href="<c:url value='/rh/documents'/>">Retour</a></div>
  <f:flash/>
  <form method="post" action="<c:url value='/rh/documents'/>" enctype="multipart/form-data" class="card">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
    <c:if test="${not empty form.idOnboardingTache}"><input type="hidden" name="idOnboardingTache" value="${form.idOnboardingTache}"></c:if>
    <div class="form-grid">
      <div class="field"><label for="typeDocument">Type de document *</label>
        <select id="typeDocument" name="typeDocument" required>
          <option value="">Choisir…</option>
          <option value="CONTRAT" ${form.typeDocument == 'CONTRAT' ? 'selected' : ''}>Contrat</option>
          <option value="ADMINISTRATIF" ${form.typeDocument == 'ADMINISTRATIF' ? 'selected' : ''}>Administratif</option>
          <option value="SOCIAL" ${form.typeDocument == 'SOCIAL' ? 'selected' : ''}>Social</option>
          <option value="SANTE" ${form.typeDocument == 'SANTE' ? 'selected' : ''}>Santé</option>
          <option value="INTERNE" ${form.typeDocument == 'INTERNE' ? 'selected' : ''}>Interne</option>
          <option value="FORMATION" ${form.typeDocument == 'FORMATION' ? 'selected' : ''}>Formation</option>
          <option value="MATERIEL" ${form.typeDocument == 'MATERIEL' ? 'selected' : ''}>Matériel</option>
          <option value="CV" ${form.typeDocument == 'CV' ? 'selected' : ''}>CV</option>
        </select>
      </div>
      <div class="field"><label for="niveauConfidentialite">Confidentialité</label>
        <select id="niveauConfidentialite" name="niveauConfidentialite">
          <option value="PUBLIC" ${form.niveauConfidentialite == 'PUBLIC' ? 'selected' : ''}>Public</option>
          <option value="INTERNE" ${form.niveauConfidentialite == 'INTERNE' ? 'selected' : ''}>Interne</option>
          <option value="CONFIDENTIEL" ${form.niveauConfidentialite == 'CONFIDENTIEL' ? 'selected' : ''}>Confidentiel</option>
          <option value="SECRET" ${form.niveauConfidentialite == 'SECRET' ? 'selected' : ''}>Secret</option>
        </select>
      </div>
      <div class="field span2"><label for="titre">Titre *</label><input id="titre" type="text" name="titre" value="<c:out value='${form.titre}'/>" required></div>
      <div class="field"><label for="idEmploye">Salarié *</label>
        <select id="idEmploye" name="idEmploye" required>
          <option value="">Choisir…</option>
          <c:forEach var="e" items="${employes}"><option value="${e.id}" ${form.idEmploye == e.id ? 'selected' : ''}><c:out value="${e.nomComplet}"/> — <c:out value="${e.matricule}"/></option></c:forEach>
        </select>
      </div>
      <div class="field"><label for="idContrat">Contrat rattaché</label>
        <select id="idContrat" name="idContrat">
          <option value="">Aucun</option>
          <c:forEach var="c" items="${contrats}"><option value="${c.id}" ${form.idContrat == c.id ? 'selected' : ''}><c:out value="${c.reference}"/></option></c:forEach>
        </select>
      </div>
      <div class="field span2"><label for="fichier">Fichier *</label><input id="fichier" type="file" name="fichier" required accept=".pdf,.doc,.docx,.png,.jpg,.jpeg"><span class="hint">Le nom d'origine est conservé, l'accès est contrôlé côté serveur.</span></div>
      <div class="field span2"><label for="commentaire">Commentaire</label><textarea id="commentaire" name="commentaire"><c:out value="${form.commentaire}"/></textarea></div>
    </div>
    <div class="form-pied"><a class="btn" href="<c:url value='/rh/documents'/>">Annuler</a><button class="btn btn-primary" type="submit">Déposer le document</button></div>
  </form>
</t:layout>
