<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/views/recrutement/fragments/entete.jsp"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="barre"><h1><c:out value="${titrePage}"/></h1><a class="btn sec" href="${ctx}/rh/recrutement/reserve">Réserve</a></div>
<div class="carte">En attente d'étude — conformes : <c:out value="${indicateurs.conformes}"/> · non conformes : <c:out value="${indicateurs.nonConformes}"/> · à regarder : <c:out value="${indicateurs.aRegarder}"/> · en retard (&gt; 5 jours ouvrables) : <c:out value="${indicateurs.enRetard}"/></div>
<form class="filtre" method="get" action="${ctx}/rh/recrutement/candidatures">
  <input type="text" name="q" value="<c:out value='${q}'/>" placeholder="Nom, e-mail ou numéro">
  <select name="offreId"><option value="">Toutes les offres</option><c:forEach items="${offres}" var="o"><option value="<c:out value='${o.idOffre}'/>" <c:if test="${offreId eq o.idOffre}">selected</c:if>><c:out value="${o.reference}"/> — <c:out value="${o.titre}"/></option></c:forEach></select>
  <select name="classement"><option value="">Tous les classements</option><c:forEach items="${classements}" var="x"><option value="<c:out value='${x}'/>" <c:if test="${classement eq x}">selected</c:if>><c:out value="${x}"/></option></c:forEach></select>
  <select name="statut"><option value="">Tous les statuts</option><c:forEach items="${statuts}" var="x"><option value="<c:out value='${x}'/>" <c:if test="${statut eq x}">selected</c:if>><c:out value="${x}"/></option></c:forEach></select>
  <select name="tri"><option value="dateReception">Date de réception</option><option value="numero">Numéro</option><option value="statut">Statut</option><option value="classementAuto">Classement</option></select>
  <select name="direction"><option value="DESC">Décroissant</option><option value="ASC">Croissant</option></select>
  <button class="btn" type="submit">Filtrer</button>
</form>
<c:choose><c:when test="${empty page.content}"><div class="carte">Aucune candidature trouvée.</div></c:when><c:otherwise>
<table><thead><tr><th>Numéro</th><th>Candidat</th><th>Offre</th><th>Classement</th><th>Statut</th><th>Reçue le</th><th></th></tr></thead><tbody>
<c:forEach items="${page.content}" var="c"><tr><td><c:out value="${c.numero}"/></td><td><c:out value="${c.candidat}"/><br><c:out value="${c.email}"/></td><td><c:out value="${c.offre}"/></td>
<td><c:out value="${c.classement}"/></td><td><c:out value="${c.statut}"/></td><td><c:out value="${c.dateReception}"/></td><td><a href="${ctx}/rh/recrutement/candidatures/${c.id}">Détail</a></td></tr></c:forEach>
</tbody></table>
<c:if test="${page.totalPages gt 1}"><nav class="pagination"><c:forEach begin="0" end="${page.totalPages - 1}" var="p"><c:url var="lien" value="/rh/recrutement/candidatures"><c:param name="q" value="${q}"/><c:param name="offreId" value="${offreId}"/><c:param name="classement" value="${classement}"/><c:param name="statut" value="${statut}"/><c:param name="tri" value="${tri}"/><c:param name="direction" value="${direction}"/><c:param name="page" value="${p}"/></c:url><a href="${lien}"><c:out value="${p + 1}"/></a></c:forEach></nav></c:if>
</c:otherwise></c:choose>
<jsp:include page="/WEB-INF/views/recrutement/fragments/pied.jsp"/>
