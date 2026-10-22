<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout titre="Accès refusé">

  <div class="card stack" style="max-width:520px">
    <h1>Accès refusé</h1>
    <p class="muted">Votre rôle ne vous permet pas d'ouvrir cette page.</p>
    <div><a class="btn btn-primary" href="<c:url value='/accueil'/>">Retour à l'accueil</a></div>
  </div>

</t:layout>
