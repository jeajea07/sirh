<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/views/recrutement/fragments/entete.jsp"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="barre"><h1>Dossier <c:out value="${candidature.numero}"/></h1><a class="btn sec" href="${ctx}/rh/recrutement/candidatures">Retour à la liste</a></div>
<section class="carte"><h2>Identité</h2><dl>
<dt>Nom</dt><dd><c:out value="${candidature.prenom}"/> <c:out value="${candidature.nom}"/></dd>
<dt>E-mail</dt><dd><c:out value="${candidature.email}"/></dd><dt>Téléphone</dt><dd><c:out value="${candidature.telephone}"/></dd>
<dt>Adresse</dt><dd><c:out value="${candidature.adresse}"/></dd><dt>Offre</dt><dd><c:out value="${candidature.offre}"/></dd>
<dt>Reçue le</dt><dd><c:out value="${candidature.dateReception}"/></dd><dt>Statut</dt><dd><c:out value="${candidature.statut}"/></dd>
<dt>Classement automatique</dt><dd><c:out value="${candidature.classementAuto}"/></dd>
<dt>Décision RH</dt><dd><c:out value="${candidature.classementFinal}"/></dd>
</dl></section>
<section class="carte"><h2>CV numérique</h2><dl>
<dt>Niveau d'étude</dt><dd><c:out value="${candidature.niveauEtude}"/></dd><dt>Expérience</dt><dd><c:out value="${candidature.anneesExperience}"/> an(s)</dd>
<dt>Formations</dt><dd><c:out value="${candidature.formations}"/></dd><dt>Expériences</dt><dd><c:out value="${candidature.experiences}"/></dd>
<dt>Langues</dt><dd><c:out value="${candidature.langues}"/></dd><dt>Disponibilité</dt><dd><c:out value="${candidature.disponibilite}"/></dd>
<dt>Compétences</dt><dd><c:forEach items="${candidature.competences}" var="x"><c:out value="${x}"/><br></c:forEach></dd>
<dt>Fichier joint</dt><dd><a href="${ctx}/rh/recrutement/candidatures/${candidature.id}/cv"><c:out value="${candidature.cvNomFichier}"/> (télécharger)</a></dd>
</dl></section>
<section class="carte"><h2>Critères obligatoires</h2><c:choose><c:when test="${empty candidature.criteresObligatoires}"><p>Aucun critère détaillé.</p></c:when><c:otherwise><table><thead><tr><th>Critère</th><th>Évaluation</th><th>Valeur candidat</th><th>Détail</th></tr></thead><tbody><c:forEach items="${candidature.criteresObligatoires}" var="x"><tr><td><c:out value="${x.nature}"/> — <c:out value="${x.libelle}"/></td><td><c:out value="${x.etat}"/></td><td><c:out value="${x.valeurCandidat}"/></td><td><c:out value="${x.detail}"/></td></tr></c:forEach></tbody></table></c:otherwise></c:choose></section>
<section class="carte"><h2>Critères souhaités (informatifs)</h2><c:choose><c:when test="${empty candidature.criteresSouhaites}"><p>Aucun critère souhaité.</p></c:when><c:otherwise><ul><c:forEach items="${candidature.criteresSouhaites}" var="x"><li><c:out value="${x.detail}"/></li></c:forEach></ul></c:otherwise></c:choose></section>
<section class="carte"><h2>Motif du classement automatique</h2><pre><c:out value="${candidature.motif}"/></pre></section>
<section class="carte"><h2>Historique</h2><c:choose><c:when test="${empty historique}"><p>Aucun événement.</p></c:when><c:otherwise><ol><c:forEach items="${historique}" var="h"><li><c:out value="${h.dateAction}"/> — <c:out value="${h.action}"/> — <c:out value="${h.ancienStatut}"/> → <c:out value="${h.nouveauStatut}"/> — <c:out value="${h.commentaire}"/></li></c:forEach></ol></c:otherwise></c:choose></section>
<c:if test="${peutDecider}"><section class="carte"><h2>Décision RH</h2>
<c:if test="${not classementARegarder}"><form method="post" action="${ctx}/rh/recrutement/candidatures/${candidature.id}/valider"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn ok" type="submit">Valider le classement automatique</button></form></c:if>
<form method="post" action="${ctx}/rh/recrutement/candidatures/${candidature.id}/decider"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
<label>Classement final</label><select name="classement" required><option value="">Choisir</option><c:forEach items="${classementsFinaux}" var="x"><option value="${x}"><c:out value="${x}"/></option></c:forEach></select>
<label>Commentaire (obligatoire si différent du classement automatique)</label><textarea name="commentaire" rows="4" maxlength="2000"></textarea><button class="btn" type="submit">Enregistrer la décision</button></form></section></c:if>
<jsp:include page="/WEB-INF/views/recrutement/fragments/pied.jsp"/>
