<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:include page="/WEB-INF/views/recrutement/fragments/entete.jsp"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="barre"><h1><c:out value="${titrePage}"/></h1><a class="btn sec" href="${ctx}/recrutement/offres">Offres</a></div>
<form class="filtre" method="get" action="${ctx}/rh/recrutement/reserve">
  <input type="text" name="q" value="<c:out value='${q}'/>" placeholder="Nom, e-mail ou numéro">
  <button class="btn" type="submit">Rechercher</button>
</form>
<c:choose><c:when test="${empty page.content}"><div class="carte">Aucune candidature spontanée dans la réserve.</div></c:when><c:otherwise>
<table><thead><tr><th>Numéro</th><th>Candidat</th><th>Reçue le</th><th>Rattacher à une offre ouverte</th></tr></thead><tbody>
<c:forEach items="${page.content}" var="c">
<tr><td><c:out value="${c.numero}"/></td><td><c:out value="${c.prenom}"/> <c:out value="${c.nom}"/><br><c:out value="${c.email}"/></td>
<td><c:out value="${c.dateReception}"/></td><td>
<c:choose><c:when test="${empty offres}">Aucune offre ouverte</c:when><c:otherwise>
<form method="post" action="${ctx}/rh/recrutement/reserve/${c.id}/rattacher">
  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
  <select name="offreId" required><c:forEach items="${offres}" var="o"><option value="<c:out value='${o.idOffre}'/>"><c:out value="${o.reference}"/> — <c:out value="${o.titre}"/></option></c:forEach></select>
  <button class="btn" type="submit">Rattacher</button>
</form></c:otherwise></c:choose></td></tr>
</c:forEach></tbody></table>
<c:if test="${page.totalPages gt 1}"><nav class="pagination"><c:forEach begin="0" end="${page.totalPages - 1}" var="p"><c:url var="lien" value="/rh/recrutement/reserve"><c:param name="q" value="${q}"/><c:param name="page" value="${p}"/></c:url><a href="${lien}"><c:out value="${p + 1}"/></a></c:forEach></nav></c:if>
</c:otherwise></c:choose>
<jsp:include page="/WEB-INF/views/recrutement/fragments/pied.jsp"/>
