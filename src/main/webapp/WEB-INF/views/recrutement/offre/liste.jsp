<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/views/recrutement/fragments/entete.jsp"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<div class="barre">
  <h1>Offres d'emploi</h1>
  <a class="btn" href="${ctx}/recrutement/offres/nouveau">Nouvelle offre</a>
</div>

<form class="filtre" method="get" action="${ctx}/recrutement/offres">
  <input type="text" name="q" value="<c:out value='${q}'/>" placeholder="Rechercher par titre">
  <select name="statut">
    <option value="">Tous les statuts</option>
    <c:forEach items="${statuts}" var="s">
      <option value="<c:out value='${s}'/>" <c:if test="${s == statut}">selected</c:if>><c:out value="${s}"/></option>
    </c:forEach>
  </select>
  <button class="btn" type="submit">Filtrer</button>
</form>

<table>
  <thead>
    <tr><th>Reference</th><th>Titre</th><th>Postes</th><th>Candidatures</th><th>Date limite</th><th>Statut</th><th></th></tr>
  </thead>
  <tbody>
    <c:forEach items="${page.content}" var="o">
      <tr>
        <td><c:out value="${o.reference}"/></td>
        <td><c:out value="${o.titre}"/></td>
        <td><c:out value="${o.nombrePostes}"/></td>
        <td><c:out value="${o.nbCandidatures}"/></td>
        <td><c:out value="${o.dateLimite}"/></td>
        <td><span class="badge"><c:out value="${o.statut}"/></span></td>
        <td><a href="${ctx}/recrutement/offres/${o.idOffre}">Ouvrir</a></td>
      </tr>
    </c:forEach>
    <c:if test="${empty page.content}">
      <tr><td colspan="7">Aucune offre.</td></tr>
    </c:if>
  </tbody>
</table>

<c:if test="${page.totalPages > 1}">
  <p class="pagination">
    <c:forEach begin="0" end="${page.totalPages - 1}" var="i">
      <c:url var="lien" value="/recrutement/offres">
        <c:param name="q" value="${q}"/>
        <c:param name="statut" value="${statut}"/>
        <c:param name="page" value="${i}"/>
      </c:url>
      <a class="${i == page.number ? 'actif' : ''}" href="${lien}"><c:out value="${i + 1}"/></a>
    </c:forEach>
  </p>
</c:if>

<jsp:include page="/WEB-INF/views/recrutement/fragments/pied.jsp"/>
