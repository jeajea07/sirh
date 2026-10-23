<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout titre="Résultats test technique" actif="tests-rh">
  <div class="page-head">
    <div>
      <h1>Résultats du test technique</h1>
      <p>Seuil appliqué : <strong>${noteSeuil}</strong></p>
    </div>
    <a class="btn btn-primary" href="<c:url value='/accueil'/>">Retour accueil</a>
  </div>

  <div class="card">
    <div class="card-head">
      <h2>Candidats</h2>
      <span class="badge badge-info">${resultats.size()} résultat(s)</span>
    </div>

    <c:if test="${empty resultats}">
      <div class="alert alert-info">Aucun test technique n’a encore été enregistré dans le système.</div>
    </c:if>

    <c:if test="${not empty resultats}">
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Candidat</th>
              <th>Note</th>
              <th>Décision</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="resultat" items="${resultats}">
              <tr>
                <td><strong><c:out value="${resultat.candidat}" /></strong></td>
                <td><c:out value="${resultat.note}" /></td>
                <td>
                  <c:choose>
                    <c:when test="${resultat.admis}">
                      <span class="badge badge-ok">Admis</span>
                    </c:when>
                    <c:otherwise>
                      <span class="badge badge-bad">Refusé</span>
                    </c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </c:if>
  </div>
</t:layout>
