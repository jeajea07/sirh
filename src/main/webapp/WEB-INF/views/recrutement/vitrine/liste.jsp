<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout titre="Offres d'emploi" actif="offres-public">
  <div class="page-head">
    <div>
      <h1>Offres d'emploi</h1>
      <p>Découvrez les postes ouverts et envoyez votre candidature en ligne.</p>
    </div>
  </div>

  <form class="toolbar" method="get" action="<c:url value='/offres'/>">
    <input type="search" name="q" value="<c:out value='${q}'/>" placeholder="Rechercher un poste" aria-label="Rechercher une offre">
    <button class="btn btn-primary" type="submit">Rechercher</button>
  </form>

  <c:choose>
    <c:when test="${empty offresPubliques}">
      <div class="card empty">
        <h2>Aucune offre ouverte actuellement</h2>
        <p class="muted" style="margin-top:6px">Vous pouvez tout de même déposer une candidature spontanée.</p>
        <p style="margin-top:14px"><a class="btn btn-primary" href="<c:url value='/candidatures/nouvelle'/>">Déposer une candidature spontanée</a></p>
      </div>
    </c:when>
    <c:otherwise>
      <div class="offer-list">
        <c:forEach items="${offresPubliques}" var="offre">
          <article class="offer">
            <div class="grow">
              <h2><c:out value="${offre.titre}"/></h2>
              <p class="muted"><c:out value="${offre.reference}"/> · Date limite : <c:out value="${offre.dateLimite}"/></p>
              <p style="margin-top:8px"><c:out value="${offre.description}"/></p>
            </div>
            <a class="btn btn-sm btn-primary" href="<c:url value='/offres/${offre.id}'/>">Consulter et postuler</a>
          </article>
        </c:forEach>
      </div>
      <c:if test="${offres.totalPages gt 1}">
        <nav class="pagination" aria-label="Pagination des offres" style="margin-top:18px">
          <c:forEach begin="0" end="${offres.totalPages - 1}" var="p">
            <c:url var="lien" value="/offres"><c:param name="q" value="${q}"/><c:param name="page" value="${p}"/></c:url>
            <a class="${p == offres.number ? 'actif' : ''}" href="${lien}"><c:out value="${p + 1}"/></a>
          </c:forEach>
        </nav>
      </c:if>
    </c:otherwise>
  </c:choose>

  <div class="card" style="margin-top:18px">
    <div class="row wrap between">
      <div><h2>Vous ne trouvez pas l'offre qui vous correspond ?</h2><p class="muted">Présentez votre profil à notre équipe de recrutement.</p></div>
      <a class="btn" href="<c:url value='/candidatures/nouvelle'/>">Candidature spontanée</a>
    </div>
  </div>
</t:layout>
