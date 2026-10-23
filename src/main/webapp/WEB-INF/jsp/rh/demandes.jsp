<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout titre="Demandes à vérifier" actif="demandes-rh">

  <div class="page-head">
    <div><h1>Demandes à vérifier</h1><p>Les demandes de besoin en personnel qui attendent la vérification de la RH.</p></div>
  </div>

  <c:if test="${not empty succes}"><div class="alert alert-ok" style="margin-bottom:16px"><c:out value="${succes}"/></div></c:if>
  <c:if test="${not empty erreur}"><div class="alert alert-bad" style="margin-bottom:16px"><c:out value="${erreur}"/></div></c:if>

  <c:choose>
    <c:when test="${empty demandes}">
      <div class="card empty">Aucune demande n'attend votre vérification pour le moment.</div>
    </c:when>
    <c:otherwise>
      <div class="card card-flat">
        <div class="table-wrap"><table>
          <thead><tr><th>N°</th><th>Poste</th><th>Département</th><th>Demandeur</th><th>Contrat</th><th>Salaire / grille</th><th>Reçue le</th><th></th></tr></thead>
          <tbody>
            <c:forEach var="d" items="${demandes}">
              <tr>
                <td><b><c:out value="${d.numero}"/></b></td>
                <td><c:out value="${d.poste}"/></td>
                <td><c:out value="${d.departement}"/></td>
                <td><c:out value="${d.demandeur}"/></td>
                <td><c:out value="${d.contrat}"/></td>
                <td>
                  <c:out value="${d.controleSalaire.salaireFr}"/><br>
                  <span class="badge ${d.controleSalaire.badge}"><c:out value="${d.controleSalaire.libelle}"/></span>
                </td>
                <td><c:out value="${d.dateReceptionFr}"/></td>
                <td class="right"><a class="btn btn-sm btn-primary" href="<c:url value='/rh/demandes/${d.id}'/>">Vérifier</a></td>
              </tr>
            </c:forEach>
          </tbody>
        </table></div>
      </div>
    </c:otherwise>
  </c:choose>

</t:layout>
