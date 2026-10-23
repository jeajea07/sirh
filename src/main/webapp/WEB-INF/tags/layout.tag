<%@ tag description="Gabarit des pages connectées : barre latérale + barre du haut + contenu" pageEncoding="UTF-8" body-content="scriptless" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="titre" required="true" description="Titre affiché dans la barre du haut et l'onglet" %>
<%@ attribute name="actif" required="false" description="Lien actif du menu (voir data-nav dans layouts/sidebar.jsp)" %>
<%--
  Utilisation dans une page :
    <%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
    <t:layout titre="Accueil" actif="accueil"> ...contenu... </t:layout>
  Le rôle (roleCode), le login, etc. viennent de LayoutAdvice.java.
--%>
<!DOCTYPE html>
<html lang="fr">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title><c:out value="${titre}"/> — SIRH</title>
<link rel="icon" href="<c:url value='/logo.svg'/>">
<link rel="stylesheet" href="<c:url value='/css/base.css'/>">
<link rel="stylesheet" href="<c:url value='/css/components.css'/>">
<link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
</head>
<body data-role="<c:out value='${roleCode}'/>">
<div class="app">
  <jsp:include page="/WEB-INF/jsp/layouts/sidebar.jsp">
    <jsp:param name="actif" value="${actif}"/>
  </jsp:include>
  <div class="app-main">
    <jsp:include page="/WEB-INF/jsp/layouts/topbar.jsp">
      <jsp:param name="titre" value="${titre}"/>
    </jsp:include>
    <main class="content">
      <jsp:doBody/>
    </main>
  </div>
</div>
<script src="<c:url value='/js/ui.js'/>"></script>
<script src="<c:url value='/js/select.js'/>"></script>
<script src="<c:url value='/js/modele-onboarding.js'/>"></script>
</body>
</html>
