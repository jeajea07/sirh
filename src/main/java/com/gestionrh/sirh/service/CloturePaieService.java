package com.gestionrh.sirh.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.AbsenceModel;
import com.gestionrh.sirh.model.DemandeCongeModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.JourneeTravailModel;
import com.gestionrh.sirh.model.PeriodePaieModel;
import com.gestionrh.sirh.model.VariablePaieModel;
import com.gestionrh.sirh.repository.AbsenceRepository;
import com.gestionrh.sirh.repository.AnomaliePointageRepository;
import com.gestionrh.sirh.repository.DemandeCongeRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.JourneeTravailRepository;
import com.gestionrh.sirh.repository.PeriodePaieRepository;
import com.gestionrh.sirh.repository.PlanningRepository;
import com.gestionrh.sirh.repository.VariablePaieRepository;

/**
 * Bloc 6 §11 et Bloc 7 §11 — clôture mensuelle : contrôles, variables de paie (temps + congés),
 * acquisition des congés payés, verrouillage de la période (RG-VP, RG-PP).
 */
@Service
public class CloturePaieService {

    private final PeriodePaieRepository periodes;
    private final VariablePaieRepository variables;
    private final JourneeTravailRepository journees;
    private final AnomaliePointageRepository anomalies;
    private final AbsenceRepository absences;
    private final DemandeCongeRepository demandes;
    private final PlanningRepository plannings;
    private final EmployeRepository employes;
    private final CalculTempsService calcul;
    private final CongeService conges;
    private final SoldeService soldes;

    public CloturePaieService(PeriodePaieRepository periodes, VariablePaieRepository variables,
                              JourneeTravailRepository journees, AnomaliePointageRepository anomalies,
                              AbsenceRepository absences, DemandeCongeRepository demandes, PlanningRepository plannings,
                              EmployeRepository employes, CalculTempsService calcul, CongeService conges, SoldeService soldes) {
        this.periodes = periodes;
        this.variables = variables;
        this.journees = journees;
        this.anomalies = anomalies;
        this.absences = absences;
        this.demandes = demandes;
        this.plannings = plannings;
        this.employes = employes;
        this.calcul = calcul;
        this.conges = conges;
        this.soldes = soldes;
    }

    @Transactional(readOnly = true)
    public List<PeriodePaieModel> periodes() {
        return periodes.findAllByOrderByAnneeDescMoisDesc();
    }

    @Transactional(readOnly = true)
    public List<VariablePaieModel> variables(Integer idPeriode) {
        return variables.dePeriode(idPeriode);
    }

    /** Points bloquants avant clôture (RG-VP01, RG-AN07, RG-PP03). Liste vide = clôture possible. */
    @Transactional(readOnly = true)
    public List<String> blocages(int annee, int mois) {
        LocalDate premier = LocalDate.of(annee, mois, 1);
        LocalDate dernier = premier.plusMonths(1).minusDays(1);
        List<String> b = new java.util.ArrayList<>();
        if (!dernier.isBefore(LocalDate.now())) b.add("Le mois n'est pas terminé.");
        if (periodes.existsByAnneeAndMoisAndStatut(annee, mois, "CLOTUREE")) b.add("La période est déjà clôturée.");
        long anos = anomalies.aTraiterEntre(premier, dernier);
        if (anos > 0) b.add(anos + " anomalie(s) de pointage à traiter (RG-AN07).");
        long enAttente = absences.toutesActivesEntre(premier, dernier).stream().filter(AbsenceModel::isEnAttente).count();
        if (enAttente > 0) b.add(enAttente + " absence(s) en attente de décision RH (RG-PP03).");
        return b;
    }

