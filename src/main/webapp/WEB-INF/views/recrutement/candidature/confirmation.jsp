<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout titre="Candidature reçue" actif="offres-public">
  <div class="card stack" style="max-width:720px;margin:24px auto;text-align:center">
    <span class="badge badge-ok" style="align-self:center">Candidature envoyée</span>
    <h1>Votre dossier est en cours d'étude</h1>
    <p class="muted">Conservez votre numéro de candidature pour le suivi.</p>
    <p class="big-score"><c:out value="${numeroCandidature}"/></p>
    <p>Un accusé de réception sera envoyé à l'adresse e-mail indiquée.</p>
    <div class="row wrap" style="justify-content:center"><a class="btn btn-primary" href="<c:url value='/offres'/>">Retour aux offres</a><a class="btn" href="<c:url value='/connexion'/>">Connexion</a></div>
  </div>
</t:layout>
