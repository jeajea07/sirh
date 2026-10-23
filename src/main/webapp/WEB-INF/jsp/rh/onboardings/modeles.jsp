<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Modèles d'onboarding" actif="modeles-onboarding">
  <div class="page-head"><div><h1>Modèles d'onboarding</h1><p>Bibliothèque versionnée des parcours d'intégration.</p></div><a class="btn btn-primary" href="<c:url value='/rh/onboardings/modeles/nouveau'/>">Nouveau modèle</a></div>
  <f:flash/>
  <c:choose>
    <c:when test="${empty modeles}"><div class="card empty-state"><b>Aucun modèle</b>Créez un premier parcours type.</div></c:when>
    <c:otherwise>
      <div class="card card-flat"><div class="table-wrap"><table><thead><tr><th>Modèle</th><th>Version</th><th>Périmètre</th><th>Tâches</th><th>Durée</th><th>Statut</th><th>Dernière modification</th><th></th></tr></thead><tbody>
        <c:forEach var="m" items="${modeles}"><tr><td><b><c:out value="${m.nom}"/></b><c:if test="${not empty m.description}"><br><span class="muted small"><c:out value="${m.description}"/></span></c:if></td><td><c:out value="${m.versionLibelle}"/></td><td><c:out value="${m.poste}"/> / <c:out value="${m.departement}"/></td><td><c:out value="${m.nombreTaches}"/></td><td><c:out value="${m.dureeIndicativeJours}"/> j</td><td><span class="badge <c:out value='${m.actifBadge}'/>"><c:out value="${m.actifLibelle}"/></span></td><td><c:out value="${m.dateMajFr}"/></td><td class="right"><div class="actions"><a class="btn btn-sm" href="<c:url value='/rh/onboardings/modeles/${m.id}'/>">Modifier</a><c:if test="${not m.actif}"><form method="post" action="<c:url value='/rh/onboardings/modeles/${m.id}/activer'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-sm btn-ok" type="submit">Activer</button></form></c:if><form method="post" action="<c:url value='/rh/onboardings/modeles/${m.id}/dupliquer'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-sm" type="submit">Dupliquer</button></form></div></td></tr></c:forEach>
      </tbody></table></div></div>
    </c:otherwise>
  </c:choose>
</t:layout>
