<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title><c:out value="${param.titre}"/> — TeamUp Carrières</title>
<link rel="icon" href="<c:url value='/logo.svg'/>">
<link rel="stylesheet" href="<c:url value='/css/base.css'/>">
<link rel="stylesheet" href="<c:url value='/css/components.css'/>">
<link rel="stylesheet" href="<c:url value='/css/vitrine.css'/>">
</head>
<body class="v-body" data-role="site">
<header class="v-header">
  <div class="v-wrap">
    <a class="v-brand" href="<c:url value='/'/>"><img src="<c:url value='/logo.svg'/>" alt="">TeamUp <span>Carrières</span></a>
    <nav class="v-nav" aria-label="Navigation principale">
      <a class="${param.actif == 'accueil' ? 'actif' : ''}" href="<c:url value='/'/>">Accueil</a>
      <a class="${param.actif == 'offres' ? 'actif' : ''}" href="<c:url value='/offres'/>">Offres d'emploi</a>
      <a class="${param.actif == 'spontanee' ? 'actif' : ''}" href="<c:url value='/candidatures/nouvelle'/>">Candidature spontanée</a>
      <a class="v-espace" href="<c:url value='/connexion'/>">Se connecter</a>
    </nav>
  </div>
</header>
