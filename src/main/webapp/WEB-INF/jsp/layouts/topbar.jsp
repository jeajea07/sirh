<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<header class="topbar">
  <button class="burger" type="button" aria-label="Ouvrir le menu"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 6h16M4 12h16M4 18h16"/></svg></button>
  <h2><c:out value="${param.titre}"/></h2>

  <c:if test="${empty role}">
    <a class="btn btn-sm btn-primary" style="margin-left:auto" href="<c:url value='/connexion'/>">Connexion</a>
  </c:if>
  <c:if test="${not empty role}">
  <div style="position:relative;margin-left:auto">
    <button class="icon-btn" type="button" data-bell aria-label="Notifications"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M6 16V11a6 6 0 1112 0v5l2 2H4l2-2zM10 21h4"/></svg><c:if test="${nbNotifications > 0}"><span class="dot"></span></c:if></button>
    <div class="drop">
      <h3>Notifications<c:if test="${nbNotifications > 0}"> <span class="badge badge-info"><c:out value="${nbNotifications}"/> non lue(s)</span></c:if></h3>
      <c:choose>
        <c:when test="${empty notifications}"><p>Aucune notification.</p></c:when>
        <c:otherwise>
          <c:forEach var="n" items="${notifications}">
            <c:choose>
              <c:when test="${not empty n.lien}">
                <a class="notif ${n.lue ? '' : 'non-lue'}" href="<c:url value='${n.lien}'/>"><c:out value="${n.message}"/><small><c:out value="${n.dateFr}"/></small></a>
              </c:when>
              <c:otherwise>
                <div class="notif ${n.lue ? '' : 'non-lue'}"><c:out value="${n.message}"/><small><c:out value="${n.dateFr}"/></small></div>
              </c:otherwise>
            </c:choose>
          </c:forEach>
          <c:if test="${nbNotifications > 0}">
            <form method="post" action="<c:url value='/notifications/lues'/>" class="notif-pied">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
              <button class="btn btn-sm btn-block" type="submit">Tout marquer comme lu</button>
            </form>
          </c:if>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
  <div class="avatar"><c:out value="${initiales}"/></div>
  </c:if>
</header>
