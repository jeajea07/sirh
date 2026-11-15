<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="fragments/entete.jsp"><jsp:param name="titre" value="Offres d'emploi"/><jsp:param name="actif" value="offres"/></jsp:include>

<section class="v-hero">
  <div class="v-wrap">
    <div class="v-kicker">Rejoignez-nous</div>
    <h1>Construisons ensemble la suite de TeamUp</h1>
    <p>Consultez nos postes ouverts et postulez en quelques minutes avec votre CV.</p>
    <form class="v-search" method="get" action="<c:url value='/offres'/>" role="search">
      <label class="hidden" for="q">Rechercher une offre</label>
      <input id="q" type="search" name="q" value="<c:out value='${q}'/>" placeholder="Intitulé, référence, mot-clé…">
      <button class="btn btn-primary" type="submit">Rechercher</button>
    </form>
  </div>
</section>

<main class="v-main">
  <div class="v-wrap">
    <div class="v-section-head">
      <h2>Postes ouverts</h2>
      <span class="v-count"><c:out value="${offres.totalElements}"/> offre(s)<c:if test="${not empty q}"> pour « <c:out value="${q}"/> »</c:if></span>
    </div>

    <c:choose>
      <c:when test="${empty offresPubliques}">
        <div class="card empty">
          <c:choose>
            <c:when test="${not empty q}">Aucune offre ne correspond à votre recherche. <a class="bold" href="<c:url value='/offres'/>">Voir toutes les offres</a></c:when>
            <c:otherwise>Aucune offre n'est ouverte actuellement. Vous pouvez déposer une candidature spontanée.</c:otherwise>
          </c:choose>
        </div>
      </c:when>
      <c:otherwise>
        <div class="v-offres">
          <c:forEach items="${offresPubliques}" var="offre">
            <article class="v-offre">
              <div class="v-tags">
                <c:if test="${not empty offre.typeContrat}"><span class="badge badge-dark"><c:out value="${offre.typeContrat}"/></span></c:if>
                <c:if test="${not empty offre.departement}"><span class="chip"><c:out value="${offre.departement}"/></span></c:if>
                <c:if test="${offre.nombrePostes gt 1}"><span class="chip"><c:out value="${offre.nombrePostes}"/> postes</span></c:if>
              </div>
              <h2><a href="<c:url value='/offres/${offre.id}'/>"><c:out value="${offre.titre}"/></a></h2>
              <c:if test="${not empty offre.description}"><p class="v-desc"><c:out value="${offre.description}"/></p></c:if>
              <div class="v-foot">
                <span class="muted">Réf. <c:out value="${offre.reference}"/>
                  <c:if test="${not empty offre.dateLimite}"> · Jusqu'au <fmt:formatNumber value="${offre.dateLimite.dayOfMonth}" minIntegerDigits="2"/>/<fmt:formatNumber value="${offre.dateLimite.monthValue}" minIntegerDigits="2"/>/<c:out value="${offre.dateLimite.year}"/></c:if>
                </span>
                <a class="btn btn-sm btn-primary" href="<c:url value='/offres/${offre.id}'/>">Voir l'offre</a>
              </div>
            </article>
          </c:forEach>
        </div>

        <c:if test="${offres.totalPages gt 1}">
          <nav class="v-pager" aria-label="Pagination">
            <c:forEach begin="0" end="${offres.totalPages - 1}" var="p">
              <c:url var="lien" value="/offres"><c:param name="q" value="${q}"/><c:param name="page" value="${p}"/></c:url>
              <a class="${p == offres.number ? 'actif' : ''}" href="${lien}"><c:out value="${p + 1}"/></a>
            </c:forEach>
          </nav>
        </c:if>
      </c:otherwise>
    </c:choose>

    <div class="v-spontanee">
      <div>
        <h3>Vous ne trouvez pas le poste idéal ?</h3>
        <p class="muted">Déposez une candidature spontanée : nous la rapprocherons des prochaines offres.</p>
      </div>
      <a class="btn btn-dark" href="<c:url value='/candidatures/nouvelle'/>">Candidature spontanée</a>
    </div>
  </div>
</main>

<jsp:include page="fragments/pied.jsp"/>
