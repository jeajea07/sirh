<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Nouvel onboarding" actif="onboardings">
  <div class="page-head"><div><h1>Nouvel onboarding</h1><p>Le parcours est généré à partir d'un modèle actif.</p></div><a class="btn" href="<c:url value='/rh/onboardings'/>">Retour</a></div>
  <f:flash/>
  <form method="post" action="<c:url value='/rh/onboardings'/>" class="card">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
    <div class="form-grid">
      <div class="field span2"><label for="idEmploye">Salarié *</label><select id="idEmploye" name="idEmploye" required><option value="">Choisir…</option><c:forEach var="e" items="${employes}"><option value="${e.id}" ${form.idEmploye == e.id ? 'selected' : ''}><c:out value="${e.nomComplet}"/> — <c:out value="${e.matricule}"/></option></c:forEach></select></div>
      <div class="field"><label for="idContrat">Contrat *</label><select id="idContrat" name="idContrat" required><option value="">Choisir…</option><c:forEach var="c" items="${contrats}"><option value="${c.id}" ${form.idContrat == c.id ? 'selected' : ''}><c:out value="${c.reference}"/></option></c:forEach></select></div>
      <div class="field"><label for="idModeleOnboarding">Modèle *</label><select id="idModeleOnboarding" name="idModeleOnboarding" required><option value="">Choisir…</option><c:forEach var="m" items="${modeles}"><option value="${m.id}" ${form.idModeleOnboarding == m.id ? 'selected' : ''}><c:out value="${m.nom}"/> — v<c:out value="${m.version}"/> (<c:out value="${m.nombreTaches}"/> tâches)</option></c:forEach></select></div>
      <div class="field"><label for="dateDebutPrevue">Début prévu</label><input id="dateDebutPrevue" type="date" name="dateDebutPrevue" value="${form.dateDebutPrevue}"></div>
      <div class="field"><label for="idResponsableRh">Responsable RH</label><select id="idResponsableRh" name="idResponsableRh"><option value="">RH par défaut</option><c:forEach var="e" items="${employes}"><option value="${e.id}" ${form.idResponsableRh == e.id ? 'selected' : ''}><c:out value="${e.nomComplet}"/></option></c:forEach></select></div>
    </div>
    <div class="form-pied"><a class="btn" href="<c:url value='/rh/onboardings'/>">Annuler</a><button class="btn btn-primary" type="submit">Créer le parcours</button></div>
  </form>
</t:layout>
