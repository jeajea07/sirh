<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Connexion — SIRH</title>
<link rel="icon" href="<c:url value='/logo.svg'/>">
<link rel="stylesheet" href="<c:url value='/css/base.css'/>">
<link rel="stylesheet" href="<c:url value='/css/components.css'/>">
<link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
</head>
<body>
<div class="login">
  <div class="login-side"><img src="<c:url value='/logo-original.png'/>" alt="SIRH"></div>

  <div class="login-form">
    <form id="form-connexion" method="post" action="<c:url value='/connexion'/>" novalidate class="stack">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

      <div>
        <h1>Connexion</h1>
        <p class="muted" style="margin-top:4px">Accédez à votre espace.</p>
      </div>

      <%-- messages venant du serveur (Spring Security) --%>
      <c:if test="${param.erreur != null}">
        <div class="alert alert-bad">Identifiant ou mot de passe incorrect.</div>
      </c:if>
      <c:if test="${param.deconnecte != null}">
        <div class="alert alert-ok">Vous êtes déconnecté.</div>
      </c:if>

      <%-- messages venant de js/login.js (champs vides) --%>
      <div class="form-errors"></div>

      <div class="field">
        <label for="login">Email *</label>
        <input type="email" id="login" name="login" required autofocus autocomplete="username">
        <span class="hint">Votre email professionnel.</span>
      </div>
      <div class="field">
        <label for="mdp">Mot de passe *</label>
        <input type="password" id="mdp" name="mdp" required autocomplete="current-password">
      </div>

      <button class="btn btn-primary btn-block" type="submit">Se connecter</button>
    </form>
    <p class="muted" style="margin-top:1rem">Vous cherchez un emploi ? <a href="<c:url value='/offres'/>">Consulter les offres publiques</a>.</p>
  </div>
</div>

<script src="<c:url value='/js/login.js'/>"></script>
</body>
</html>
