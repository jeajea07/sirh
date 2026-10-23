<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout titre="${titrePage}" actif="offres-rh">
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="base" value="${ctx}/recrutement/offres/${offre.idOffre}"/>

<div class="page-head">
  <h1><c:out value="${offre.titre}"/> <span class="badge"><c:out value="${offre.statut}"/></span>
    <c:if test="${offreFermeeParDate}"><span class="badge TERMINEE">Clôturée (date limite dépassée)</span></c:if>
  </h1>
  <a class="btn" href="${ctx}/recrutement/offres">Retour à la liste</a>
</div>

<c:if test="${not empty message}"><div class="alert alert-ok" style="margin-bottom:16px"><c:out value="${message}"/></div></c:if>
<c:if test="${not empty erreur}"><div class="alert alert-bad" style="margin-bottom:16px"><c:out value="${erreur}"/></div></c:if>

<c:if test="${not empty manquants}">
  <div class="alert alert-warn">
    <strong>Offre incomplete</strong> : elle ne peut pas etre validee ni publiee. A renseigner :
    <c:forEach items="${manquants}" var="m" varStatus="st"><c:out value="${m}"/><c:if test="${!st.last}">, </c:if></c:forEach>.
  </div>
</c:if>

<div class="card">
  <dl class="kv">
    <dt>Reference</dt><dd><c:out value="${offre.reference}"/></dd>
    <dt>Demande de besoin</dt><dd>n&deg;<c:out value="${offre.idDemandeBesoin}"/></dd>
    <dt>Nombre de postes</dt><dd><c:out value="${offre.nombrePostes}"/></dd>
    <dt>Salaire proposé</dt><dd><c:out value="${offre.salairePropose}"/></dd>
    <dt>Durée du contrat</dt><dd><c:choose><c:when test="${empty offre.dureeMois}">-</c:when><c:otherwise><c:out value="${offre.dureeMois}"/> mois</c:otherwise></c:choose></dd>
    <dt>Candidatures reçues</dt><dd><c:out value="${offre.nbCandidatures}"/></dd>
    <dt>Date limite</dt><dd><c:out value="${offre.dateLimite}"/></dd>
    <dt>Validée le</dt><dd><c:out value="${fn:replace(fn:substring(offre.dateValidation, 0, 16), 'T', ' ')}"/></dd>
    <dt>Publiée le</dt><dd><c:out value="${fn:replace(fn:substring(offre.datePublication, 0, 16), 'T', ' ')}"/></dd>
    <dt>Description</dt><dd><c:out value="${offre.description}"/></dd>
    <dt>Missions</dt><dd><c:out value="${offre.missions}"/></dd>
  </dl>
</div>

<div class="card" style="margin-top:16px">
  <c:if test="${offre.statut == 'BROUILLON'}">
    <a class="btn" href="${base}/modifier">Modifier</a>
    <form class="inline" method="post" action="${base}/valider"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-primary" type="submit">Valider</button></form>
    <form class="inline" method="post" action="${base}/supprimer" onsubmit="return confirm('Supprimer ce brouillon ?');">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
      <button class="btn danger" type="submit">Supprimer</button></form>
  </c:if>
  <c:if test="${offre.statut == 'VALIDEE'}">
    <form class="inline" method="post" action="${base}/publier"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-primary" type="submit">Publier sur le site vitrine</button></form>
  </c:if>
  <c:if test="${offre.statut == 'PUBLIEE'}">
    <form class="inline" method="post" action="${base}/suspendre"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn" type="submit">Suspendre</button></form>
  </c:if>
  <c:if test="${offre.statut == 'SUSPENDUE'}">
    <form class="inline" method="post" action="${base}/reprendre"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-primary" type="submit">Reprendre</button></form>
  </c:if>
  <c:if test="${offre.statut == 'PUBLIEE' || offre.statut == 'SUSPENDUE'}">
    <form class="inline" method="post" action="${base}/terminer" onsubmit="return confirm('Terminer cette offre ?');">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
      <button class="btn" type="submit">Terminer</button></form>
  </c:if>
  <c:if test="${offre.statut != 'ANNULEE' && offre.statut != 'TERMINEE'}">
    <form class="inline" method="post" action="${base}/annuler" onsubmit="return confirm('Annuler cette offre ? Cette action est definitive.');">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
      <button class="btn danger" type="submit">Annuler l'offre</button></form>
  </c:if>
