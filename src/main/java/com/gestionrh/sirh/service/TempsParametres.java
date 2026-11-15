package com.gestionrh.sirh.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Paramètres des Blocs 6 et 7 (application.properties). Aucun taux n'est codé en dur (Bloc 6 §4, RG-RT07).
 */
@Component
public class TempsParametres {

    @Value("${app.temps.jours-recalcul:7}") public int joursRecalcul;
    @Value("${app.temps.duree-max-jour-heures:10}") public int dureeMaxJourHeures;
    @Value("${app.temps.duree-max-hebdo-heures:48}") public int dureeMaxHebdoHeures;
    @Value("${app.temps.jours-consecutifs-max:6}") public int joursConsecutifsMax;
    @Value("${app.temps.delai-prevenance-planning-jours:1}") public int delaiPrevenancePlanningJours;
    @Value("${app.temps.delai-justification-jours:3}") public int delaiJustificationJours;
    @Value("${app.temps.heures-mensuelles-reference:173.33}") public BigDecimal heuresMensuellesReference;
    @Value("${app.temps.majoration-heures-sup:0.30}") public BigDecimal majorationHeuresSup;
    @Value("${app.temps.majoration-nuit:0.30}") public BigDecimal majorationNuit;
    @Value("${app.temps.majoration-dimanche:0.40}") public BigDecimal majorationDimanche;
    @Value("${app.temps.majoration-ferie:0.50}") public BigDecimal majorationFerie;
    @Value("${app.conges.acquisition-mensuelle-jours:2.5}") public BigDecimal acquisitionMensuelleJours;
    @Value("${app.conges.permissions-annuelles-jours:10}") public BigDecimal permissionsAnnuellesJours;
    @Value("${app.conges.delai-validation-manager-jours:3}") public int delaiValidationManagerJours;
}
