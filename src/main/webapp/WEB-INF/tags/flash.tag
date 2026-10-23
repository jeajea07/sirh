<%@ tag description="Messages de succès et d'erreur" pageEncoding="UTF-8" body-content="empty" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty succes}"><div class="alert alert-ok" style="margin-bottom:16px"><c:out value="${succes}"/></div></c:if>
<c:if test="${not empty erreur}"><div class="alert alert-bad" style="margin-bottom:16px"><c:out value="${erreur}"/></div></c:if>
<c:if test="${not empty erreurs}">
  <div class="alert alert-bad" style="margin-bottom:16px">
    <c:forEach var="message" items="${erreurs}"><div><c:out value="${message}"/></div></c:forEach>
  </div>
</c:if>
