<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Formalité sociale" actif="formalites">
  <div class="page-head">
    <div><h1>Formalité <c:out value="${formalite.typeDeclaration}"/></h1><p><c:out value="${formalite.employeNom}"/> — <c:out value="${formalite.organisme}"/></p></div>
    <div class="page-actions"><a class="btn" href="<c:url value='/rh/formalites'/>">Retour</a><c:if test="${formalite.statut == 'A_PREPARER'}"><a class="btn" href="<c:url value='/rh/formalites/${formalite.id}/modifier'/>">Modifier</a></c:if></div>
  </div>
  <f:flash/>
  <div class="detail-grid">
    <div class="card">
      <div class="card-head"><h2>Déclaration</h2><span class="badge <c:out value='${formalite.statutBadge}'/>"><c:out value="${formalite.statutLibelle}"/></span></div>
      <dl class="meta-list">
        <div><dt>Salarié</dt><dd><a href="<c:url value='/rh/salaries/${formalite.idEmploye}'/>"><c:out value="${formalite.employeNom}"/></a></dd></div>
        <div><dt>Organisme</dt><dd><c:out value="${formalite.organisme}"/></dd></div>
        <div><dt>Type</dt><dd><c:out value="${formalite.typeDeclaration}"/></dd></div>
        <div><dt>Préparation</dt><dd><c:out value="${formalite.datePreparationFr}"/></dd></div>
        <div><dt>Envoi</dt><dd><c:out value="${formalite.dateEnvoiFr}"/></dd></div>
        <div><dt>Référence externe</dt><dd><c:out value="${formalite.referenceExterne}"/></dd></div>
        <c:if test="${not empty formalite.idDocumentJustificatif}"><div><dt>Justificatif</dt><dd><a class="btn btn-sm" href="<c:url value='/rh/documents/${formalite.idDocumentJustificatif}/telecharger'/>">Télécharger</a></dd></div></c:if>
      </dl>
      <c:if test="${not empty formalite.commentaire}"><p class="muted" style="margin-top:14px;white-space:pre-line"><c:out value="${formalite.commentaire}"/></p></c:if>
    </div>
    <div class="stack">
      <div class="card"><div class="card-head"><h2>Actions</h2></div><div class="stack">
        <c:if test="${formalite.statut == 'A_PREPARER'}"><form method="post" action="<c:url value='/rh/formalites/${formalite.id}/preparer'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-primary btn-block" type="submit">Marquer préparée</button></form></c:if>
        <c:if test="${formalite.statut == 'PREPAREE'}"><form method="post" action="<c:url value='/rh/formalites/${formalite.id}/transmettre'/>" class="stack"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><div class="field"><label for="dateEnvoi">Date d'envoi *</label><input id="dateEnvoi" type="date" name="dateEnvoi" required></div><div class="field"><label for="referenceExterne">Référence externe *</label><input id="referenceExterne" type="text" name="referenceExterne" required></div><button class="btn btn-primary btn-block" type="submit">Transmettre</button></form></c:if>
        <c:if test="${formalite.statut == 'TRANSMISE'}">
          <form method="post" action="<c:url value='/rh/formalites/${formalite.id}/confirmer'/>" class="stack"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><div class="field"><label for="idDocumentJustificatif">Justificatif de confirmation *</label><select id="idDocumentJustificatif" name="idDocumentJustificatif" required><option value="">Choisir…</option><c:forEach var="d" items="${documents}"><c:if test="${d.typeDocument == 'SOCIAL' or d.typeDocument == 'SANTE'}"><option value="${d.id}"><c:out value="${d.titre}"/> — <c:out value="${d.typeDocument}"/></option></c:if></c:forEach></select></div><button class="btn btn-ok btn-block" type="submit">Confirmer la formalité</button></form>
          <form method="post" action="<c:url value='/rh/formalites/${formalite.id}/rejeter'/>" class="stack"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="text" name="motif" placeholder="Motif du rejet" required><button class="btn btn-danger btn-block" type="submit">Rejeter</button></form>
        </c:if>
        <c:if test="${formalite.statut == 'CONFIRMEE' or formalite.statut == 'REJETEE'}"><p class="muted">Cette formalité est clôturée.</p></c:if>
      </div></div>
    </div>
  </div>
</t:layout>
