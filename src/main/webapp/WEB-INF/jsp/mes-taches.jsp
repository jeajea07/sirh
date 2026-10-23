<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Mes tâches" actif="mes-taches">
  <div class="page-head"><div><h1>Mes tâches</h1><p>Tâches d'intégration qui vous sont assignées.</p></div><c:if test="${not empty retard}"><span class="badge badge-bad">${retard.size()} en retard</span></c:if></div>
  <f:flash/>
  <c:choose>
    <c:when test="${empty taches}"><div class="card empty-state"><b>Aucune tâche assignée</b>Vous n'avez pas de tâche d'onboarding en cours.</div></c:when>
    <c:otherwise>
      <div class="card">
        <div class="task-list">
          <c:forEach var="t" items="${taches}">
            <div class="task ${t.enRetard ? 'late' : ''} ${t.bloquee ? 'blocked' : ''}">
              <div class="task-main">
                <div class="task-title"><c:out value="${t.titre}"/> <span class="badge <c:out value='${t.statutBadge}'/>"><c:out value="${t.statutLibelle}"/></span><c:if test="${t.obligatoire}"><span class="chip">Obligatoire</span></c:if></div>
                <div class="task-meta"><span><c:out value="${t.employeNom}"/></span><span>Parcours : <b><c:out value="${t.onboardingReference}"/></b></span><span>Échéance : <b class="${t.enRetard ? 'late' : ''}"><c:out value="${t.dateEcheanceFr}"/></b></span><c:if test="${not empty t.dependanceTitre}"><span>Après : <b><c:out value="${t.dependanceTitre}"/></b></span></c:if></div>
                <c:if test="${not empty t.description}"><div class="task-desc"><c:out value="${t.description}"/></div></c:if>
                <c:if test="${not empty t.motifBlocage}"><div class="task-desc late">Blocage : <c:out value="${t.motifBlocage}"/></div></c:if>
              </div>
              <div class="task-actions">
                <c:if test="${not t.terminee and not t.annulee}">
                  <c:if test="${t.statut == 'A_FAIRE' and t.dependancePrete}"><form method="post" action="<c:url value='/mes-taches'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="hidden" name="action" value="demarrer"><input type="hidden" name="idTache" value="${t.id}"><button class="btn btn-sm" type="submit">Démarrer</button></form></c:if>
                  <c:if test="${t.statut == 'BLOQUEE'}"><form method="post" action="<c:url value='/mes-taches'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="hidden" name="action" value="debloquer"><input type="hidden" name="idTache" value="${t.id}"><button class="btn btn-sm btn-ok" type="submit">Débloquer</button></form></c:if>
                  <c:if test="${t.statut != 'BLOQUEE' and t.dependancePrete}"><form method="post" action="<c:url value='/mes-taches'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="hidden" name="action" value="terminer"><input type="hidden" name="idTache" value="${t.id}"><button class="btn btn-sm btn-ok" type="submit">Terminer</button></form></c:if>
                  <c:if test="${t.statut != 'BLOQUEE'}"><details><summary class="btn btn-sm">Bloquer</summary><form method="post" action="<c:url value='/mes-taches'/>" class="stack" style="margin-top:8px;min-width:220px"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="hidden" name="action" value="bloquer"><input type="hidden" name="idTache" value="${t.id}"><input type="text" name="commentaire" placeholder="Motif du blocage" required><button class="btn btn-sm btn-danger btn-block" type="submit">Confirmer le blocage</button></form></details></c:if>
                </c:if>
                <a class="btn btn-sm" href="<c:url value='/rh/onboardings/${t.idOnboarding}'/>">Voir</a>
              </div>
            </div>
          </c:forEach>
        </div>
      </div>
    </c:otherwise>
  </c:choose>
</t:layout>
