<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title><c:out value="${titrePage}"/> - SIRH</title>
<link rel="stylesheet" href="<c:url value='/css/base.css'/>">
<link rel="stylesheet" href="<c:url value='/css/components.css'/>">
<style>body{font-family:system-ui,sans-serif;max-width:960px;margin:2rem auto;padding:0 1rem;color:#243447}article{border:1px solid #d8dee8;border-radius:8px;padding:1rem;margin:1rem 0}a{color:#12569b}.muted{color:#596579}</style></head><body>
<header><a href="${pageContext.request.contextPath}/offres">SIRH — Recrutement</a></header>
<main><h1><c:out value="${titrePage}"/></h1>
<form method="get" action="${pageContext.request.contextPath}/offres"><label for="q">Rechercher une offre</label>
<input id="q" type="search" name="q" value="<c:out value='${q}'/>"><button type="submit">Rechercher</button></form>
<c:choose><c:when test="${empty offresPubliques}"><p>Aucune offre n'est ouverte actuellement.</p></c:when><c:otherwise>
<c:forEach items="${offresPubliques}" var="offre"><article><h2><a href="${pageContext.request.contextPath}/offres/${offre.id}"><c:out value="${offre.titre}"/></a></h2>
<p class="muted">Référence <c:out value="${offre.reference}"/> · Date limite : <c:out value="${offre.dateLimite}"/></p>
<p><c:out value="${offre.description}"/></p>
<a href="${pageContext.request.contextPath}/offres/${offre.id}">Consulter l'offre et postuler</a></article></c:forEach>
<c:if test="${offres.totalPages gt 1}"><nav aria-label="Pagination"><c:forEach begin="0" end="${offres.totalPages - 1}" var="p">
<c:url var="lien" value="/offres"><c:param name="q" value="${q}"/><c:param name="page" value="${p}"/></c:url>
<a href="${lien}"><c:out value="${p + 1}"/></a> </c:forEach></nav></c:if>
</c:otherwise></c:choose>
<p>Vous ne trouvez pas d'offre correspondant à votre profil ? <a href="${pageContext.request.contextPath}/candidatures/nouvelle">Déposer une candidature spontanée</a>.</p>
</main></body></html>
