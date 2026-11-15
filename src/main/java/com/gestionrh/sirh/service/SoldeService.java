package com.gestionrh.sirh.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.AbsenceModel;
import com.gestionrh.sirh.model.DemandeCongeModel;
import com.gestionrh.sirh.model.DroitCongeModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.MouvementSoldeModel;
import com.gestionrh.sirh.repository.AbsenceRepository;
import com.gestionrh.sirh.repository.DemandeCongeRepository;
import com.gestionrh.sirh.repository.DroitCongeRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.MouvementSoldeRepository;

/**
 * Bloc 7 §6 — droits et soldes. Le solde n'est jamais saisi : il est la somme des mouvements,
 * diminuée des jours réservés par les demandes en cours (RG-SC03, RG-SC05).
 */
@Service
public class SoldeService {

    public static final List<String> TYPES_SOLDE = List.of("CONGE_PAYE", "PERMISSION");

    /** Solde d'un type pour un salarié. */
    public static final class Solde {
        private final String typeSolde;
        private final BigDecimal acquis;
        private final BigDecimal pris;
        private final BigDecimal reserve;
        public Solde(String typeSolde, BigDecimal acquis, BigDecimal pris, BigDecimal reserve) {
            this.typeSolde = typeSolde;
            this.acquis = acquis;
            this.pris = pris;
            this.reserve = reserve;
        }
        public String typeSolde() { return typeSolde; }
        public BigDecimal acquis() { return acquis; }
        public BigDecimal pris() { return pris; }
        public BigDecimal reserve() { return reserve; }

        public BigDecimal getDisponible() { return acquis.subtract(pris).subtract(reserve); }
        public String getTypeSolde() { return typeSolde; }
        public String getLibelle() { return "CONGE_PAYE".equals(typeSolde) ? "Congés payés" : "Permissions exceptionnelles"; }
        public String getAcquisFr() { return fr(acquis); }
        public String getPrisFr() { return fr(pris); }
        public String getReserveFr() { return fr(reserve); }
        public String getDisponibleFr() { return fr(getDisponible()); }
        private static String fr(BigDecimal v) { return v.stripTrailingZeros().toPlainString() + " j"; }
    }

    private final MouvementSoldeRepository mouvements;
    private final DroitCongeRepository droits;
    private final DemandeCongeRepository demandes;
    private final AbsenceRepository absences;
    private final EmployeRepository employes;
    private final TempsParametres parametres;

    public SoldeService(MouvementSoldeRepository mouvements, DroitCongeRepository droits, DemandeCongeRepository demandes,
                        AbsenceRepository absences, EmployeRepository employes, TempsParametres parametres) {
        this.mouvements = mouvements;
        this.droits = droits;
        this.demandes = demandes;
        this.absences = absences;
        this.employes = employes;
        this.parametres = parametres;
    }

    @Transactional(readOnly = true)
    public Solde solde(Integer idEmploye, String typeSolde) {
        BigDecimal acquis = mouvements.somme(idEmploye, typeSolde, List.of("CREDIT", "AJUSTEMENT", "EXPIRATION"));
        BigDecimal pris = mouvements.somme(idEmploye, typeSolde, List.of("DEBIT")).negate();
        return new Solde(typeSolde, acquis, pris, demandes.reserve(idEmploye, typeSolde));
    }

    @Transactional(readOnly = true)
    public List<Solde> soldes(Integer idEmploye) {
        return TYPES_SOLDE.stream().map(t -> solde(idEmploye, t)).toList();
    }

    @Transactional(readOnly = true)
    public List<MouvementSoldeModel> historique(Integer idEmploye) {
        return mouvements.historique(idEmploye);
    }

    /** RG-SC04 : ajustement réservé au RH, motivé, tracé comme mouvement. */
    @Transactional
    public void ajuster(Integer idEmploye, String typeSolde, BigDecimal jours, String motif, EmployeModel rh) {
        if (!TYPES_SOLDE.contains(typeSolde)) throw new Bloc4Exception("Type de solde inconnu.");
        if (jours == null || jours.signum() == 0) throw new Bloc4Exception("Indiquez un nombre de jours non nul (+ ou −).");
        if (motif == null || motif.isBlank()) throw new Bloc4Exception("Le motif de l'ajustement est obligatoire (RG-SC04).");
        if (idEmploye.equals(rh.getId())) throw new Bloc4Exception("Vous ne pouvez pas ajuster votre propre solde.");
        EmployeModel e = employes.findById(idEmploye).orElseThrow(() -> new Bloc4Exception("Salarié introuvable."));
        MouvementSoldeModel m = new MouvementSoldeModel();
        m.setEmploye(e);
        m.setTypeSolde(typeSolde);
        m.setTypeMouvement("AJUSTEMENT");
        m.setJours(jours);
        m.setAuteur(rh);
        m.setMotif(motif.trim());
        mouvements.save(m);
    }

    /** Débit définitif d'un congé pris (une seule fois par demande). */
    @Transactional
    public void debiter(DemandeCongeModel d, BigDecimal jours) {
        String solde = d.getType().getTypeSolde();
        if (solde == null || jours == null || jours.signum() <= 0
                || mouvements.existsByIdDemandeCongeAndTypeMouvement(d.getId(), "DEBIT")) {
            return;
        }
        MouvementSoldeModel m = new MouvementSoldeModel();
        m.setEmploye(d.getEmploye());
        m.setTypeSolde(solde);
        m.setTypeMouvement("DEBIT");
        m.setJours(jours.negate());
        m.setIdDemandeConge(d.getId());
        m.setMotif("Congé " + d.getReference());
        mouvements.save(m);
    }