    @Transactional
    public PeriodePaieModel cloturer(int annee, int mois, EmployeModel rh) {
        LocalDate premier = LocalDate.of(annee, mois, 1);
        LocalDate dernier = premier.plusMonths(1).minusDays(1);
        // Dernier recalcul avant verrouillage (RG-TT07), puis contrôle des blocages.
        for (EmployeModel e : employes.findByActifTrueOrderByNomAsc()) calcul.recalculer(e, premier, dernier);
        conges.avancerStatuts(LocalDate.now());
        List<String> b = blocages(annee, mois);
        if (!b.isEmpty()) throw new Bloc4Exception("Clôture impossible : " + String.join(" ", b));

        PeriodePaieModel p = periodes.findByAnneeAndMois(annee, mois).orElseGet(() -> {
            PeriodePaieModel n = new PeriodePaieModel();
            n.setAnnee(annee);
            n.setMois(mois);
            return periodes.save(n);
        });

        // --- Variables du Bloc 6 : temps ---
        Map<EmployeModel, List<JourneeTravailModel>> parSalarie = new LinkedHashMap<>();
        for (JourneeTravailModel j : journees.findByJourBetween(premier, dernier)) {
            parSalarie.computeIfAbsent(j.getEmploye(), k -> new java.util.ArrayList<>()).add(j);
        }
        parSalarie.forEach((e, js) -> {
            int sup = somme(js, JourneeTravailModel::getMinutesSup);
            ajouter(p, e, "HEURES_NORMALES", "Heures normales travaillées", heures(somme(js, JourneeTravailModel::getMinutesTravaillees) - sup), "HEURE", "BLOC6");
            ajouter(p, e, "HEURES_SUP", "Heures supplémentaires", heures(sup), "HEURE", "BLOC6");
            ajouter(p, e, "HEURES_NUIT", "Heures de nuit", heures(somme(js, JourneeTravailModel::getMinutesNuit)), "HEURE", "BLOC6");
            ajouter(p, e, "HEURES_DIMANCHE", "Heures du dimanche", heures(somme(js, JourneeTravailModel::getMinutesDimanche)), "HEURE", "BLOC6");
            ajouter(p, e, "HEURES_FERIE", "Heures de jour férié", heures(somme(js, JourneeTravailModel::getMinutesFerie)), "HEURE", "BLOC6");
            ajouter(p, e, "RETARDS", "Retards et départs anticipés", heures(somme(js, j -> j.getMinutesRetard() + j.getMinutesDepartAnticipe())), "HEURE", "BLOC6");
            ajouter(p, e, "ABSENCE_INJUSTIFIEE_HEURES", "Absence injustifiée (heures)", heures(somme(js, JourneeTravailModel::getMinutesAbsence)), "HEURE", "BLOC6");
            ajouter(p, e, "JOURS_TRAVAILLES", "Jours travaillés", BigDecimal.valueOf(js.stream().filter(j -> j.getMinutesTravaillees() > 0).count()), "JOUR", "BLOC6");
        });

        // --- Variables du Bloc 7 : congés et absences, découpés par mois (RG-PP04) ---
        Map<String, BigDecimal> cumul = new LinkedHashMap<>();
        Map<String, Object[]> meta = new LinkedHashMap<>();
        for (DemandeCongeModel d : demandes.toutesValideesEntre(premier, dernier)) {
            LocalDate debut = d.getDateDebut().isBefore(premier) ? premier : d.getDateDebut();
            LocalDate fin = d.getDateFin().isAfter(dernier) ? dernier : d.getDateFin();
            BigDecimal jours = conges.calculerJours(d.getType(), debut, debut.equals(d.getDateDebut()) ? d.getDemiJourneeDebut() : "MATIN",
                    fin, fin.equals(d.getDateFin()) ? d.getDemiJourneeFin() : "APRES_MIDI");
            cumuler(cumul, meta, d.getEmploye(), "CONGE_" + d.getType().getCode(), d.getType().getLibelle() + " (" + d.getType().getRemunerationLibelle() + ")", jours);
        }
        for (AbsenceModel a : absences.toutesActivesEntre(premier, dernier)) {
            // Une absence constatée au pointage est déjà comptée en heures par le Bloc 6 : pas de double retenue.
            if ("CONSTATEE_POINTAGE".equals(a.getOrigine())) continue;
            LocalDate debut = a.getDateDebut().isBefore(premier) ? premier : a.getDateDebut();
            LocalDate fin = a.getDateFin().isAfter(dernier) ? dernier : a.getDateFin();
            cumuler(cumul, meta, a.getEmploye(), "ABSENCE_" + a.getType().getCode(), a.getType().getLibelle() + " (" + a.getType().getRemunerationLibelle() + ")",
                    BigDecimal.valueOf(SoldeService.chevauchement(debut, fin, debut, fin)));
        }
        cumul.forEach((cle, jours) -> {
            Object[] m = meta.get(cle);
            ajouter(p, (EmployeModel) m[0], (String) m[1], (String) m[2], jours, "JOUR", "BLOC7");
        });
        // RG-SC10 : solde figé à la sortie, transmis pour l'indemnité compensatrice.
        for (EmployeModel e : employes.findAll()) {
            if (e.getDateSortie() != null && !e.getDateSortie().isBefore(premier) && !e.getDateSortie().isAfter(dernier)) {
                ajouter(p, e, "SOLDE_CONGES_SORTIE", "Congés payés non pris à indemniser",
                        soldes.solde(e.getId(), "CONGE_PAYE").getDisponible(), "JOUR", "BLOC7");
            }
        }

        // Bloc 7 §6.3 : acquisition du mois, puis verrouillage (RG-VP02).
        soldes.acquisitionMensuelle(annee, mois);
        for (JourneeTravailModel j : journees.findByJourBetween(premier, dernier)) {
            j.setStatut("CLOTUREE");
            journees.save(j);
        }
        plannings.findByStatutInOrderByDateDebutAsc(List.of("PUBLIE")).stream()
                .filter(pl -> !pl.getDateFin().isAfter(dernier))
                .forEach(pl -> { pl.setStatut("CLOTURE"); plannings.save(pl); });
        p.setStatut("CLOTUREE");
        p.setCloturePar(rh);
        p.setDateCloture(LocalDateTime.now());
        return periodes.save(p);
    }

