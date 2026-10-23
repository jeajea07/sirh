<%-- Bloc 3 --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout titre="Dossier de sélection" actif="bloc3">
  <div class="page-head">
    <div>
      <h1>Dossier candidat #<c:out value="${selection.idCandidatB2}"/></h1>
      <p>Candidature #<c:out value="${selection.idCandidatureB2}"/> · Offre #<c:out value="${selection.idOffreB2}"/></p>
    </div>
    <a class="btn" href="<c:url value='/bloc3/selections'/>">Retour à la liste</a>
  </div>

  <c:if test="${not empty succes}"><div class="alert alert-ok" style="margin-bottom:16px"><c:out value="${succes}"/></div></c:if>
  <c:if test="${not empty erreur}"><div class="alert alert-bad" style="margin-bottom:16px"><c:out value="${erreur}"/></div></c:if>

  <section class="card card-flat" style="margin-bottom:16px">
    <div class="card-head"><h2>Résultats transmis par le Bloc 2</h2><span class="muted small">Les notes ne sont pas recalculées dans le Bloc 3.</span></div>
    <div class="table-wrap"><table>
      <thead><tr><th>Rang</th><th>Test (/20)</th><th>Entretien RH</th><th>Entretien technique</th><th>Entretien DG</th><th>Note finale (/5)</th></tr></thead>
      <tbody><tr>
        <td><c:out value="${empty selection.rangClassement ? '—' : selection.rangClassement}"/></td>
        <td><c:out value="${empty selection.scoreTest ? '—' : selection.scoreTest}"/></td>
        <td><c:out value="${empty selection.scoreEntretienRh ? '—' : selection.scoreEntretienRh}"/></td>
        <td><c:out value="${empty selection.scoreEntretienTech ? '—' : selection.scoreEntretienTech}"/></td>
        <td><c:out value="${empty selection.scoreEntretienDg ? '—' : selection.scoreEntretienDg}"/></td>
        <td><b><c:out value="${empty selection.scoreFinal ? '—' : selection.scoreFinal}"/></b></td>
      </tr></tbody>
    </table></div>
  </section>

  <div class="card" style="margin-bottom:16px">
    <div class="steps">
      <div class="step done"><i>1</i><span>Dossier Bloc 2</span></div>
      <div class="step ${selection.etatDossier.code == 'COMPLET' ? 'done' : 'current'}"><i>2</i><span>Vérification</span></div>
      <div class="step ${not empty selection.decision ? 'done' : ''}"><i>3</i><span>Décision</span></div>
      <div class="step ${not empty propositions ? 'done' : ''}"><i>4</i><span>Proposition</span></div>
      <div class="step ${not empty recrutement ? 'done' : ''}"><i>5</i><span>Bloc 4</span></div>
    </div>
  </div>

  <div class="grid two" style="display:grid;grid-template-columns:repeat(auto-fit,minmax(300px,1fr));gap:16px">
    <section class="card stack">
      <div class="card-head"><h2>Vérification du dossier</h2><span class="badge"><c:out value="${selection.etatDossier.libelle}"/></span></div>
      <dl class="kv">
        <dt>Éléments à vérifier</dt><dd><c:out value="${empty selection.elementsManquants ? 'Aucun élément signalé' : selection.elementsManquants}"/></dd>
      </dl>
      <c:if test="${pageContext.request.isUserInRole('RH')}">
        <form method="post" action="<c:url value='/bloc3/selections/${selection.idSelection}/verification'/>">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
          <div class="form-grid">
            <div class="field"><label for="codeEtat">Résultat</label>
              <select id="codeEtat" name="codeEtat" required>
                <c:forEach var="etat" items="${etatsDossier}"><option value="${etat.code}" ${selection.etatDossier.id == etat.id ? 'selected' : ''}><c:out value="${etat.libelle}"/></option></c:forEach>
              </select>
            </div>
            <div class="field span2"><label for="elementsManquants">Éléments manquants ou incohérences</label>
              <textarea id="elementsManquants" name="elementsManquants"><c:out value="${selection.elementsManquants}"/></textarea>
            </div>
          </div>
          <button class="btn btn-primary" type="submit" style="margin-top:12px">Enregistrer la vérification</button>
        </form>
      </c:if>
    </section>

    <section class="card stack">
      <div class="card-head"><h2>Décision finale</h2><span class="badge"><c:out value="${empty selection.decision ? 'À décider' : selection.decision.libelle}"/></span></div>
      <dl class="kv">
        <dt>Décidée le</dt><dd><c:out value="${empty selection.decideLe ? '—' : selection.decideLe}"/></dd>
        <dt>Justification</dt><dd><c:out value="${empty selection.justification ? '—' : selection.justification}"/></dd>
        <dt>Validation</dt><dd><c:out value="${selection.etatValidation.libelle}"/></dd>
      </dl>

      <c:if test="${pageContext.request.isUserInRole('RH') && selection.etatDossier.code == 'COMPLET' && not empty selection.scoreFinal}">
        <form method="post" action="<c:url value='/bloc3/selections/${selection.idSelection}/decision'/>">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
          <div class="form-grid">
            <div class="field"><label for="codeDecision">Décision</label>
              <select id="codeDecision" name="codeDecision" required>
                <c:forEach var="decision" items="${decisions}"><option value="${decision.code}" ${not empty selection.decision && selection.decision.id == decision.id ? 'selected' : ''}><c:out value="${decision.libelle}"/></option></c:forEach>
              </select>
            </div>
            <div class="field"><label for="rangListeAttente">Rang si liste d’attente</label><input id="rangListeAttente" type="number" min="1" name="rangListeAttente" value="${selection.rangListeAttente}"></div>
            <div class="field span2"><label for="justification">Justification</label><textarea id="justification" name="justification"><c:out value="${selection.justification}"/></textarea></div>
            <label class="check span2"><input type="checkbox" name="validationObligatoire" value="true" ${selection.validationObligatoire ? 'checked' : ''}>Exiger une validation hiérarchique avant la proposition</label>
          </div>
          <button class="btn btn-primary" type="submit" style="margin-top:12px">Enregistrer la décision</button>
        </form>
      </c:if>

      <c:if test="${selection.validationObligatoire && selection.etatValidation.code == 'EN_ATTENTE' && (pageContext.request.isUserInRole('RH') || pageContext.request.isUserInRole('DG'))}">
        <form method="post" action="<c:url value='/bloc3/selections/${selection.idSelection}/validation'/>">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
          <div class="field"><label for="commentaireValidation">Commentaire de validation ou de refus</label><textarea id="commentaireValidation" name="commentaire"></textarea></div>
          <div class="row wrap" style="margin-top:10px">
            <button class="btn btn-ok" name="approuvee" value="true" type="submit">Valider la décision</button>
            <button class="btn btn-danger" name="approuvee" value="false" type="submit">Refuser la validation</button>
          </div>
        </form>
      </c:if>
    </section>
  </div>

  <section class="card stack" style="margin-top:16px">
    <div class="card-head"><h2>Propositions d’embauche</h2></div>
    <c:if test="${empty propositions}"><p class="muted">Aucune proposition préparée.</p></c:if>
    <c:forEach var="p" items="${propositions}">
      <div class="card" style="margin-bottom:12px">
        <div class="card-head"><h3>Version <c:out value="${p.numeroVersion}"/> · <c:out value="${p.intitulePoste}"/></h3><span class="badge badge-info"><c:out value="${p.etatProposition.libelle}"/></span></div>
        <dl class="kv">
          <dt>Contrat</dt><dd><c:out value="${p.typeContrat.libelle}"/></dd>
          <dt>Salaire mensuel</dt><dd><c:out value="${p.salaireMensuel}"/> Ar</dd>
          <dt>Début prévu</dt><dd><c:out value="${p.dateDebutPrevue}"/></dd>
          <dt>Durée</dt><dd><c:choose><c:when test="${empty p.dureeMois}">—</c:when><c:otherwise><c:out value="${p.dureeMois}"/> mois</c:otherwise></c:choose></dd>
          <dt>Envoyée le</dt><dd><c:out value="${empty p.envoyeeLe ? '—' : p.envoyeeLe}"/></dd>
          <dt>Réponse limite</dt><dd><c:out value="${empty p.dateLimiteReponse ? '—' : p.dateLimiteReponse}"/></dd>
        </dl>

        <c:if test="${pageContext.request.isUserInRole('RH') && p.etatProposition.code == 'A_PREPARER'}">
          <form method="post" action="<c:url value='/bloc3/propositions/${p.idProposition}/envoyer'/>">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <button class="btn btn-primary" type="submit">Marquer comme envoyée</button>
          </form>
        </c:if>

        <c:if test="${pageContext.request.isUserInRole('RH') && p.etatProposition.code == 'ENVOYEE'}">
          <form method="post" action="<c:url value='/bloc3/propositions/${p.idProposition}/reponse'/>">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <div class="form-grid">
              <div class="field"><label for="typeReponse${p.idProposition}">Réponse reçue</label>
                <select id="typeReponse${p.idProposition}" name="codeTypeReponse" required><c:forEach var="type" items="${typesReponse}"><option value="${type.code}"><c:out value="${type.libelle}"/></option></c:forEach></select>
              </div>
              <div class="field"><label for="commentaireReponse${p.idProposition}">Commentaire (obligatoire pour refus ou demande)</label><input id="commentaireReponse${p.idProposition}" type="text" name="commentaire"></div>
            </div>
            <button class="btn" type="submit" style="margin-top:10px">Enregistrer la réponse</button>
          </form>
        </c:if>

        <c:if test="${not empty reponsesParProposition[p.idProposition]}">
          <h4>Historique des réponses</h4>
          <ul><c:forEach var="rep" items="${reponsesParProposition[p.idProposition]}"><li><c:out value="${rep.typeReponse.libelle}"/> — <c:out value="${rep.reponseLe}"/> <c:out value="${rep.commentaire}"/></li></c:forEach></ul>
        </c:if>
      </div>
    </c:forEach>

    <c:if test="${pageContext.request.isUserInRole('RH') && selection.decision.code == 'RETENU' && (!selection.validationObligatoire || selection.etatValidation.code == 'VALIDEE')}">
      <form method="post" action="<c:url value='/bloc3/selections/${selection.idSelection}/propositions'/>">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <h3>Préparer une proposition</h3>
        <div class="form-grid">
          <div class="field"><label for="intitulePoste">Poste</label><input id="intitulePoste" type="text" name="intitulePoste" required></div>
          <div class="field"><label for="idTypeContrat">Type de contrat</label><select id="idTypeContrat" name="idTypeContrat" required><option value="">Choisir</option><c:forEach var="type" items="${typesContrat}"><option value="${type.id}"><c:out value="${type.libelle}"/></option></c:forEach></select></div>
          <div class="field"><label for="salaireMensuel">Salaire mensuel (Ar)</label><input id="salaireMensuel" type="number" min="0" step="0.01" name="salaireMensuel" required></div>
          <div class="field"><label for="dateDebutPrevue">Date de début prévue</label><input id="dateDebutPrevue" type="date" name="dateDebutPrevue" required></div>
          <div class="field"><label for="dureeMois">Durée en mois (CDD)</label><input id="dureeMois" type="number" min="1" name="dureeMois"></div>
          <div class="field"><label for="dateLimiteReponse">Date limite de réponse (facultative)</label><input id="dateLimiteReponse" type="date" name="dateLimiteReponse"></div>
        </div>
        <button class="btn btn-primary" type="submit" style="margin-top:12px">Enregistrer le brouillon</button>
      </form>
    </c:if>
  </section>

  <section class="card stack" style="margin-top:16px">
    <div class="card-head"><h2>Confirmation et transfert au Bloc 4</h2></div>
    <c:choose>
      <c:when test="${empty recrutement}">
        <p class="muted">Le recrutement pourra être confirmé après acceptation de la proposition.</p>
        <c:if test="${pageContext.request.isUserInRole('RH') && not empty propositions && propositions[0].etatProposition.code == 'ACCEPTEE'}">
          <form method="post" action="<c:url value='/bloc3/selections/${selection.idSelection}/confirmer'/>">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <div class="field" style="max-width:280px"><label for="nombrePostes">Nombre de postes autorisés pour cette offre</label><input id="nombrePostes" type="number" name="nombrePostes" min="1" required><span class="hint">À reprendre du besoin validé transmis par le Bloc 2.</span></div>
            <button class="btn btn-primary" type="submit" style="margin-top:10px">Confirmer le recrutement</button>
          </form>
        </c:if>
      </c:when>
      <c:otherwise>
        <dl class="kv">
          <dt>Recrutement</dt><dd><c:out value="${recrutement.etatRecrutement.libelle}"/></dd>
          <dt>Confirmé le</dt><dd><c:out value="${recrutement.confirmeLe}"/></dd>
          <dt>Transfert</dt><dd><c:out value="${recrutement.etatTransfert.libelle}"/></dd>
          <dt>Référence Bloc 4</dt><dd><c:out value="${empty recrutement.referenceBloc4 ? '—' : recrutement.referenceBloc4}"/></dd>
          <dt>Erreur</dt><dd><c:out value="${empty recrutement.erreurTransfert ? '—' : recrutement.erreurTransfert}"/></dd>
        </dl>
        <c:if test="${pageContext.request.isUserInRole('RH') && recrutement.etatTransfert.code != 'TRANSMIS'}">
          <form method="post" action="<c:url value='/bloc3/selections/${selection.idSelection}/transfert'/>">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <div class="form-grid">
              <div class="field">
                <label for="idPoste">Poste du salarié</label>
                <select id="idPoste" name="idPoste" required>
                  <option value="">Choisir un poste…</option>
                  <c:forEach var="p" items="${postes}">
                    <option value="${p.id}"><c:out value="${p.designation}"/></option>
                  </c:forEach>
                </select>
              </div>
              <div class="field">
                <label for="idDepartement">Département d'affectation</label>
                <select id="idDepartement" name="idDepartement" required>
                  <option value="">Choisir un département…</option>
                  <c:forEach var="d" items="${departements}">
                    <option value="${d.id}"><c:out value="${d.designation}"/></option>
                  </c:forEach>
                </select>
                <span class="hint">Ces informations créent le salarié, son contrat, son affectation et son onboarding.</span>
              </div>
            </div>
            <button class="btn btn-primary" name="transmis" value="true" type="submit" style="margin-top:10px">Transférer et créer le contrat</button>
          </form>
          <details style="margin-top:12px">
            <summary class="btn btn-sm">Transfert manuel (dépannage)</summary>
            <form method="post" action="<c:url value='/bloc3/selections/${selection.idSelection}/transfert'/>" class="stack" style="margin-top:8px">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
              <div class="form-grid">
                <div class="field"><label for="referenceBloc4">Référence de réception du Bloc 4</label><input id="referenceBloc4" type="text" name="referenceBloc4"></div>
                <div class="field"><label for="erreurTransfert">Motif si le transfert a échoué</label><input id="erreurTransfert" type="text" name="erreurTransfert"></div>
              </div>
              <div class="row wrap" style="margin-top:10px">
                <button class="btn btn-ok" name="transmis" value="true" type="submit">Confirmer manuellement</button>
                <button class="btn btn-danger" name="transmis" value="false" type="submit">Enregistrer un échec</button>
              </div>
            </form>
          </details>
        </c:if>
      </c:otherwise>
    </c:choose>
    <p class="hint">Le transfert automatique construit un dossier Bloc 4 à partir du recrutement confirmé. Le transfert manuel reste disponible pour dépanner ou enregistrer une réception externe.</p>
  </section>

  <section class="card stack" style="margin-top:16px">
    <div class="card-head"><h2>Historique</h2></div>
    <c:choose>
      <c:when test="${empty historique}"><p class="muted">Aucune action enregistrée pour le moment.</p></c:when>
      <c:otherwise>
        <ul class="timeline"><c:forEach var="h" items="${historique}"><li><time><c:out value="${h.dateAction}"/></time><br><b><c:out value="${h.action}"/></b> — <c:out value="${h.commentaire}"/></li></c:forEach></ul>
      </c:otherwise>
    </c:choose>
  </section>
</t:layout>