    /**
     * Bloc 7 §6.3 : acquisition mensuelle des congés payés, au prorata du service effectif (RG-SC01, RG-SC02).
     * Rejouable : une seule acquisition par salarié et par mois.
     */
    @Transactional
    public int acquisitionMensuelle(int annee, int mois) {
        LocalDate premier = LocalDate.of(annee, mois, 1);
        LocalDate dernier = premier.plusMonths(1).minusDays(1);
        int n = 0;
        for (EmployeModel e : employes.findAll()) {
            if (e.getDateEmbauche() == null || e.getDateEmbauche().isAfter(dernier)) continue;
            if (e.getDateSortie() != null && e.getDateSortie().isBefore(premier)) continue;
            if (droits.existsByEmploye_IdAndTypeSoldeAndPeriodeReferenceAndOrigine(e.getId(), "CONGE_PAYE", premier, "ACQUISITION_MENSUELLE")
                    || droits.repriseCouvre(e.getId(), premier)) {
                continue;
            }
            LocalDate debut = e.getDateEmbauche().isAfter(premier) ? e.getDateEmbauche() : premier;
            LocalDate fin = e.getDateSortie() != null && e.getDateSortie().isBefore(dernier) ? e.getDateSortie() : dernier;
            long joursService = ChronoUnit.DAYS.between(debut, fin) + 1 - joursNonAssimiles(e, debut, fin);
            if (joursService <= 0) continue;
            BigDecimal jours = parametres.acquisitionMensuelleJours
                    .multiply(BigDecimal.valueOf(joursService))
                    .divide(BigDecimal.valueOf(premier.lengthOfMonth()), 2, RoundingMode.HALF_UP);
            crediter(e, "CONGE_PAYE", premier, jours, "ACQUISITION_MENSUELLE", "Acquisition " + mois + "/" + annee);
            n++;
        }
        return n;
    }

    /** RG-SC11 : permissions remises à zéro chaque année puis réattribuées. */
    @Transactional
    public int attributionAnnuelle(int annee) {
        LocalDate periode = LocalDate.of(annee, 1, 1);
        int n = 0;
        for (EmployeModel e : employes.findByActifTrueOrderByNomAsc()) {
            if (droits.existsByEmploye_IdAndTypeSoldeAndPeriodeReferenceAndOrigine(e.getId(), "PERMISSION", periode, "ATTRIBUTION_ANNUELLE")) {
                continue;
            }
            Solde reste = solde(e.getId(), "PERMISSION");
            BigDecimal restant = reste.acquis().subtract(reste.pris());
            if (restant.signum() > 0) {
                MouvementSoldeModel exp = new MouvementSoldeModel();
                exp.setEmploye(e);
                exp.setTypeSolde("PERMISSION");
                exp.setTypeMouvement("EXPIRATION");
                exp.setJours(restant.negate());
                exp.setMotif("Permissions non utilisées de l'année " + (annee - 1));
                mouvements.save(exp);
            }
            crediter(e, "PERMISSION", periode, parametres.permissionsAnnuellesJours, "ATTRIBUTION_ANNUELLE",
                    "Allocation annuelle " + annee);
            n++;
        }
        return n;
    }

    private void crediter(EmployeModel e, String solde, LocalDate periode, BigDecimal jours, String origine, String motif) {
        if (jours.signum() <= 0) return;
        DroitCongeModel d = new DroitCongeModel();
        d.setEmploye(e);
        d.setTypeSolde(solde);
        d.setPeriodeReference(periode);
        d.setJours(jours);
        d.setOrigine(origine);
        d = droits.save(d);
        MouvementSoldeModel m = new MouvementSoldeModel();
        m.setEmploye(e);
        m.setTypeSolde(solde);
        m.setTypeMouvement("CREDIT");
        m.setJours(jours);
        m.setIdDroitConge(d.getId());
        m.setMotif(motif);
        mouvements.save(m);
    }

    /** Jours d'absence ou de congé non assimilés à du service effectif (sans solde, injustifiée…). */
    private long joursNonAssimiles(EmployeModel e, LocalDate debut, LocalDate fin) {
        long n = 0;
        for (DemandeCongeModel d : demandes.valideesEntre(e.getId(), debut, fin)) {
            if (!d.getType().isAssimileTravail()) n += chevauchement(d.getDateDebut(), d.getDateFin(), debut, fin);
        }
        for (AbsenceModel a : absences.activesEntre(e.getId(), debut, fin)) {
            if (!a.getType().isAssimileTravail()) n += chevauchement(a.getDateDebut(), a.getDateFin(), debut, fin);
        }
        return n;
    }

    static long chevauchement(LocalDate d1, LocalDate f1, LocalDate d2, LocalDate f2) {
        LocalDate d = d1.isAfter(d2) ? d1 : d2;
        LocalDate f = f1.isBefore(f2) ? f1 : f2;
        return f.isBefore(d) ? 0 : ChronoUnit.DAYS.between(d, f) + 1;
    }
}
