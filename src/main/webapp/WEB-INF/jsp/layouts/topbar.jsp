<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  TOPBAR — barre du haut (adaptée de teamup-rh/layouts/topbar.html).
  Paramètre : titre = titre de la page.
  La recherche du modèle d'origine n'est pas reprise (pas encore de fonction de recherche).
--%>
<header class="topbar">
  <button class="burger" type="button" aria-label="Ouvrir le menu"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 6h16M4 12h16M4 18h16"/></svg></button>
  <h2><c:out value="${param.titre}"/></h2>

  <div style="position:relative;margin-left:auto">
    <button class="icon-btn" type="button" data-bell aria-label="Notifications"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M6 16V11a6 6 0 1112 0v5l2 2H4l2-2zM10 21h4"/></svg></button>
    <div class="drop">
      <h3>Notifications</h3>
      <%-- Les notifications seront listées ici, une ligne <p>Message<small>Détail</small></p> chacune. --%>
      <p>Aucune notification.</p>
    </div>
  </div>
  <div class="avatar"><c:out value="${initiales}"/></div>
</header>
