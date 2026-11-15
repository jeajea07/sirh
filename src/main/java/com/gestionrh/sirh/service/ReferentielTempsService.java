package com.gestionrh.sirh.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.JourFerieModel;
import com.gestionrh.sirh.model.ModeleHoraireModel;
import com.gestionrh.sirh.model.RegimeSalarieModel;
import com.gestionrh.sirh.model.RegimeTravailModel;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.JourFerieRepository;
import com.gestionrh.sirh.repository.ModeleHoraireRepository;
import com.gestionrh.sirh.repository.RegimeSalarieRepository;
import com.gestionrh.sirh.repository.RegimeTravailRepository;

/** Bloc 6 §5 — paramétrage : modèles horaires, régimes, jours fériés (RG-RT). */
@Service
public class ReferentielTempsService {

    private final ModeleHoraireRepository modeles;
    private final RegimeTravailRepository regimes;
    private final RegimeSalarieRepository regimesSalaries;
    private final JourFerieRepository feries;
    private final EmployeRepository employes;
    private final CalculTempsService calcul;
    private final AuditService audit;

    public ReferentielTempsService(ModeleHoraireRepository modeles, RegimeTravailRepository regimes,
                                   RegimeSalarieRepository regimesSalaries, JourFerieRepository feries,
                                   EmployeRepository employes, CalculTempsService calcul, AuditService audit) {
        this.modeles = modeles;
        this.regimes = regimes;
        this.regimesSalaries = regimesSalaries;
        this.feries = feries;
        this.employes = employes;
        this.calcul = calcul;
        this.audit = audit;
    }

    @Transactional(readOnly = true) public List<ModeleHoraireModel> modeles() { return modeles.findAllByOrderByCodeAsc(); }
    @Transactional(readOnly = true) public List<ModeleHoraireModel> modelesActifs() { return modeles.findByActifTrueOrderByHeureDebutAsc(); }
    @Transactional(readOnly = true) public List<RegimeTravailModel> regimes() { return regimes.findByActifTrueOrderByLibelleAsc(); }
    @Transactional(readOnly = true) public List<JourFerieModel> feries() { return feries.findAllByOrderByJourDesc(); }

    @Transactional(readOnly = true)
    public RegimeSalarieModel regimeEnCours(Integer idEmploye) {
        return regimesSalaries.enCours(idEmploye).orElse(null);
    }

    @Transactional
    public void creerModele(String code, String libelle, LocalTime debut, LocalTime fin, int pause,
                            int toleranceRetard, int toleranceDepart, boolean nuit) {
        if (code == null || code.isBlank() || libelle == null || libelle.isBlank() || debut == null || fin == null) {
            throw new Bloc4Exception("Code, libellé, début et fin sont obligatoires.");
        }
        if (!nuit && !fin.isAfter(debut)) throw new Bloc4Exception("La fin doit suivre le début, sauf horaire de nuit (RG-RT05).");
        if (pause < 0 || toleranceRetard < 0 || toleranceDepart < 0) throw new Bloc4Exception("Les durées ne peuvent pas être négatives.");
        String c = code.trim().toUpperCase().replaceAll("[^A-Z0-9_]", "_");
        if (modeles.findByCode(c).isPresent()) throw new Bloc4Exception("Ce code existe déjà : un modèle utilisé ne se modifie pas, créez-en un autre (RG-RT06).");
        ModeleHoraireModel m = new ModeleHoraireModel();
        m.setCode(c);
        m.setLibelle(libelle.trim());
        m.setHeureDebut(debut);
        m.setHeureFin(fin);
        m.setDureePauseMinutes(pause);
        m.setToleranceRetardMinutes(toleranceRetard);
        m.setToleranceDepartMinutes(toleranceDepart);
        m.setEstNuit(nuit);
        modeles.save(m);
    }

    @Transactional
    public void ajouterFerie(LocalDate jour, String libelle) {
        if (jour == null || libelle == null || libelle.isBlank()) throw new Bloc4Exception("La date et le libellé sont obligatoires.");
        if (feries.existsByJour(jour)) throw new Bloc4Exception("Ce jour est déjà férié.");
        if (calcul.periodeCloturee(jour)) throw new Bloc4Exception("La période est clôturée.");
        JourFerieModel f = new JourFerieModel();
        f.setJour(jour);
        f.setLibelle(libelle.trim());
        feries.save(f);
        if (jour.isBefore(LocalDate.now())) {
            employes.findByActifTrueOrderByNomAsc().forEach(e -> calcul.calculer(e, jour));
        }
    }

    /** RG-RT02 / RG-RT04 : changement daté, la période précédente est clôturée, rien n'est écrasé. */
    @Transactional
    public void changerRegime(Integer idEmploye, Integer idRegime, LocalDate dateEffet, EmployeModel rh) {
        EmployeModel e = employes.findById(idEmploye).orElseThrow(() -> new Bloc4Exception("Salarié introuvable."));
        RegimeTravailModel r = regimes.findById(idRegime == null ? -1 : idRegime)
                .orElseThrow(() -> new Bloc4Exception("Régime introuvable."));
        if (dateEffet == null) throw new Bloc4Exception("La date d'effet est obligatoire.");
        if (calcul.periodeCloturee(dateEffet)) throw new Bloc4Exception("La période de cette date est clôturée (RG-RT04).");
        RegimeSalarieModel courant = regimesSalaries.enCours(idEmploye).orElse(null);
        String ancien = null;
        if (courant != null) {
            if (!dateEffet.isAfter(courant.getDateDebut())) {
                throw new Bloc4Exception("La date d'effet doit suivre le début du régime en cours.");
            }
            ancien = courant.getRegime().getLibelle();
            courant.setDateFin(dateEffet.minusDays(1));
            regimesSalaries.saveAndFlush(courant);
        }
        RegimeSalarieModel n = new RegimeSalarieModel();
        n.setEmploye(e);
        n.setRegime(r);
        n.setDateDebut(dateEffet);
        regimesSalaries.save(n);
        audit.tracer("SALARIE", idEmploye, "REGIME_TRAVAIL", ancien, r.getLibelle(), "À partir du " + dateEffet, rh);
        calcul.recalculer(e, dateEffet, LocalDate.now().minusDays(1));
    }
}
