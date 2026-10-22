<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="d" value="${suivi.demande}"/>
<t:layout titre="Suivi de la demande" actif="demandes">

  <div class="page-head">
    <div>
      <h1>Demande n° <c:out value="${d.numero}"/> <span class="badge ${d.statutBadge}" style="vertical-align:middle"><c:out value="${d.statutLibelle}"/></span></h1>
      <p><c:choose><c:when test="${empty d.poste}">Poste à préciser</c:when><c:otherwise><c:out value="${d.poste.designation}"/></c:otherwise></c:choose>
        — <c:out value="${d.departement.designation}"/>, créée le <c:out value="${d.dateCreationFr}"/></p>
    </div>
    <div class="row wrap">
      <a class="btn" href="<c:url value='/demandes'/>">Mes demandes</a>
      <c:if test="${d.modifiable}">
        <a class="btn" href="<c:url value='/demandes/${d.id}/modifier'/>">Modifier</a>
        <form method="post" action="<c:url value='/demandes/${d.id}/soumettre'/>">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
          <button class="btn btn-primary" type="submit">Soumettre</button>
        </form>
      </c:if>
      <c:if test="${d.retirable}">
        <form method="post" action="<c:url value='/demandes/${d.id}/retirer'/>" data-confirm="Retirer cette demande ? Elle sera fermée et ne suivra plus le circuit.">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
          <button class="btn btn-danger" type="submit">Retirer</button>
        </form>
      </c:if>
    </div>
  </div>

  <c:if test="${not empty succes}"><div class="alert alert-ok" style="margin-bottom:16px"><c:out value="${succes}"/></div></c:if>
  <c:if test="${not empty alertes}">
    <div class="alert alert-warn" style="margin-bottom:16px"><ul style="margin:0"><c:forEach var="a" items="${alertes}"><li><c:out value="${a}"/></li></c:forEach></ul></div>
  </c:if>
  <c:if test="${d.statut == 'A_CORRIGER'}">
    <div class="alert alert-warn" style="margin-bottom:16px"><b>Votre demande a été renvoyée pour correction.</b>
      <c:if test="${not empty suivi.commentaireDecision}"><br>Commentaire : <c:out value="${suivi.commentaireDecision}"/></c:if>
      <br>Modifiez-la puis soumettez-la de nouveau : elle repartira de l'étape qui l'a renvoyée.</div>
  </c:if>
  <c:if test="${d.statut == 'REJETEE'}">
    <div class="alert alert-bad" style="margin-bottom:16px"><b>Votre demande a été rejetée.</b>
      <c:if test="${not empty suivi.commentaireDecision}"><br>Motif : <c:out value="${suivi.commentaireDecision}"/></c:if></div>
  </c:if>
  <c:if test="${d.statut == 'RETIREE'}">
    <div class="alert alert-info" style="margin-bottom:16px">Vous avez retiré cette demande. Elle reste dans votre historique.</div>
  </c:if>
  <c:if test="${d.statut == 'BROUILLON'}">
    <div class="alert alert-info" style="margin-bottom:16px">Cette demande est un brouillon : elle n'a pas encore été envoyée. Complétez-la puis cliquez sur « Soumettre ».</div>
  </c:if>

  <div class="card" style="margin-bottom:16px">
    <div class="steps">
      <c:forEach var="e" items="${suivi.etapes}">
        <div class="step ${e.etat}"><i><c:out value="${e.symbole}"/></i><span><c:out value="${e.libelle}"/><c:if test="${not empty e.info}"><small><c:out value="${e.info}"/></small></c:if></span></div>
      </c:forEach>
    </div>
    <c:if test="${not empty suivi.enCours}"><p class="muted" style="margin-top:16px;text-align:center"><c:out value="${suivi.enCours}"/></p></c:if>
  </div>

  <div class="grid g-32">
    <div class="stack">

      <div class="card">
        <h2 style="margin-bottom:12px">Détail de la demande</h2>
        <dl class="kv">
          <dt>Département</dt><dd><c:out value="${d.departement.designation}"/></dd>
          <dt>Poste</dt><dd><c:choose><c:when test="${empty d.poste}">—</c:when><c:otherwise><c:out value="${d.poste.designation}"/></c:otherwise></c:choose></dd>
          <dt>Nombre de postes</dt><dd><c:out value="${d.nombrePostes}"/></dd>
          <dt>Motif</dt><dd><c:out value="${d.motifLibelle}"/></dd>
          <dt>Urgence</dt><dd><c:out value="${d.urgenceLibelle}"/></dd>
          <dt>Contrat</dt><dd><c:choose><c:when test="${empty d.typeContrat}">—</c:when><c:otherwise><c:out value="${d.typeContrat.code}"/><c:if test="${not empty d.dureeMois}"> (<c:out value="${d.dureeMois}"/> mois)</c:if></c:otherwise></c:choose></dd>
          <dt>Salaire proposé</dt><dd><c:out value="${d.salaireFr}"/></dd>
          <dt>Date souhaitée</dt><dd><c:out value="${d.dateSouhaiteeFr}"/></dd>
        </dl>
        <c:if test="${not empty d.justification}">
          <h3 style="margin:16px 0 6px">Justification</h3>
          <p class="demande-texte"><c:out value="${d.justification}"/></p>
        </c:if>
      </div>

      <div class="card">
        <h2 style="margin-bottom:12px">Critères de recrutement</h2>
        <c:choose>
          <c:when test="${empty d.critereRecrutement}"><p class="muted">Aucun critère renseigné.</p></c:when>
          <c:otherwise>
            <dl class="kv">
              <dt>Niveau d'étude</dt><dd><c:out value="${d.critereRecrutement.niveauEtudeLibelle}"/></dd>
              <dt>Expérience</dt><dd><c:choose><c:when test="${empty d.critereRecrutement.anneeExperience}">—</c:when><c:otherwise><c:out value="${d.critereRecrutement.anneeExperience}"/> an(s)</c:otherwise></c:choose></dd>
              <dt>Compétences</dt><dd><c:out value="${empty d.critereRecrutement.competence ? '—' : d.critereRecrutement.competence}"/></dd>
              <dt>Missions</dt><dd><c:out value="${empty d.critereRecrutement.missions ? '—' : d.critereRecrutement.missions}"/></dd>
            </dl>
          </c:otherwise>
        </c:choose>
      </div>

    </div>

    <div class="card">
      <h2 style="margin-bottom:12px">Historique</h2>
      <c:choose>
        <c:when test="${empty suivi.evenements}"><p class="muted">Aucun évènement pour l'instant.</p></c:when>
        <c:otherwise>
          <ul class="timeline">
            <c:forEach var="ev" items="${suivi.evenements}">
              <li><time><c:out value="${ev.dateFr}"/></time><p><b><c:out value="${ev.titre}"/></b></p>
                <c:if test="${not empty ev.detail}"><p class="muted small"><c:out value="${ev.detail}"/></p></c:if></li>
            </c:forEach>
          </ul>
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <script src="<c:url value='/js/demande.js'/>?v=2"></script>
</t:layout>