    /** Export CSV (séparateur « ; ») des variables d'une période clôturée. */
    @Transactional(readOnly = true)
    public String exporter(Integer idPeriode) {
        StringBuilder sb = new StringBuilder("matricule;nom;code;libelle;quantite;unite;source\n");
        for (VariablePaieModel v : variables.dePeriode(idPeriode)) {
            sb.append(v.getEmploye().getMatricule()).append(';').append(v.getEmploye().getNomComplet().trim()).append(';')
              .append(v.getCode()).append(';').append(v.getLibelle().replace(';', ',')).append(';')
              .append(v.getQuantite().toPlainString()).append(';').append(v.getUnite()).append(';').append(v.getSource()).append('\n');
        }
        return sb.toString();
    }

    private static void cumuler(Map<String, BigDecimal> cumul, Map<String, Object[]> meta, EmployeModel e,
                                String code, String libelle, BigDecimal jours) {
        String cle = e.getId() + "|" + code;
        cumul.merge(cle, jours, BigDecimal::add);
        meta.putIfAbsent(cle, new Object[] { e, code, libelle });
    }

    private void ajouter(PeriodePaieModel p, EmployeModel e, String code, String libelle, BigDecimal quantite,
                         String unite, String source) {
        if (quantite == null || quantite.signum() == 0) return;
        VariablePaieModel v = new VariablePaieModel();
        v.setPeriode(p);
        v.setEmploye(e);
        v.setCode(code.length() > 40 ? code.substring(0, 40) : code);
        v.setLibelle(libelle);
        v.setQuantite(quantite.setScale(2, RoundingMode.HALF_UP));
        v.setUnite(unite);
        v.setSource(source);
        variables.save(v);
    }

    private static int somme(List<JourneeTravailModel> js, java.util.function.ToIntFunction<JourneeTravailModel> f) {
        return js.stream().mapToInt(f).sum();
    }

    private static BigDecimal heures(int minutes) {
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
}
