<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="d" value="${demande}"/>
<t:layout titre="Décision finale DG" actif="demandes-dg">
  <div class="page-head"><div><h1>Décision finale — demande n° <c:out value="${d.numero}"/> <span class="badge ${d.statutBadge}" style="vertical-align:middle"><c:out value="${d.statutLibelle}"/></span></h1>
    <p><c:out value="${d.poste.designation}"/> — <c:out value="${d.departement.designation}"/>, reçue par la DG le <c:out value="${dateReception}"/></p></div>
    <a class="btn" href="<c:url value='/dg/demandes'/>">Retour à la liste</a></div>
  <c:if test="${not empty erreurs}"><div class="alert alert-bad" style="margin-bottom:16px"><b>Action impossible :</b><ul><c:forEach var="e" items="${erreurs}"><li><c:out value="${e}"/></li></c:forEach></ul></div></c:if>
  <div class="grid g-32" style="grid-template-columns:1fr 1fr;align-items:start">
    <div class="card"><h2 style="margin-bottom:12px">Détail de la demande</h2><dl class="kv">
      <dt>Numéro</dt><dd><c:out value="${d.numero}"/></dd><dt>Département</dt><dd><c:out value="${d.departement.designation}"/></dd>
      <dt>Poste</dt><dd><c:out value="${d.poste.designation}"/></dd><dt>Nombre de postes</dt><dd><c:out value="${d.nombrePostes}"/></dd>
      <dt>Motif</dt><dd><c:out value="${d.motifLibelle}"/></dd><dt>Urgence</dt><dd><c:out value="${d.urgenceLibelle}"/></dd>
      <dt>Contrat</dt><dd><c:out value="${d.typeContrat.code}"/><c:if test="${not empty d.dureeMois}"> — <c:out value="${d.dureeMois}"/> mois</c:if></dd>
      <dt>Salaire mensuel</dt><dd><c:out value="${d.salaireFr}"/></dd><dt>Date souhaitée</dt><dd><c:out value="${d.dateSouhaiteeFr}"/></dd>
    </dl><c:if test="${not empty d.justification}"><h3 style="margin:16px 0 6px">Justification</h3><p class="demande-texte"><c:out value="${d.justification}"/></p></c:if></div>
    <div class="card"><h2 style="margin-bottom:12px">Critères de recrutement</h2><c:choose><c:when test="${not empty d.critereRecrutement}"><dl class="kv">
      <dt>Niveau d'étude</dt><dd><c:out value="${d.critereRecrutement.niveauEtude}"/></dd><dt>Expérience</dt><dd><c:out value="${d.critereRecrutement.anneeExperience}"/> an(s)</dd>
    </dl><h3 style="margin:16px 0 6px">Compétences</h3><p class="demande-texte"><c:out value="${d.critereRecrutement.competence}"/></p></c:when><c:otherwise><p class="muted">Aucun critère de recrutement renseigné.</p></c:otherwise></c:choose></div>
  </div>
  <div class="card" style="margin-top:16px"><h2 style="margin-bottom:12px">Décision de la Direction générale</h2><p class="muted small" style="margin-bottom:12px">La validation confirme définitivement la demande. Le renvoi et le rejet nécessitent un commentaire destiné au demandeur.</p>
    <form method="post" action="<c:url value='/dg/demandes/${d.id}'/>" class="stack"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
      <div class="field"><label for="commentaire">Commentaire</label><textarea id="commentaire" name="commentaire"><c:out value="${commentaire}"/></textarea></div>
      <div class="form-pied"><a class="btn" href="<c:url value='/dg/demandes'/>">Annuler</a>
        <button class="btn btn-danger" type="submit" name="action" value="rejeter" data-commentaire="requis">Rejeter</button>
        <button class="btn" type="submit" name="action" value="renvoyer" data-commentaire="requis">Renvoyer pour correction</button>
        <button class="btn btn-ok" type="submit" name="action" value="valider">Valider définitivement</button></div>
    </form>
  </div>
  <script src="<c:url value='/js/rh.js'/>?v=2"></script>
</t:layout>
