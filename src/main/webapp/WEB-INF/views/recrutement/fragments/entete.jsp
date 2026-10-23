<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Entete provisoire du module recrutement.
     A remplacer par le gabarit commun (Faneva) : il suffit de changer ce fichier et pied.jsp. --%>
<!DOCTYPE html>
<html lang="fr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title><c:out value="${titrePage}"/> - SIRH</title>
<style>
  body { font-family: system-ui, sans-serif; margin: 0; background: #f5f6f8; color: #222; }
  header { background: #1f3a5f; color: #fff; padding: 12px 24px; }
  header a { color: #fff; margin-left: 20px; text-decoration: none; }
  main { max-width: 1000px; margin: 24px auto; padding: 0 16px; }
  h1 { font-size: 1.4rem; margin: 0; }
  .barre { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
  .carte { background: #fff; border-radius: 8px; padding: 16px 20px; margin-bottom: 16px; box-shadow: 0 1px 3px rgba(0,0,0,.08); }
  table { width: 100%; border-collapse: collapse; background: #fff; }
  th, td { text-align: left; padding: 8px 10px; border-bottom: 1px solid #e3e6ea; vertical-align: top; }
  th { background: #eef1f5; font-size: .85rem; }
  .btn { display: inline-block; background: #1f3a5f; color: #fff; border: 0; border-radius: 6px; padding: 7px 14px;
         cursor: pointer; text-decoration: none; font-size: .9rem; }
  .btn.sec { background: #6b7a90; } .btn.danger { background: #b3261e; } .btn.ok { background: #2e7d32; }
  form.inline { display: inline; }
  .alert { padding: 10px 14px; border-radius: 6px; margin-bottom: 16px; }
  .alert.ok { background: #e3f4e5; color: #1b5e20; } .alert.ko { background: #fde7e5; color: #8c1d18; }
  .alert.warn { background: #fff4d6; color: #7a5a00; }
  .badge { padding: 2px 9px; border-radius: 10px; font-size: .78rem; background: #dfe4ea; }
  .badge.PUBLIEE { background: #c8e6c9; } .badge.VALIDEE { background: #bbdefb; } .badge.SUSPENDUE { background: #ffe0b2; }
  .badge.ANNULEE { background: #ffcdd2; } .badge.TERMINEE { background: #d7ccc8; }
  label { display: block; font-weight: 600; margin: 12px 0 4px; font-size: .9rem; }
  input[type=text], input[type=number], input[type=date], select, textarea { width: 100%; padding: 7px 9px; border: 1px solid #c4cbd4;
         border-radius: 6px; box-sizing: border-box; font: inherit; }
  .grille { display: grid; grid-template-columns: 1fr 1fr; gap: 0 16px; }
  .err { color: #b3261e; font-size: .82rem; }
  .pagination a { display: inline-block; padding: 4px 10px; margin-right: 4px; background: #fff; border-radius: 4px; text-decoration: none; color: #1f3a5f; }
  .pagination a.actif { background: #1f3a5f; color: #fff; }
  .filtre { display: flex; gap: 8px; margin-bottom: 16px; } .filtre input, .filtre select { width: auto; }
  dl { display: grid; grid-template-columns: 180px 1fr; gap: 6px 12px; margin: 0; } dt { font-weight: 600; }
</style>
</head>
<body>
<header><strong>SIRH</strong>
  <a href="${pageContext.request.contextPath}/recrutement/offres">Offres d'emploi</a>
  <c:if test="${not empty nbNotificationsRecrutement}">
    <a href="${pageContext.request.contextPath}/recrutement/notifications">Mes notifications (<c:out value="${nbNotificationsRecrutement}"/>)</a>
  </c:if>
  <c:if test="${role eq 'RH'}">
    <a href="${pageContext.request.contextPath}/rh/recrutement/reserve">Réserve</a>
  </c:if>
</header>
<main>
<c:if test="${not empty message}"><div class="alert ok"><c:out value="${message}"/></div></c:if>
<c:if test="${not empty erreur}"><div class="alert ko"><c:out value="${erreur}"/></div></c:if>
