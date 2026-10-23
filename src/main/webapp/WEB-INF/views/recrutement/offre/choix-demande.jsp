<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout titre="Créer une offre depuis une demande" actif="offre-demande-rh">
  <div class="page-head">
    <div>
      <h1>Créer une offre depuis une demande</h1>
      <p>Choisissez une demande ayant terminé avec succès son circuit de validation.</p>
    </div>
    <a class="btn" href="<c:url value='/recrutement/offres'/>">Retour aux offres</a>
  </div>

  <c:if test="${not empty erreur}"><div class="alert alert-bad" style="margin-bottom:16px"><c:out value="${erreur}"/></div></c:if>
  <div class="card stack" style="max-width:820px">
    <div>
      <h2>Sélection de la demande</h2>
      <p class="muted">L'offre sera créée en brouillon. Vous pourrez compléter ses informations avant de la valider et de la publier.</p>
    </div>
    <c:choose>
      <c:when test="${empty demandesAcceptees}">
        <div class="alert alert-info">Aucune demande acceptée sans offre active n'est disponible.</div>
        <p class="muted">Une demande apparaît ici après la validation finale et si elle n'a pas déjà une offre en cours.</p>
      </c:when>
      <c:otherwise>
        <form method="post" action="<c:url value='/recrutement/offres/nouveau'/>" class="stack">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
          <div class="field">
            <label for="idDemandeBesoin">Demande acceptée</label>
            <select id="idDemandeBesoin" name="idDemandeBesoin" required>
              <c:forEach items="${demandesAcceptees}" var="demande">
                <option value="<c:out value='${demande.id}'/>">
                  <c:out value="${demande.numero}"/> — <c:out value="${demande.poste}"/> — <c:out value="${demande.departement}"/>
                </option>
              </c:forEach>
            </select>
          </div>
          <div class="row wrap">
            <button class="btn btn-primary" type="submit">Créer le brouillon</button>
            <a class="btn" href="<c:url value='/recrutement/offres'/>">Annuler</a>
          </div>
        </form>
      </c:otherwise>
    </c:choose>
  </div>
</t:layout>
