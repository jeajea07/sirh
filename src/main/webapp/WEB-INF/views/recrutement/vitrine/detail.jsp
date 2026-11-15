<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="fragments/entete.jsp"><jsp:param name="titre" value="${offre.titre}"/><jsp:param name="actif" value="offres"/></jsp:include>

<section class="v-hero">
  <div class="v-wrap">
    <a class="v-back" href="<c:url value='/offres'/>">← Toutes les offres</a>
    <h1><c:out value="${offre.titre}"/></h1>
    <div class="v-meta">
      <c:if test="${not empty offre.typeContrat}"><span class="chip"><c:out value="${offre.typeContrat}"/><c:if test="${not empty offre.dureeMois and offre.dureeMois gt 0}"> · <c:out value="${offre.dureeMois}"/> mois</c:if></span></c:if>
      <c:if test="${not empty offre.departement}"><span class="chip"><c:out value="${offre.departement}"/></span></c:if>
      <span class="chip">Réf. <c:out value="${offre.reference}"/></span>
    </div>
  </div>
</section>

<main class="v-main">
  <div class="v-wrap v-detail">
    <div>
      <section class="card">
        <h2>Le poste</h2>
        <c:choose>
          <c:when test="${not empty offre.description}"><p class="v-texte"><c:out value="${offre.description}"/></p></c:when>
          <c:otherwise><p class="muted">Description à venir.</p></c:otherwise>
        </c:choose>
      </section>

      <c:if test="${not empty offre.missions}">
        <section class="card">
          <h2>Vos missions</h2>
          <p class="v-texte"><c:out value="${offre.missions}"/></p>
        </section>
      </c:if>

      <section class="card">
        <h2>Profil recherché</h2>
        <c:choose>
          <c:when test="${empty offre.criteres}"><p class="muted">Consultez le détail du poste dans la description.</p></c:when>
          <c:otherwise>
            <ul class="v-criteres">
              <c:forEach items="${offre.criteres}" var="critere">
                <li>
                  <c:choose>
                    <c:when test="${critere.obligatoire}"><span class="badge badge-warn">Requis</span></c:when>
                    <c:otherwise><span class="badge badge-info">Apprécié</span></c:otherwise>
                  </c:choose>
                  <span>
                    <c:choose>
                      <c:when test="${critere.nature == 'EXPERIENCE'}">Expérience : <strong><c:out value="${critere.valeurMin}"/> an(s) minimum</strong></c:when>
                      <c:when test="${critere.nature == 'NIVEAU_ETUDE'}">Niveau d'étude : <strong><c:out value="${critere.libelle}"/></strong><c:if test="${critere.obligatoire}"> minimum</c:if></c:when>
                      <c:when test="${critere.nature == 'LANGUE'}">Langue : <strong><c:out value="${critere.libelle}"/></strong></c:when>
                      <c:otherwise>Compétence : <strong><c:out value="${critere.libelle}"/></strong></c:otherwise>
                    </c:choose>
                  </span>
                </li>
              </c:forEach>
            </ul>
          </c:otherwise>
        </c:choose>
      </section>
    </div>

    <aside class="card v-apply">
      <c:if test="${not empty offre.salaire}">
        <span class="muted small">Salaire mensuel proposé</span>
        <div class="v-salaire"><c:out value="${salaireFr}"/></div>
      </c:if>
      <dl class="kv">
        <c:if test="${not empty offre.typeContrat}"><dt>Contrat</dt><dd><c:out value="${offre.typeContrat}"/></dd></c:if>
        <c:if test="${not empty offre.nombrePostes}"><dt>Postes</dt><dd><c:out value="${offre.nombrePostes}"/></dd></c:if>
        <c:if test="${not empty offre.datePublication}"><dt>Publiée le</dt><dd><fmt:formatNumber value="${offre.datePublication.dayOfMonth}" minIntegerDigits="2"/>/<fmt:formatNumber value="${offre.datePublication.monthValue}" minIntegerDigits="2"/>/<c:out value="${offre.datePublication.year}"/></dd></c:if>
        <c:if test="${not empty offre.dateLimite}"><dt>Date limite</dt><dd><strong><fmt:formatNumber value="${offre.dateLimite.dayOfMonth}" minIntegerDigits="2"/>/<fmt:formatNumber value="${offre.dateLimite.monthValue}" minIntegerDigits="2"/>/<c:out value="${offre.dateLimite.year}"/></strong></dd></c:if>
      </dl>
      <a class="btn btn-primary btn-block" href="<c:url value='/candidatures/nouvelle'><c:param name='offreId' value='${offre.id}'/></c:url>">Postuler à cette offre</a>
      <p class="muted small" style="margin-top:10px">CV au format PDF ou DOCX, 5 Mo maximum.</p>
    </aside>
  </div>
</main>

<jsp:include page="fragments/pied.jsp"/>
