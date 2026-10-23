package com.gestionrh.sirh.dto.recrutement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Projection en liste blanche des seules informations publiables d'une offre. */
public record OffrePublique(Integer id, String reference, String titre, String description, String missions,
        BigDecimal salaire, LocalDate dateLimite, List<CritereOffrePublic> criteres) { }
