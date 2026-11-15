<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Planning" actif="planning">
  <div class="page-head">
    <div><h1><c:out value="${planning.reference}"/> <span class="badge <c:out value='${planning.statutBadge}'/>"><c:out value="${planning.statutLibelle}"/></span></h1>
      <p><c:out value="${planning.departement.designation}"/> · <c:out value="${planning.periodeLibelle}"/> · version <c:out value="${planning.version}"/> · préparé par <c:out value="${planning.auteur.nomComplet}"/></p></div>
    <div class="page-actions"><a class="btn" href="<c:url value='/planning'/>">Retour</a>
      <c:if test="${preparateur and planning.statut == 'PUBLIE'}"><form method="post" action="<c:url value='/planning/${planning.id}/nouvelle-version'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn" type="submit">Modifier (nouvelle version)</button></form></c:if>
    </div>
  </div>
  <f:flash/>
  <c:if test="${not empty planning.motifRejet}"><div class="alert alert-bad" style="margin-bottom:16px">Rejeté : <c:out value="${planning.motifRejet}"/></div></c:if>
  <c:if test="${editable and not empty controles}"><div class="alert alert-warn" style="margin-bottom:16px"><b>À corriger avant soumission :</b><c:forEach var="m" items="${controles}"><div><c:out value="${m}"/></div></c:forEach></div></c:if>

  <div class="stat-grid" style="margin-bottom:16px">
    <div class="stat-card"><span class="stat-label">Heures majorées estimées</span><span class="stat-value" style="font-size:1.2rem"><c:out value="${planning.coutFr}"/></span><span class="stat-note"><c:out value="${planning.impactBudgetaire ? 'impact budgétaire : validation Finance' : 'sans impact budgétaire'}"/></span></div>
    <div class="stat-card"><span class="stat-label">Budget restant (année)</span><span class="stat-value" style="font-size:1.2rem"><c:out value="${budgetRestant}"/></span><span class="stat-note">département, après plannings publiés</span></div>
    <div class="stat-card"><span class="stat-label">Circuit</span><span class="stat-note">RH : <c:out value="${empty planning.valideurRh ? '—' : planning.valideurRh.nomComplet}"/></span><span class="stat-note">Finance : <c:out value="${empty planning.valideurFinance ? '—' : planning.valideurFinance.nomComplet}"/></span><span class="stat-note">DG : <c:out value="${empty planning.decideurDg ? '—' : planning.decideurDg.nomComplet}"/></span></div>
  </div>

  <form method="post" action="<c:url value='/planning/${planning.id}/grille'/>" class="card card-flat">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
    <div class="table-wrap"><table style="font-size:.8rem"><thead><tr><th>Salarié</th>
      <c:forEach var="j" items="${jours}"><th class="${j.dayOfWeek.value == 7 ? 'late' : ''}"><c:out value="${j.dayOfMonth}"/>/<c:out value="${j.monthValue}"/></th></c:forEach><th>Total</th></tr></thead><tbody>
      <c:forEach var="r" items="${rangees}"><tr><td><b><c:out value="${r.employe.nomComplet}"/></b></td>
        <c:forEach var="cs" items="${r.cases}"><td style="${cs.ligne.majore ? 'background:var(--warn-bg)' : ''}">
          <c:choose>
            <c:when test="${editable and not cs.verrouillee}">
              <select name="c_${cs.ligne.id}" style="min-width:96px;font-size:.75rem;padding:2px">
                <option value="REPOS" ${cs.valeur == 'REPOS' ? 'selected' : ''}>Repos</option>
                <option value="FERIE" ${cs.valeur == 'FERIE' ? 'selected' : ''}>Férié</option>
                <c:forEach var="m" items="${modeles}"><option value="${m.id}" ${cs.valeur == m.id.toString() ? 'selected' : ''}><c:out value="${m.code}"/></option></c:forEach>
              </select>
            </c:when>
            <c:otherwise><span class="${cs.verrouillee ? 'badge badge-info' : ''}"><c:out value="${cs.ligne.typeJour == 'TRAVAIL' ? cs.ligne.modele.code : cs.ligne.typeJour}"/></span></c:otherwise>
          </c:choose>
          <c:if test="${cs.ligne.minutesSupPrevues > 0}"><div class="small late">+<c:out value="${cs.ligne.minutesSupPrevues / 60}"/> h sup</div></c:if>
        </td></c:forEach><td><c:out value="${r.totalLibelle}"/></td></tr></c:forEach>
    </tbody></table></div>
    <p class="small muted" style="padding:0 12px">Cases jaunes : heures majorées (heures sup., nuit, dimanche, férié). Les congés et absences validés sont verrouillés (RG-PL04).
      <c:forEach var="m" items="${modeles}"> · <b><c:out value="${m.code}"/></b> <c:out value="${m.plageLibelle}"/></c:forEach></p>
    <c:if test="${editable}"><div class="form-pied"><button class="btn" type="submit">Enregistrer</button><button class="btn btn-primary" type="submit" name="soumettre" value="1">Enregistrer et soumettre au RH</button></div></c:if>
  </form>

  <c:if test="${peutValider}">
    <div class="card" style="margin-top:16px"><div class="card-head"><h2>Décision</h2></div>
      <div class="form-pied" style="justify-content:flex-start;gap:8px">
        <form method="post" action="<c:url value='/planning/${planning.id}/valider'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-ok" type="submit">Valider</button></form>
        <form method="post" action="<c:url value='/planning/${planning.id}/rejeter'/>" style="display:flex;gap:6px;flex:1"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="text" name="motif" placeholder="Motif du rejet (obligatoire)" required style="flex:1"><button class="btn btn-danger" type="submit">Rejeter</button></form>
      </div>
    </div>
  </c:if>
</t:layout>
