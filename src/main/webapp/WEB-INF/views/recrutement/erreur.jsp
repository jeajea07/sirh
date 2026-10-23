<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="fr"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title><c:out value="${titrePage}"/> - SIRH</title></head><body>
<main><h1>Une erreur est survenue (<c:out value="${statutErreur}"/>)</h1>
<p><c:out value="${messageErreur}"/></p>
<a href="<c:out value='${urlRetour}'/>">Retour aux offres</a></main></body></html>
