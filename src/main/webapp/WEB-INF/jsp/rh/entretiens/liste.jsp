<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Entretiens" actif="entretiens">
  <div class="page-head">
    <div>
      <h1>Entretiens</h1>
      <p>Trois niveaux dans l'ordre : RH, Technicien puis DG. Un candidat ne passe au niveau suivant qu'après confirmation de la RH.</p>
    </div>
  </div>
  <f:flash/>

  <div class="card card-flat">
    <div class="card-head"><h2>Candidats en phase d'entretien</h2><span class="badge badge-info">${lignes.size()} candidat(s)</span></div>
    <c:choose>
      <c:when test="${empty lignes}">
        <div class="empty">Aucun candidat en phase d'entretien. Les candidats admis au test technique apparaissent ici.</div>
      </c:when>
      <c:otherwise>
        <div class="table-wrap"><table>
          <thead><tr><th>Candidature</th><th>Candidat</th><th>Offre</th><th>Niveau</th><th>Date</th><th>Note</th><th>Étape</th><th></th></tr></thead>
          <tbody>
            <c:forEach var="l" items="${lignes}">
              <tr>
                <td><b><c:out value="${l.numero}"/></b></td>
                <td><c:out value="${l.candidat}"/></td>
                <td><c:out value="${l.offre}"/></td>
                <td><span class="chip"><c:out value="${l.niveauLibelle}"/></span></td>
                <td><c:out value="${empty l.entretien ? '—' : l.entretien.datePrevueFr}"/></td>
                <td><c:choose><c:when test="${not empty l.entretien.noteGenerale}"><b><c:out value="${l.entretien.noteGenerale}"/></b> / 5</c:when><c:otherwise>—</c:otherwise></c:choose></td>
                <td>
                  <c:choose>
                    <c:when test="${l.etape == 'A planifier'}"><span class="badge badge-warn">À planifier</span></c:when>
                    <c:when test="${l.etape == 'A confirmer'}"><span class="badge badge-info">À confirmer — <c:out value="${l.entretien.resultatPropose == 'ADMIS' ? 'admis proposé' : 'non admis proposé'}"/></span></c:when>
                    <c:otherwise><span class="badge"><c:out value="${l.entretien.statutLibelle}"/></span></c:otherwise>
                  </c:choose>
                </td>
                <td class="right"><a class="btn btn-sm btn-primary" href="<c:url value='/rh/entretiens/candidature/${l.idCandidature}'/>">Ouvrir</a></td>
              </tr>
            </c:forEach>
          </tbody>
        </table></div>
      </c:otherwise>
    </c:choose>
  </div>
</t:layout>
