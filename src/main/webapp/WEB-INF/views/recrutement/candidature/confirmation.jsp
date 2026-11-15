<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="../vitrine/fragments/entete.jsp"><jsp:param name="titre" value="Candidature reçue"/></jsp:include>

<main class="v-main">
  <div class="v-wrap">
    <div class="card v-merci">
      <div class="v-check" aria-hidden="true">✓</div>
      <h1>Merci, votre candidature est bien reçue</h1>
      <p class="muted" style="margin-top:8px">Votre dossier est en cours d'étude. Conservez votre numéro de candidature :</p>
      <div class="v-numero"><c:out value="${numeroCandidature}"/></div>
      <p class="muted">Un accusé de réception sera envoyé à l'adresse e-mail indiquée. Si votre profil est retenu, la RH vous communiquera vos accès à l'espace candidat pour passer le test technique.</p>
      <div class="row" style="justify-content:center;margin-top:20px">
        <a class="btn btn-primary" href="<c:url value='/offres'/>">Voir les autres offres</a>
      </div>
    </div>
  </div>
</main>

<jsp:include page="../vitrine/fragments/pied.jsp"/>
