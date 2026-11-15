<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Entretiens ${candidature.numero}" actif="entretiens">
  <div class="page-head">
    <div>
      <h1><c:out value="${candidature.candidat.prenom}"/> <c:out value="${candidature.candidat.nom}"/></h1>
      <p>Candidature <b><c:out value="${candidature.numero}"/></b> — <c:out value="${empty candidature.offre ? 'Sans offre' : candidature.offre.titre}"/>
        · <span class="badge badge-info"><c:out value="${candidature.statut}"/></span></p>
    </div>
    <a class="btn" href="<c:url value='/rh/entretiens'/>">Retour</a>
  </div>
  <f:flash/>

  <c:if test="${estConfirmationFinale}">
    <div class="alert alert-ok" style="margin-bottom:16px">Les trois entretiens sont réussis : le dossier est prêt pour la confirmation finale.
      <a class="bold" href="<c:url value='/bloc3/selections'/>">Ouvrir Sélection et embauche →</a></div>
  </c:if>

  <div class="stat-grid" style="margin-bottom:16px">
    <div class="stat-card"><span class="stat-label">Test technique</span><span class="stat-value"><c:out value="${empty resume.test20 ? '—' : resume.test20}"/></span><span class="stat-note">sur 20</span></div>
    <div class="stat-card"><span class="stat-label">Entretien RH</span><span class="stat-value"><c:out value="${empty resume.entretienRh ? '—' : resume.entretienRh}"/></span><span class="stat-note">sur 5</span></div>
    <div class="stat-card"><span class="stat-label">Entretien technicien</span><span class="stat-value"><c:out value="${empty resume.entretienTech ? '—' : resume.entretienTech}"/></span><span class="stat-note">sur 5</span></div>
    <div class="stat-card"><span class="stat-label">Entretien DG</span><span class="stat-value"><c:out value="${empty resume.entretienDg ? '—' : resume.entretienDg}"/></span><span class="stat-note">sur 5</span></div>
  </div>

  <div class="stack">
    <%-- Niveau courant à planifier --%>
    <c:if test="${niveauCourant > 0 and empty courant}">
      <form class="card stack" method="post" action="<c:url value='/rh/entretiens/candidature/${candidature.idCandidature}/planifier'/>">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <div class="card-head"><h2>Planifier : <c:out value="${niveauCourantLibelle}"/></h2><span class="badge badge-warn">À planifier</span></div>
        <div class="form-grid">
          <div class="field"><label for="date">Date et heure *</label><input id="date" type="datetime-local" name="date" min="${dateMin}" required></div>
          <div class="field"><label for="lieu">Lieu</label><input id="lieu" type="text" name="lieu" maxlength="150" placeholder="Ex. Salle de réunion 2"></div>
          <div class="field"><label for="jury">Jury</label><input id="jury" type="text" name="jury" maxlength="255" placeholder="Noms des membres du jury"></div>
          <div class="field"><label for="seuil">Note générale minimale (sur 5) *</label><input id="seuil" type="number" name="seuil" min="1" max="5" step="0.1" value="3" required></div>
        </div>
        <div>
          <h3 style="margin-bottom:6px">Grille d'évaluation</h3>
          <p class="muted small" style="margin-bottom:10px">Questions connues à l'avance. Importance : 1 normale, 2 importante, 3 très importante. Une question éliminatoire exige une note minimale. La grille est figée dès la saisie des notes.</p>
          <div class="table-wrap"><table>
            <thead><tr><th>Question</th><th style="width:130px">Importance</th><th style="width:110px">Éliminatoire</th><th style="width:110px">Note min.</th></tr></thead>
            <tbody>
              <c:forEach var="q" items="${grilleDefaut}" varStatus="s">
                <tr>
                  <td><input type="text" name="question_${s.index}" value="<c:out value='${q.question}'/>" maxlength="255"></td>
                  <td><select name="importance_${s.index}"><option value="1" ${q.importance == 1 ? 'selected' : ''}>1 — normale</option><option value="2" ${q.importance == 2 ? 'selected' : ''}>2 — importante</option><option value="3" ${q.importance == 3 ? 'selected' : ''}>3 — très importante</option></select></td>
                  <td class="center"><input type="checkbox" name="eliminatoire_${s.index}" ${q.eliminatoire ? 'checked' : ''}></td>
                  <td><input type="number" name="min_${s.index}" min="1" max="5" step="0.5" value="${q.noteMinimale}"></td>
                </tr>
              </c:forEach>
              <c:forEach begin="${grilleDefaut.size()}" end="${grilleDefaut.size() + 1}" var="i">
                <tr>
                  <td><input type="text" name="question_${i}" maxlength="255" placeholder="Question supplémentaire (facultatif)"></td>
                  <td><select name="importance_${i}"><option value="1">1 — normale</option><option value="2">2 — importante</option><option value="3">3 — très importante</option></select></td>
                  <td class="center"><input type="checkbox" name="eliminatoire_${i}"></td>
                  <td><input type="number" name="min_${i}" min="1" max="5" step="0.5"></td>
                </tr>
              </c:forEach>
            </tbody>
          </table></div>
        </div>
        <div class="row" style="justify-content:flex-end"><button class="btn btn-primary" type="submit">Planifier et convoquer le candidat</button></div>
      </form>
    </c:if>

    <%-- Entretiens existants (le niveau courant en premier plan, les précédents en consultation) --%>
    <c:forEach var="e" items="${entretiens}">
      <c:set var="estCourant" value="${not empty courant and e.id == courant.id}"/>
      <div class="card stack">
        <div class="card-head">
          <h2><c:out value="${e.niveauLibelle}"/></h2>
          <div class="row">
            <span class="badge"><c:out value="${e.statutLibelle}"/></span>
            <c:if test="${e.decide}"><span class="badge ${e.decision == 'ADMIS' ? 'badge-ok' : 'badge-bad'}">Décision : <c:out value="${e.decision == 'ADMIS' ? 'admis' : 'non admis'}"/></span></c:if>
          </div>
        </div>
        <dl class="kv">
          <dt>Date</dt><dd><b><c:out value="${e.datePrevueFr}"/></b><c:if test="${e.reporte}"> <span class="muted small">(rendez-vous d'origine : <c:out value="${e.dateOrigineFr}"/> — <c:out value="${e.motifReport}"/>)</span></c:if></dd>
          <dt>Lieu</dt><dd><c:out value="${empty e.lieu ? '—' : e.lieu}"/></dd>
          <dt>Jury</dt><dd><c:out value="${empty e.jury ? '—' : e.jury}"/></dd>
          <dt>Seuil</dt><dd><c:out value="${e.seuilGeneral}"/> / 5</dd>
        </dl>

        <c:choose>
          <c:when test="${estCourant and e.modifiable}">
            <form method="post" action="<c:url value='/rh/entretiens/${e.id}/notes'/>" class="stack">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
              <div class="table-wrap"><table>
                <thead><tr><th>Question</th><th>Importance</th><th>Éliminatoire</th><th style="width:130px">Note du jury (1 à 5)</th></tr></thead>
                <tbody>
                  <c:forEach var="n" items="${e.notes}">
                    <tr>
                      <td><c:out value="${n.question}"/></td>
                      <td><c:out value="${n.importance}"/> — <c:out value="${n.importanceLibelle}"/></td>
                      <td><c:choose><c:when test="${n.eliminatoire}"><span class="badge badge-warn">min. <c:out value="${n.noteMinimale}"/></span></c:when><c:otherwise>—</c:otherwise></c:choose></td>
                      <td><input type="number" name="note_${n.id}" min="1" max="5" step="0.5" required aria-label="Note : ${n.question}"></td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table></div>
              <div class="row" style="justify-content:flex-end"><button class="btn btn-primary" type="submit">Enregistrer les notes et calculer</button></div>
            </form>
            <div class="row wrap" style="gap:10px;align-items:flex-end">
              <form method="post" action="<c:url value='/rh/entretiens/${e.id}/reporter'/>" class="row wrap" style="gap:8px;align-items:flex-end">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <div class="field"><label>Nouvelle date</label><input type="datetime-local" name="date" min="${dateMin}" required></div>
                <div class="field"><label>Motif du report</label><input type="text" name="motif" required maxlength="255"></div>
                <button class="btn" type="submit">Reporter</button>
              </form>
              <form method="post" action="<c:url value='/rh/entretiens/${e.id}/absent'/>" data-confirm="Noter le candidat absent ?">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button class="btn btn-danger" type="submit">Candidat absent</button>
              </form>
            </div>
          </c:when>
          <c:otherwise>
            <div class="table-wrap"><table>
              <thead><tr><th>Question</th><th>Importance</th><th>Éliminatoire</th><th>Note</th></tr></thead>
              <tbody>
                <c:forEach var="n" items="${e.notes}">
                  <tr>
                    <td><c:out value="${n.question}"/></td>
                    <td><c:out value="${n.importance}"/></td>
                    <td><c:choose><c:when test="${n.eliminatoire}">min. <c:out value="${n.noteMinimale}"/></c:when><c:otherwise>—</c:otherwise></c:choose></td>
                    <td><b class="${n.eliminatoire and not empty n.note and n.note lt n.noteMinimale ? 'late' : ''}"><c:out value="${empty n.note ? '—' : n.note}"/></b></td>
                  </tr>
                </c:forEach>
              </tbody>
            </table></div>
          </c:otherwise>
        </c:choose>

        <c:if test="${not empty e.resultatPropose}">
          <div class="alert ${e.resultatPropose == 'ADMIS' ? 'alert-ok' : 'alert-warn'}">
            <c:if test="${not empty e.noteGenerale}">Note générale <b><c:out value="${e.noteGenerale}"/> / 5</b> (seuil <c:out value="${e.seuilGeneral}"/>). </c:if>
            Résultat proposé : <b><c:out value="${e.resultatPropose == 'ADMIS' ? 'admis' : 'non admis'}"/></b>
            <c:if test="${not empty e.motifProposition}"> — <c:out value="${e.motifProposition}"/></c:if>
          </div>
        </c:if>

        <c:if test="${e.decide}">
          <p class="muted small">Décision RH du <c:out value="${e.dateDecisionFr}"/><c:if test="${not empty e.motifDecision}"> — motif : <c:out value="${e.motifDecision}"/></c:if></p>
        </c:if>

        <c:if test="${estCourant and e.ADecider}">
          <form method="post" action="<c:url value='/rh/entretiens/${e.id}/decision'/>" class="stack">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <div class="field"><label for="motif-${e.id}">Motif (obligatoire si la décision diffère de la proposition)</label><input id="motif-${e.id}" type="text" name="motif" maxlength="500"></div>
            <div class="row" style="justify-content:flex-end;gap:8px">
              <button class="btn btn-danger" type="submit" name="decision" value="non_admis">Confirmer : non admis</button>
              <button class="btn btn-ok" type="submit" name="decision" value="admis">Confirmer : admis<c:if test="${e.niveau < 3}"> → niveau suivant</c:if><c:if test="${e.niveau == 3}"> → confirmation finale</c:if></button>
            </div>
          </form>
        </c:if>
      </div>
    </c:forEach>

    <c:if test="${niveauCourant == 0 and empty entretiens}">
      <div class="card empty">Cette candidature n'est pas encore à l'étape des entretiens.</div>
    </c:if>
  </div>
</t:layout>
