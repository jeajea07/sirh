<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags" %>
<t:layout titre="Paramétrage du temps" actif="temps-rh">
  <c:set var="onglet" value="parametres"/>
  <div class="page-head"><div><h1>Paramétrage du temps</h1><p>Taux de majoration, durées maximales et délais : <code>application.properties</code> (<code>app.temps.*</code>, <code>app.conges.*</code>), à confirmer avec le Code du travail.</p></div></div>
  <f:flash/>
  <div class="tabs"><a class="tab ${onglet == 'anomalies' ? 'active' : ''}" href="<c:url value='/rh/temps'/>">Anomalies</a><a class="tab ${onglet == 'releve' ? 'active' : ''}" href="<c:url value='/rh/temps/releve'/>">Relevés et pointages</a><a class="tab" href="<c:url value='/planning'/>">Plannings</a><a class="tab ${onglet == 'periodes' ? 'active' : ''}" href="<c:url value='/rh/temps/periodes'/>">Clôture et paie</a><a class="tab ${onglet == 'parametres' ? 'active' : ''}" href="<c:url value='/rh/temps/parametres'/>">Paramétrage</a></div>
  <div class="detail-grid">
    <div class="stack">
      <div class="card"><div class="card-head"><h2>Modèles horaires</h2></div>
        <div class="table-wrap"><table><thead><tr><th>Code</th><th>Libellé</th><th>Plage</th><th>Pause</th><th>Durée</th><th>Tolérances</th></tr></thead><tbody>
          <c:forEach var="m" items="${modeles}"><tr><td><b><c:out value="${m.code}"/></b><c:if test="${m.estNuit}"> <span class="badge badge-info">nuit</span></c:if></td><td><c:out value="${m.libelle}"/></td><td><c:out value="${m.plageLibelle}"/></td><td><c:out value="${m.dureePauseMinutes}"/> min</td><td><c:out value="${m.dureeLibelle}"/></td><td class="small">retard <c:out value="${m.toleranceRetardMinutes}"/> / départ <c:out value="${m.toleranceDepartMinutes}"/> min</td></tr></c:forEach>
        </tbody></table></div>
      </div>
      <div class="card"><div class="card-head"><h2>Régimes de travail</h2></div>
        <div class="table-wrap"><table><thead><tr><th>Régime</th><th>Hebdo</th><th>Jours</th><th>Modèle par défaut</th><th>Pointage</th><th>Heures sup.</th></tr></thead><tbody>
          <c:forEach var="r" items="${regimes}"><tr><td><b><c:out value="${r.libelle}"/></b></td><td><c:out value="${r.dureeHebdoHeures}"/> h</td><td><c:out value="${r.joursTravailles}"/></td><td><c:out value="${r.modeleDefaut.code}"/></td><td><c:out value="${r.pointageObligatoire ? 'oui' : 'non'}"/></td><td><c:out value="${r.heuresSupAutorisees ? 'oui' : 'non'}"/></td></tr></c:forEach>
        </tbody></table></div>
        <p class="small muted">Jours : 1 = lundi … 7 = dimanche. Le régime d'un salarié se change depuis son relevé.</p>
      </div>
    </div>
    <div class="stack">
      <div class="card"><div class="card-head"><h2>Nouveau modèle horaire</h2></div>
        <form method="post" action="<c:url value='/rh/temps/parametres/modeles'/>" class="stack">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
          <div class="form-grid"><div class="field"><label>Code *</label><input type="text" name="code" required></div><div class="field"><label>Libellé *</label><input type="text" name="libelle" required></div>
          <div class="field"><label>Début *</label><input type="time" name="heureDebut" required></div><div class="field"><label>Fin *</label><input type="time" name="heureFin" required></div>
          <div class="field"><label>Pause (min)</label><input type="number" min="0" name="pause" value="60"></div><div class="field"><label>Horaire de nuit</label><select name="nuit"><option value="false">Non</option><option value="true">Oui (chevauche minuit)</option></select></div>
          <div class="field"><label>Tolérance retard</label><input type="number" min="0" name="toleranceRetard" value="10"></div><div class="field"><label>Tolérance départ</label><input type="number" min="0" name="toleranceDepart" value="10"></div></div>
          <span class="hint">Un modèle utilisé ne se modifie pas : créez-en une nouvelle version (RG-RT06).</span>
          <button class="btn btn-primary" type="submit">Créer</button>
        </form>
      </div>
      <div class="card"><div class="card-head"><h2>Jours fériés</h2></div>
        <form method="post" action="<c:url value='/rh/temps/parametres/feries'/>" style="display:flex;gap:6px;margin-bottom:10px;flex-wrap:wrap"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><input type="date" name="jour" required><input type="text" name="libelle" placeholder="Libellé" required><button class="btn btn-sm" type="submit">Ajouter</button></form>
        <ul class="timeline compact"><c:forEach var="j" items="${feries}"><li><b><c:out value="${j.jourFr}"/></b> — <c:out value="${j.libelle}"/></li></c:forEach></ul>
      </div>
    </div>
  </div>
</t:layout>
