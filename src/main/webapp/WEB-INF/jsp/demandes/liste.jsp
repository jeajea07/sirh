<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout titre="Mes demandes" actif="demandes">

  <div class="page-head">
    <div><h1>Mes demandes</h1><p>Vos demandes de besoin en personnel et leur avancement.</p></div>
    <a class="btn btn-primary" href="<c:url value='/demandes/nouvelle'/>">Nouvelle demande</a>
  </div>

  <c:if test="${not empty succes}"><div class="alert alert-ok" style="margin-bottom:16px"><c:out value="${succes}"/></div></c:if>
  <c:if test="${not empty erreur}"><div class="alert alert-bad" style="margin-bottom:16px"><c:out value="${erreur}"/></div></c:if>

  <c:choose>
    <c:when test="${empty demandes}">
      <div class="card empty">
        Vous n'avez encore fait aucune demande.<br><br>
        <a class="btn btn-primary" href="<c:url value='/demandes/nouvelle'/>">Faire ma première demande</a>
      </div>
    </c:when>
    <c:otherwise>
      <div class="card card-flat">
        <div class="table-wrap"><table>
          <thead><tr><th>N°</th><th>Poste</th><th>Motif</th><th>Créée le</th><th>Date souhaitée</th><th>Statut</th><th></th></tr></thead>
          <tbody>
            <c:forEach var="d" items="${demandes}">
              <tr>
                <td><b><c:out value="${d.numero}"/></b></td>
                <td><c:choose><c:when test="${empty d.posteDesignation}"><span class="muted">À préciser</span></c:when><c:otherwise><c:out value="${d.posteDesignation}"/></c:otherwise></c:choose></td>
                <td><c:out value="${d.motifLibelle}"/></td>
                <td><c:out value="${d.dateCreationFr}"/></td>
                <td><c:out value="${d.dateSouhaiteeFr}"/></td>
                <td><span class="badge ${d.statutBadge}"><c:out value="${d.statutLibelle}"/></span></td>
                <td class="right"><a class="btn btn-sm" href="<c:url value='/demandes/${d.id}'/>">Suivre</a></td>
              </tr>
            </c:forEach>
          </tbody>
        </table></div>
      </div>
    </c:otherwise>
  </c:choose>

</t:layout>
