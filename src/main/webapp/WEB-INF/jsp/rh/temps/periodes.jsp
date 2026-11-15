<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Clôture et paie" actif="temps-rh">
  <c:set var="onglet" value="periodes"/>
  <div class="page-head"><div><h1>Clôture et variables de paie</h1><p>La clôture verrouille le mois (pointages, journées, plannings), génère les variables de paie des Blocs 6 et 7 et acquiert les congés payés du mois.</p></div></div>
  <f:flash/>
  <div class="tabs"><a class="tab ${onglet == 'anomalies' ? 'active' : ''}" href="<c:url value='/rh/temps'/>">Anomalies</a><a class="tab ${onglet == 'releve' ? 'active' : ''}" href="<c:url value='/rh/temps/releve'/>">Relevés et pointages</a><a class="tab" href="<c:url value='/planning'/>">Plannings</a><a class="tab ${onglet == 'periodes' ? 'active' : ''}" href="<c:url value='/rh/temps/periodes'/>">Clôture et paie</a><a class="tab ${onglet == 'parametres' ? 'active' : ''}" href="<c:url value='/rh/temps/parametres'/>">Paramétrage</a></div>
  <div class="card" style="margin-bottom:16px"><div class="card-head"><h2>Clôturer un mois</h2>
    <form method="get" action="<c:url value='/rh/temps/periodes'/>" style="display:flex;gap:6px"><input type="month" name="mois" value="${mois}"><button class="btn btn-sm" type="submit">Contrôler</button></form></div>
    <c:choose>
      <c:when test="${not empty blocages}"><div class="alert alert-warn"><b><c:out value="${mois}"/> ne peut pas être clôturé :</b><c:forEach var="b" items="${blocages}"><div><c:out value="${b}"/></div></c:forEach></div></c:when>
      <c:otherwise><form method="post" action="<c:url value='/rh/temps/periodes/cloturer'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="hidden" name="mois" value="${mois}"><p class="small">Aucun blocage pour <b><c:out value="${mois}"/></b>. La clôture est définitive : une correction ultérieure passera en régularisation sur le mois suivant (RG-VP04).</p><button class="btn btn-primary" type="submit">Clôturer <c:out value="${mois}"/></button></form></c:otherwise>
    </c:choose>
  </div>
  <div class="card" style="margin-bottom:16px"><div class="card-head"><h2>Périodes</h2></div>
    <c:choose><c:when test="${empty periodes}"><p class="muted">Aucune période clôturée.</p></c:when><c:otherwise>
      <div class="table-wrap"><table><thead><tr><th>Mois</th><th>Statut</th><th>Clôturée par</th><th></th></tr></thead><tbody>
        <c:forEach var="p" items="${periodes}"><tr><td><b><c:out value="${p.libelle}"/></b></td><td><span class="badge ${p.cloturee ? 'badge-ok' : 'badge-warn'}"><c:out value="${p.statut}"/></span></td><td><c:out value="${empty p.cloturePar ? '—' : p.cloturePar.nomComplet}"/></td>
          <td class="right"><a class="btn btn-sm" href="<c:url value='/rh/temps/periodes'><c:param name='mois' value='${mois}'/><c:param name='voir' value='${p.id}'/></c:url>">Variables</a> <a class="btn btn-sm" href="<c:url value='/rh/temps/periodes/${p.id}/export.csv'/>">Export CSV</a></td></tr></c:forEach>
      </tbody></table></div>
    </c:otherwise></c:choose>
  </div>
  <c:if test="${not empty voir}">
    <div class="card"><div class="card-head"><h2>Variables de paie</h2></div>
      <c:choose><c:when test="${empty variables}"><p class="muted">Aucune variable.</p></c:when><c:otherwise>
        <div class="table-wrap"><table><thead><tr><th>Salarié</th><th>Code</th><th>Libellé</th><th>Quantité</th><th>Source</th></tr></thead><tbody>
          <c:forEach var="v" items="${variables}"><tr><td><c:out value="${v.employe.nomComplet}"/></td><td><code><c:out value="${v.code}"/></code></td><td><c:out value="${v.libelle}"/></td><td><b><c:out value="${v.quantite}"/></b> <c:out value="${v.unite == 'HEURE' ? 'h' : 'j'}"/></td><td><c:out value="${v.source == 'BLOC6' ? 'Temps' : 'Congés'}"/></td></tr></c:forEach>
        </tbody></table></div>
      </c:otherwise></c:choose>
    </div>
  </c:if>
</t:layout>