</div>

<%-- Criteres : obligatoires (classement) puis souhaites (affichage seulement) --%>
<c:forEach items="${[obligatoires, souhaites]}" var="liste" varStatus="st">
  <div class="card" style="margin-top:16px">
    <h2 style="font-size:1.05rem;margin-top:0">${st.first ? 'Criteres obligatoires (servent au classement)' : 'Criteres souhaites (affichage seulement)'}</h2>
    <c:if test="${empty liste}"><p>Aucun critere.</p></c:if>
    <c:if test="${not empty liste}">
      <table>
        <tr><th>Nature</th><th>Valeur</th><th></th></tr>
        <c:forEach items="${liste}" var="c">
          <tr>
            <td><c:out value="${c.nature}"/></td>
            <td>
              <c:choose>
                <c:when test="${c.nature == 'EXPERIENCE'}"><c:out value="${c.valeurMin}"/> an(s) minimum</c:when>
                <c:otherwise><c:out value="${c.libelle}"/></c:otherwise>
              </c:choose>
            </td>
            <td>
              <c:if test="${offre.statut == 'BROUILLON'}">
                <form class="inline" method="post" action="${base}/criteres/${c.idCritereOffre}/supprimer">
                  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                  <button class="btn danger" type="submit">Retirer</button></form>
              </c:if>
            </td>
          </tr>
        </c:forEach>
      </table>
    </c:if>
  </div>
</c:forEach>

<c:if test="${offre.statut == 'BROUILLON'}">
  <div class="card" style="margin-top:16px">
    <h2 style="font-size:1.05rem;margin-top:0">Ajouter un critere</h2>
    <form method="post" action="${base}/criteres">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
      <div class="grid g2">
        <div>
          <label>Type</label>
          <select name="typeCritere">
            <option value="OBLIGATOIRE">Obligatoire</option>
            <option value="SOUHAITE">Souhaite</option>
          </select>
        </div>
        <div>
          <label>Nature</label>
          <select name="nature" id="nature" onchange="majChamps()">
            <option value="NIVEAU_ETUDE">Niveau d'etude</option>
            <option value="EXPERIENCE">Experience</option>
            <option value="COMPETENCE">Competence</option>
            <option value="LANGUE">Langue</option>
          </select>
        </div>
      </div>
      <div id="c-niveau"><label>Niveau d'etude minimum</label>
        <select name="niveauEtude"><c:forEach items="${niveaux}" var="n"><option value="<c:out value='${n}'/>"><c:out value="${n}"/></option></c:forEach></select></div>
      <div id="c-exp"><label>Annees d'experience minimum</label><input type="number" name="valeurMin" min="0"></div>
      <div id="c-lib"><label>Competence ou langue</label><input type="text" name="libelle" maxlength="150"></div>
      <p><button class="btn" type="submit">Ajouter</button></p>
    </form>
  </div>
  <script>
    function majChamps() {
      var n = document.getElementById('nature').value;
      document.getElementById('c-niveau').style.display = n === 'NIVEAU_ETUDE' ? '' : 'none';
      document.getElementById('c-exp').style.display    = n === 'EXPERIENCE'   ? '' : 'none';
      document.getElementById('c-lib').style.display    = (n === 'COMPETENCE' || n === 'LANGUE') ? '' : 'none';
    }
    majChamps();
  </script>
</c:if>

</t:layout>
