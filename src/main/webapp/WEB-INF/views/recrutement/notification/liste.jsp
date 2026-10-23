<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/views/recrutement/fragments/entete.jsp"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="barre">
  <h1><c:out value="${titrePage}"/></h1>
  <a class="btn sec" href="${ctx}/recrutement/offres">Retour aux offres</a>
</div>
<c:choose>
  <c:when test="${empty notificationsRecrutement}">
    <div class="carte">Aucune notification de recrutement.</div>
  </c:when>
  <c:otherwise>
    <c:forEach items="${notificationsRecrutement}" var="n">
      <article class="carte">
        <strong><c:out value="${n.objet}"/></strong>
        <c:if test="${!n.lu}"><span class="badge">Non lue</span></c:if>
        <p><c:out value="${n.message}"/></p>
        <small><c:out value="${n.dateCreation}"/></small>
        <c:if test="${!n.lu}">
          <form method="post" action="${ctx}/recrutement/notifications/${n.idNotificationRecrutement}/lue">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <button class="btn" type="submit">Marquer comme lue</button>
          </form>
        </c:if>
      </article>
    </c:forEach>
  </c:otherwise>
</c:choose>
<jsp:include page="/WEB-INF/views/recrutement/fragments/pied.jsp"/>
