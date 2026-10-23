<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout titre="Mon espace candidat" actif="candidat">
  <div class="page-head">
    <div>
      <h1>Mon espace candidat</h1>
      <p>Bonjour <strong><c:out value="${candidat.prenom}"/> <c:out value="${candidat.nom}"/></strong></p>
    </div>
    <div class="badge badge-info">Candidat</div>
  </div>

  <div class="card stack">
    <h2>Mes candidatures</h2>

    <c:if test="${empty candidatures}">
      <div class="alert alert-info">Aucune candidature enregistrée pour le moment.</div>
    </c:if>

    <c:if test="${not empty candidatures}">
      <div class="stack">
        <c:forEach var="candidature" items="${candidatures}">
          <div class="card" style="background:#fff; border-color: var(--line-strong);">
            <div class="question">
              <div>
                <strong>ID candidature</strong> : <c:out value="${candidature.idCandidature}"/>
                <p class="muted" style="margin-top:8px;">
                  Statut : <strong><c:out value="${candidature.statut}"/></strong>
                </p>
              </div>
              <div class="badge ${(candidature.statut == 'ADMIS' || candidature.statut == 'DOSSIER_VALIDE') ? 'badge-ok' : 'badge-warn'}">
                <c:choose>
                  <c:when test="${candidature.statut == 'ADMIS'}">Admis</c:when>
                  <c:when test="${candidature.statut == 'DOSSIER_VALIDE'}">Dossier validé</c:when>
                  <c:when test="${candidature.statut == 'EN_ATTENTE'}">En attente</c:when>
                  <c:otherwise>À suivre</c:otherwise>
                </c:choose>
              </div>
            </div>

            <div class="row wrap" style="margin-top: 12px;">
              <c:if test="${candidature.testTechnique != null}">
                <a class="btn btn-primary" href="<c:url value='/candidat/test/${candidature.idCandidature}'/>">Accéder au test technique</a>
              </c:if>
              <c:if test="${candidature.testTechnique == null}">
                <span class="badge badge-warn">Le test technique n’est pas encore disponible.</span>
              </c:if>
            </div>
          </div>
        </c:forEach>
      </div>
    </c:if>
  </div>
</t:layout>
