<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout titre="Test technique" actif="demandes">
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

    <c:if test="${not empty message}">
      <div class="alert alert-info">${message}</div>
    </c:if>

    <c:if test="${empty reponses and test == null}">
      <div class="alert alert-warn">Aucune réponse n’a encore été enregistrée pour cette candidature.</div>
    </c:if>

    <c:if test="${not empty reponses}">
      <div class="stack">
        <c:forEach var="reponse" items="${reponses}">
          <div class="card" style="background:#fff; border-color: var(--line-strong);">
            <div class="question">
              <div>
                <div class="chip">${reponse.questionRef}</div>
                <p style="margin-top:10px;">Type : ${reponse.typeQuestion}</p>
              </div>
              <div class="badge ${(reponse.correct) ? 'badge-ok' : 'badge-warn'}">
                <c:choose>
                  <c:when test="${reponse.correct}">Correct</c:when>
                  <c:otherwise>À vérifier</c:otherwise>
                </c:choose>
              </div>
            </div>
            <div class="field" style="margin-top:12px;">
              <label>Réponse</label>
              <textarea readonly><c:out value="${reponse.reponseTextuelle}" /></textarea>
            </div>
          </div>
        </c:forEach>
      </div>
    </c:if>
  </div>
</t:layout>
