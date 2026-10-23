<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/views/recrutement/fragments/entete.jsp"/>
<div class="barre">
  <h1><c:out value="${titrePage}"/></h1>
  <a class="btn sec" href="${pageContext.request.contextPath}/recrutement/offres">Retour</a>
</div>
<div class="carte">
  <p>La demande est vérifiée automatiquement. L'offre sera créée en brouillon ; complétez ensuite ses champs avant validation.</p>
  <c:choose>
    <c:when test="${empty demandesAcceptees}">
      <p>Aucune demande acceptée sans offre active n'est disponible.</p>
    </c:when>
    <c:otherwise>
      <form method="post" action="${pageContext.request.contextPath}/recrutement/offres/nouveau">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <label for="idDemandeBesoin">Demande acceptée</label>
        <select id="idDemandeBesoin" name="idDemandeBesoin" required>
          <c:forEach items="${demandesAcceptees}" var="demande">
            <option value="<c:out value='${demande.id}'/>">
              <c:out value="${demande.numero}"/> — <c:out value="${demande.poste}"/> — <c:out value="${demande.departement}"/>
            </option>
          </c:forEach>
        </select>
        <p><button class="btn" type="submit">Créer le brouillon</button></p>
      </form>
    </c:otherwise>
  </c:choose>
</div>
<jsp:include page="/WEB-INF/views/recrutement/fragments/pied.jsp"/>
