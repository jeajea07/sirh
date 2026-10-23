<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Nouvelle affectation" actif="affectations">
  <div class="page-head"><div><h1>Nouvelle affectation</h1><p>Le salarié change de poste, de département ou de responsable.</p></div><a class="btn" href="<c:url value='/rh/affectations'/>">Retour</a></div>
  <f:flash/>
  <form method="post" action="<c:url value='/rh/affectations'/>" class="card">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
    <div class="form-grid">
      <div class="field span2"><label for="idEmploye">Salarié *</label><select id="idEmploye" name="idEmploye" required><option value="">Choisir…</option><c:forEach var="e" items="${employes}"><option value="${e.id}" ${form.idEmploye == e.id ? 'selected' : ''}><c:out value="${e.nomComplet}"/> — <c:out value="${e.matricule}"/></option></c:forEach></select></div>
      <div class="field"><label for="idPoste">Poste *</label><select id="idPoste" name="idPoste" required><option value="">Choisir…</option><c:forEach var="p" items="${postes}"><option value="${p.id}" ${form.idPoste == p.id ? 'selected' : ''}><c:out value="${p.designation}"/></option></c:forEach></select></div>
      <div class="field"><label for="idDepartement">Département *</label><select id="idDepartement" name="idDepartement" required><option value="">Choisir…</option><c:forEach var="d" items="${departements}"><option value="${d.id}" ${form.idDepartement == d.id ? 'selected' : ''}><c:out value="${d.designation}"/></option></c:forEach></select></div>
      <div class="field"><label for="idResponsable">Responsable</label><select id="idResponsable" name="idResponsable"><option value="">Aucun</option><c:forEach var="e" items="${employes}"><option value="${e.id}" ${form.idResponsable == e.id ? 'selected' : ''}><c:out value="${e.nomComplet}"/></option></c:forEach></select></div>
      <div class="field"><label for="site">Site</label><input id="site" type="text" name="site" value="<c:out value='${form.site}'/>"></div>
      <div class="field"><label for="dateDebut">Date de début *</label><input id="dateDebut" type="date" name="dateDebut" value="${form.dateDebut}" required></div>
      <div class="field"><label for="dateFin">Date de fin</label><input id="dateFin" type="date" name="dateFin" value="${form.dateFin}"></div>
      <div class="field span2"><label for="motif">Motif</label><textarea id="motif" name="motif"><c:out value="${form.motif}"/></textarea></div>
    </div>
    <div class="form-pied"><a class="btn" href="<c:url value='/rh/affectations'/>">Annuler</a><button class="btn btn-primary" type="submit">Enregistrer</button></div>
  </form>
</t:layout>
