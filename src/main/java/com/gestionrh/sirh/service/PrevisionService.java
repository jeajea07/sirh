package com.gestionrh.sirh.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.ModeleHoraireModel;
import com.gestionrh.sirh.model.PlanningLigneModel;
import com.gestionrh.sirh.model.RegimeTravailModel;
import com.gestionrh.sirh.repository.JourFerieRepository;
import com.gestionrh.sirh.repository.PlanningLigneRepository;
import com.gestionrh.sirh.repository.RegimeSalarieRepository;

/**
 * Bloc 6 — ce qui est prévu pour un salarié un jour donné : le planning publié s'il existe,
 * sinon le régime de travail (jours travaillés + modèle horaire par défaut).
 */
@Service
public class PrevisionService {

    /** Prévision d'un jour. {@code modele} est null pour un jour non travaillé. */
    public static final class Prevision {
        private final String typeJour;
        private final ModeleHoraireModel modele;
        private final int minutesPrevues;
        private final int minutesSupPrevues;
        private final String source;
        private final Integer idLigne;
        private final RegimeTravailModel regime;
        public Prevision(String typeJour, ModeleHoraireModel modele, int minutesPrevues, int minutesSupPrevues, String source, Integer idLigne, RegimeTravailModel regime) {
            this.typeJour = typeJour;
            this.modele = modele;
            this.minutesPrevues = minutesPrevues;
            this.minutesSupPrevues = minutesSupPrevues;
            this.source = source;
            this.idLigne = idLigne;
            this.regime = regime;
        }
        public String typeJour() { return typeJour; }
        public ModeleHoraireModel modele() { return modele; }
        public int minutesPrevues() { return minutesPrevues; }
        public int minutesSupPrevues() { return minutesSupPrevues; }
        public String source() { return source; }
        public Integer idLigne() { return idLigne; }
        public RegimeTravailModel regime() { return regime; }

        public boolean travaille() { return "TRAVAIL".equals(typeJour); }
        // Accesseurs JavaBeans pour les JSP (EL).
        public String getTypeJour() { return typeJour; }
        public ModeleHoraireModel getModele() { return modele; }
        public String getDureeFr() { return com.gestionrh.sirh.model.JourneeTravailModel.hm(minutesPrevues); }
        public String getSource() { return source; }
        public String getLibelle() {
            return switch (typeJour) {
                case "TRAVAIL" -> modele.getPlageLibelle();
                case "FERIE" -> "Férié";
                case "CONGE" -> "Congé";
                case "ABSENCE" -> "Absence";
                default -> "Repos";
            };
        }
    }

    private final RegimeSalarieRepository regimes;
    private final JourFerieRepository feries;
    private final PlanningLigneRepository lignes;
    private final com.gestionrh.sirh.repository.DemandeCongeRepository conges;
    private final com.gestionrh.sirh.repository.AbsenceRepository absences;

    public PrevisionService(RegimeSalarieRepository regimes, JourFerieRepository feries, PlanningLigneRepository lignes,
                            com.gestionrh.sirh.repository.DemandeCongeRepository conges,
                            com.gestionrh.sirh.repository.AbsenceRepository absences) {
        this.conges = conges;
        this.absences = absences;
        this.regimes = regimes;
        this.feries = feries;
        this.lignes = lignes;
    }

    @Transactional(readOnly = true)
    public RegimeTravailModel regimeAu(Integer idEmploye, LocalDate jour) {
        return regimes.auJour(idEmploye, jour).stream().findFirst().map(r -> r.getRegime()).orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean estFerie(LocalDate jour) {
        return feries.existsByJour(jour);
    }

    @Transactional(readOnly = true)
    public Prevision prevision(EmployeModel e, LocalDate jour) {
        RegimeTravailModel regime = regimeAu(e.getId(), jour);
        PlanningLigneModel ligne = lignes.publieeAuJour(e.getId(), jour).stream().findFirst().orElse(null);
        // Une case CONGE/ABSENCE du planning ne vaut que si le congé existe toujours : le calcul le vérifie dans le Bloc 7.
        if (ligne != null && !"CONGE".equals(ligne.getTypeJour()) && !"ABSENCE".equals(ligne.getTypeJour())) {
            return new Prevision(ligne.getTypeJour(), ligne.getModele(), ligne.getMinutesPrevues(),
                    ligne.getMinutesSupPrevues(), "PLANNING", ligne.getId(), regime);
        }
        Prevision p = parDefaut(regime, jour);
        return ligne == null ? p
                : new Prevision(p.typeJour(), p.modele(), p.minutesPrevues(), 0, "PLANNING", ligne.getId(), regime);
    }

    /** Prévision affichée au salarié : un congé ou une absence validés priment sur le planning (RG-PL04). */
    @Transactional(readOnly = true)
    public Prevision previsionAffichee(EmployeModel e, LocalDate jour) {
        Prevision p = prevision(e, jour);
        if (!conges.valideesEntre(e.getId(), jour, jour).isEmpty()) {
            return new Prevision("CONGE", null, 0, 0, p.source(), p.idLigne(), p.regime());
        }
        if (!absences.activesEntre(e.getId(), jour, jour).isEmpty()) {
            return new Prevision("ABSENCE", null, 0, 0, p.source(), p.idLigne(), p.regime());
        }
        return p;
    }

    /** Case par défaut d'un jour selon le régime : utilisée pour pré-remplir un planning et en l'absence de planning. */
    @Transactional(readOnly = true)
    public Prevision parDefaut(RegimeTravailModel regime, LocalDate jour) {
        if (regime == null) {
            return new Prevision("REPOS", null, 0, 0, "REGIME", null, null);
        }
        if (estFerie(jour)) {
            return new Prevision("FERIE", null, 0, 0, "REGIME", null, regime);
        }
        if (!regime.travaille(jour.getDayOfWeek().getValue())) {
            return new Prevision("REPOS", null, 0, 0, "REGIME", null, regime);
        }
        ModeleHoraireModel m = regime.getModeleDefaut();
        return new Prevision("TRAVAIL", m, m.getDureeMinutes(), 0, "REGIME", null, regime);
    }
}
