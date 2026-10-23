<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout titre="Test technique" actif="candidat">
  <div class="page-head">
    <div>
      <h1>Test technique</h1>
      <p>Seuil de validation : <strong>${noteSeuil}</strong> / 3</p>
    </div>
    <div class="badge badge-info"><c:out value="${tempsRestant}"/></div>
  </div>

  <c:if test="${not empty message}"><div class="alert alert-info" style="margin-bottom:16px"><c:out value="${message}"/></div></c:if>

  <c:if test="${not empty candidature}">
    <div class="card">
      <div class="card-head">
        <h2>Questions</h2>
        <span class="muted small">Statut : <c:out value="${empty test ? 'Non ouvert' : test.statut}"/></span>
      </div>

      <form method="post" action="<c:url value='/candidat/test/${candidature.idCandidature}'/>">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <div class="stack">
          <c:forEach var="question" items="${questions}">
            <div class="field">
              <label for="<c:out value='${question.reference}'/>"><c:out value="${question.label}"/></label>
              <c:choose>
                <c:when test="${question.type == 'LIBRE'}">
                  <textarea id="<c:out value='${question.reference}'/>" name="<c:out value='${question.reference}'/>" rows="4"><c:out value="${param[question.reference]}"/></textarea>
                </c:when>
                <c:otherwise>
                  <input id="<c:out value='${question.reference}'/>" name="<c:out value='${question.reference}'/>" type="text" value="<c:out value='${param[question.reference]}'/>">
                </c:otherwise>
              </c:choose>
            </div>
          </c:forEach>
        </div>
        <div class="row wrap" style="margin-top:18px">
          <button class="btn btn-primary" type="submit">Soumettre le test</button>
          <a class="btn" href="<c:url value='/candidat'/>">Retour à l’espace candidat</a>
        </div>
      </form>
    </div>
  </c:if>
</t:layout>
