<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout titre="Test technique" actif="test-candidat">
  <div class="page-head">
    <div>
      <h1>Test technique</h1>
      <p>Seuil de validation : <strong>${noteSeuil}</strong></p>
    </div>
    <div class="badge badge-info">Candidat</div>
  </div>

  <div class="card" style="margin-bottom:16px">
    <div class="test-bar">
      <span class="timer">${tempsRestant}</span>
      <div class="muted small">Statut : <c:choose><c:when test="${test != null}">Test enregistré</c:when><c:otherwise>Pas de test encore disponible</c:otherwise></c:choose></div>
    </div>

    <c:if test="${not empty success}">
      <div class="alert alert-ok"><c:out value="${success}" /></div>
    </c:if>

    <c:if test="${not empty message}">
      <div class="alert alert-info">${message}</div>
    </c:if>

    <form method="post" action="<c:url value='/candidat/test/${candidature.idCandidature}'/>">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
      <div class="stack">
        <div class="field">
          <label for="Q1">Question 1 — Quel est le mot-clé Java utilisé pour gérer des nombres décimaux précis ?</label>
          <input id="Q1" name="Q1" type="text" value="${param.Q1 != null ? param.Q1 : ''}" placeholder="Ex : BigDecimal">
        </div>

        <div class="field">
          <label for="Q2">Question 2 — Quelle classe Java est utilisée pour manipuler des montants financiers sans erreur d’arrondi ?</label>
          <input id="Q2" name="Q2" type="text" value="${param.Q2 != null ? param.Q2 : ''}" placeholder="Ex : BigDecimal">
        </div>

        <div class="field">
          <label for="Q3">Question 3 — En une phrase, expliquez pourquoi il faut valider une réponse avant de la finaliser.</label>
          <textarea id="Q3" name="Q3" rows="4" placeholder="Ex : Il faut valider pour vérifier la logique et éviter une erreur de saisie.">${param.Q3 != null ? param.Q3 : ''}</textarea>
        </div>
      </div>

      <div class="row wrap" style="margin-top: 18px;">
        <button class="btn btn-primary" type="submit">Soumettre le test</button>
        <a class="btn" href="<c:url value='/candidat'/>">Retour à l’espace candidat</a>
      </div>
    </form>
  </div>
</t:layout>
