<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout titre="Offres d'emploi" actif="offres-rh">
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<div class="page-head">
  <h1>Offres d'emploi</h1>
  <a class="btn btn-primary" href="${ctx}/recrutement/offres/nouveau">Nouvelle offre depuis une demande</a>
</div>

<c:if test="${not empty message}"><div class="alert alert-ok" style="margin-bottom:16px"><c:out value="${message}"/></div></c:if>
<c:if test="${not empty erreur}"><div class="alert alert-bad" style="margin-bottom:16px"><c:out value="${erreur}"/></div></c:if>

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

<div class="card card-flat"><div class="table-wrap"><table>
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
</table></div></div>

<c:if test="${page.totalPages > 1}">
  <p class="pagination" style="margin-top:16px">
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

</t:layout>
