<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Temps de travail" actif="temps-rh">
  <c:set var="onglet" value="anomalies"/>
  <div class="page-head"><div><h1>Temps de travail</h1><p>Pointage en service depuis le <c:out value="${miseEnService}"/>. Calcul des journées chaque nuit pour la veille.</p></div></div>
  <f:flash/>
  <div class="tabs"><a class="tab ${onglet == 'anomalies' ? 'active' : ''}" href="<c:url value='/rh/temps'/>">Anomalies</a><a class="tab ${onglet == 'releve' ? 'active' : ''}" href="<c:url value='/rh/temps/releve'/>">Relevés et pointages</a><a class="tab" href="<c:url value='/planning'/>">Plannings</a><a class="tab ${onglet == 'periodes' ? 'active' : ''}" href="<c:url value='/rh/temps/periodes'/>">Clôture et paie</a><a class="tab ${onglet == 'parametres' ? 'active' : ''}" href="<c:url value='/rh/temps/parametres'/>">Paramétrage</a></div>
  <c:if test="${not empty plannings}"><div class="alert alert-warn" style="margin-bottom:16px"><c:out value="${plannings.size()}"/> planning(s) à valider — <a href="<c:url value='/planning'/>">voir</a></div></c:if>
  <div class="card"><div class="card-head"><h2>Anomalies à traiter (<c:out value="${anomalies.size()}"/>)</h2></div>
    <c:choose><c:when test="${empty anomalies}"><p class="muted">Aucune anomalie ouverte.</p></c:when><c:otherwise>
      <div class="table-wrap"><table><thead><tr><th>Salarié</th><th>Jour</th><th>Anomalie</th><th>Justification</th><th>Statut</th><th>Décision RH</th></tr></thead><tbody>
        <c:forEach var="a" items="${anomalies}"><tr>
          <td><a href="<c:url value='/rh/temps/releve'><c:param name='idEmploye' value='${a.employe.id}'/><c:param name='mois' value='${a.jour.toString().substring(0,7)}'/></c:url>"><c:out value="${a.employe.nomComplet}"/></a></td>
          <td><c:out value="${a.jourFr}"/></td>
          <td><b><c:out value="${a.typeLibelle}"/></b><div class="small muted"><c:out value="${a.detail}"/></div></td>
          <td class="small"><c:out value="${a.justification}"/><c:if test="${not empty a.heureProposee}"><br>proposé : <c:out value="${a.sensPropose}"/> <c:out value="${a.heureProposeeFr}"/></c:if></td>
          <td><span class="badge <c:out value='${a.statutBadge}'/>"><c:out value="${a.statutLibelle}"/></span><c:if test="${a.escaladee}"> <span class="badge badge-bad">escaladée</span></c:if></td>
          <td><form method="post" action="<c:url value='/rh/temps/anomalies/${a.id}/accepter'/>" style="display:flex;gap:4px;flex-wrap:wrap"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="text" name="commentaire" placeholder="Commentaire / motif" style="min-width:140px">
            <button class="btn btn-sm btn-ok" type="submit">Accepter</button>
            <button class="btn btn-sm btn-danger" type="submit" formaction="<c:url value='/rh/temps/anomalies/${a.id}/refuser'/>">Confirmer</button>
            <button class="btn btn-sm" type="submit" formaction="<c:url value='/rh/temps/anomalies/${a.id}/classer'/>">Classer</button></form></td>
        </tr></c:forEach>
      </tbody></table></div>
      <p class="small muted">Accepter : la justification est admise (pointage correctif créé si une heure est proposée). Confirmer : retard ou absence retenus (absence injustifiée transmise au Bloc 7). Classer : sans suite, motif obligatoire.</p>
    </c:otherwise></c:choose>
  </div>
</t:layout>
