package com.gestionrh.sirh.event.recrutement;

/**
 * Publié par le bloc 1 lorsque la DG valide définitivement une demande de besoin (transmission au bloc 2).
 * @param idValidateur employé DG ayant pris la décision finale (peut être nul lors d'un rattrapage)
 */
public record DemandeValideeEvent(Integer idDemandeBesoin, Integer idValidateur) { }
