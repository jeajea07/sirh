<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="fragments/entete.jsp"><jsp:param name="titre" value="Accueil"/><jsp:param name="actif" value="accueil"/></jsp:include>

<section class="v-hero v-hero-home">
  <div class="v-wrap">
    <div class="v-kicker">TeamUp — SIRH</div>
    <h1>Bienvenue sur l'espace RH de TeamUp</h1>
    <p>Collaborateurs : accédez à votre espace de travail. Candidats : découvrez nos postes ouverts et postulez en ligne.</p>
  </div>
</section>

<main class="v-main">
  <div class="v-wrap">
    <div class="v-portes">
      <a class="v-porte" href="<c:url value='${connecte ? "/accueil" : "/connexion"}'/>">
        <span class="v-porte-icone" aria-hidden="true">
          <svg viewBox="0 0 24 24"><path d="M15 3h4a2 2 0 012 2v14a2 2 0 01-2 2h-4M10 17l5-5-5-5M15 12H3"/></svg>
        </span>
        <span class="v-porte-texte">
          <strong><c:out value="${connecte ? 'Accéder à mon espace' : 'Se connecter'}"/></strong>
          <span>Employés, RH, Finance, Direction générale et candidats ayant reçu leurs accès.</span>
        </span>
        <span class="v-porte-fleche" aria-hidden="true">→</span>
      </a>
      <a class="v-porte v-porte-offres" href="<c:url value='/offres'/>">
        <span class="v-porte-icone" aria-hidden="true">
          <svg viewBox="0 0 24 24"><rect x="3" y="7" width="18" height="13" rx="2"/><path d="M9 7V5a2 2 0 012-2h2a2 2 0 012 2v2M3 12h18"/></svg>
        </span>
        <span class="v-porte-texte">
          <strong>Voir les offres d'emploi</strong>
          <span><c:out value="${nombreOffres}"/> poste(s) ouvert(s) — candidature en ligne avec votre CV, sans créer de compte.</span>
        </span>
        <span class="v-porte-fleche" aria-hidden="true">→</span>
      </a>
    </div>

    <c:if test="${not empty dernieresOffres}">
      <div class="v-section-head" style="margin-top:36px">
        <h2>Dernières offres publiées</h2>
        <a class="bold small" href="<c:url value='/offres'/>">Toutes les offres →</a>
      </div>
      <div class="v-offres v-offres-3">
        <c:forEach items="${dernieresOffres}" var="offre">
          <article class="v-offre">
            <h2><a href="<c:url value='/offres/${offre.idOffre}'/>"><c:out value="${offre.titre}"/></a></h2>
            <c:if test="${not empty offre.description}"><p class="v-desc"><c:out value="${offre.description}"/></p></c:if>
            <div class="v-foot">
              <span class="muted">
                <c:choose>
                  <c:when test="${not empty offre.dateLimite}">Jusqu'au <fmt:formatNumber value="${offre.dateLimite.dayOfMonth}" minIntegerDigits="2"/>/<fmt:formatNumber value="${offre.dateLimite.monthValue}" minIntegerDigits="2"/>/<c:out value="${offre.dateLimite.year}"/></c:when>
                  <c:otherwise>Réf. <c:out value="${offre.reference}"/></c:otherwise>
                </c:choose>
              </span>
              <a class="btn btn-sm btn-primary" href="<c:url value='/offres/${offre.idOffre}'/>">Voir</a>
            </div>
          </article>
        </c:forEach>
      </div>
    </c:if>
  </div>
</main>

<jsp:include page="fragments/pied.jsp"/>
