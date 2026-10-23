<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="fr"><head><meta charset="UTF-8"><title>Candidature reçue - SIRH</title></head><body>
<main><h1>Votre dossier est en cours d'étude</h1><p>Votre numéro de candidature : <strong><c:out value="${numeroCandidature}"/></strong></p>
<p>Un accusé de réception sera envoyé à l'adresse indiquée.</p><a href="${pageContext.request.contextPath}/offres">Retour aux offres</a></main></body></html>
