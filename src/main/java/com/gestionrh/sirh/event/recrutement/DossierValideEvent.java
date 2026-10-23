package com.gestionrh.sirh.event.recrutement;

public record DossierValideEvent(Integer idCandidature, Integer idCandidat, Integer idOffre, String numero) { }
