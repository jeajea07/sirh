<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- En-tête des vues 2A : ouvre le gabarit commun (barre latérale + barre du haut).
     S'associe à pied.jsp qui referme la page. --%>
<%-- Spring MVC fait un forward vers la JSP : l'URI d'origine est portée par l'attribut de forward. --%>
<c:set var="uri" value="${requestScope['jakarta.servlet.forward.request_uri']}"/>
<c:if test="${empty uri}"><c:set var="uri" value="${pageContext.request.requestURI}"/></c:if>
<c:set var="actif" value="${fn:contains(uri, 'candidature') or fn:contains(uri, '/reserve') ? 'candidatures' : 'offres'}"/>
<!DOCTYPE html>
<html lang="fr">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title><c:out value="${titrePage}"/> — SIRH</title>
<link rel="icon" href="<c:url value='/logo.svg'/>">
<link rel="stylesheet" href="<c:url value='/css/base.css'/>">
<link rel="stylesheet" href="<c:url value='/css/components.css'/>">
<link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
<%-- Pont temporaire : les vues 2A gardent leurs classes propres (carte, barre, filtre…).
     À retirer au fur et à mesure de leur migration vers les classes communes. --%>
<style>
  .barre { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; margin-bottom: 16px; }
  .carte { background: var(--card); border: 1px solid var(--line); border-radius: 12px; padding: 16px 20px; margin-bottom: 16px; }
  .carte h2 { margin-bottom: 10px; }
  .filtre { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; }
  .filtre input, .filtre select { width: auto; }
  .grille { display: grid; grid-template-columns: 1fr 1fr; gap: 0 16px; }
  .btn.sec { background: var(--steel-100); color: var(--ink); }
  .btn.ok { background: var(--ok); color: #fff; }
  .btn.danger { background: var(--bad); color: #fff; }
  .alert.ok { background: var(--ok-bg); color: var(--ok); }
  .alert.ko { background: var(--bad-bg); color: var(--bad); }
  .alert.warn { background: var(--warn-bg); color: var(--warn); }
  .erreur, .err { color: var(--bad); font-size: .84rem; }
  .badge.PUBLIEE { background: var(--ok-bg); color: var(--ok); }
  .badge.VALIDEE { background: var(--info-bg); color: var(--info); }
  .badge.SUSPENDUE { background: var(--warn-bg); color: var(--warn); }
  .badge.ANNULEE { background: var(--bad-bg); color: var(--bad); }
  .badge.TERMINEE { background: var(--steel-100); color: var(--muted); }
  .pagination a { display: inline-block; padding: 4px 10px; margin-right: 4px; border: 1px solid var(--line); border-radius: 6px; background: var(--card); }
  .pagination a.actif { background: var(--navy-800); border-color: var(--navy-800); color: #fff; }
  form.inline { display: inline; }
  dl { display: grid; grid-template-columns: 180px 1fr; gap: 6px 12px; margin: 0; }
  dt { font-weight: 600; }
</style>
</head>
<body data-role="<c:out value='${roleCode}'/>">
<div class="app">
  <jsp:include page="/WEB-INF/jsp/layouts/sidebar.jsp"><jsp:param name="actif" value="${actif}"/></jsp:include>
  <div class="app-main">
    <jsp:include page="/WEB-INF/jsp/layouts/topbar.jsp"><jsp:param name="titre" value="${titrePage}"/></jsp:include>
    <main class="content">
    <c:if test="${not empty message}"><div class="alert alert-ok"><c:out value="${message}"/></div></c:if>
    <c:if test="${not empty erreur}"><div class="alert alert-bad"><c:out value="${erreur}"/></div></c:if>
    <c:if test="${not empty nbNotificationsRecrutement}">
      <p class="muted small" style="margin-bottom:12px"><a href="<c:url value='/recrutement/notifications'/>">Notifications de recrutement (<c:out value="${nbNotificationsRecrutement}"/>)</a></p>
    </c:if>
