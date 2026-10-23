<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title><c:out value="${offre.titre}"/> - SIRH</title>
<link rel="stylesheet" href="<c:url value='/css/base.css'/>">
<link rel="stylesheet" href="<c:url value='/css/components.css'/>">
<style>body{font-family:system-ui,sans-serif;max-width:900px;margin:2rem auto;padding:0 1rem;color:#243447}.card{border:1px solid #d8dee8;border-radius:8px;padding:1rem;margin:1rem 0}.btn{display:inline-block;padding:.65rem 1rem;border-radius:5px;background:#12569b;color:white;text-decoration:none}</style></head><body>
<header><a href="${pageContext.request.contextPath}/offres">Retour aux offres</a></header>
<main><h1><c:out value="${offre.titre}"/></h1>
<p>Référence <c:out value="${offre.reference}"/> · Date limite : <c:out value="${offre.dateLimite}"/></p>
<section class="card"><h2>Description</h2><p><c:out value="${offre.description}"/></p></section>
<section class="card"><h2>Missions</h2><p><c:out value="${offre.missions}"/></p></section>
<section class="card"><h2>Salaire proposé</h2><p><c:out value="${offre.salaire}"/></p></section>
<section class="card"><h2>Critères</h2><c:choose><c:when test="${empty offre.criteres}"><p>Consultez le détail du poste dans la description.</p></c:when><c:otherwise><ul>
<c:forEach items="${offre.criteres}" var="critere"><li><c:if test="${critere.obligatoire}">Obligatoire — </c:if><c:out value="${critere.nature}"/> : <c:out value="${critere.libelle}"/><c:if test="${not empty critere.valeurMin}"> — minimum <c:out value="${critere.valeurMin}"/></c:if></li></c:forEach>
</ul></c:otherwise></c:choose></section>
<a class="btn" href="${pageContext.request.contextPath}/candidatures/nouvelle?offreId=${offre.id}">Postuler</a>
</main></body></html>
