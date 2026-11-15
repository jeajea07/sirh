package com.gestionrh.sirh.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.AbsenceModel;
import com.gestionrh.sirh.model.AnomaliePointageModel;
import com.gestionrh.sirh.model.DemandeCongeModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.JourneeTravailModel;
import com.gestionrh.sirh.model.ModeleHoraireModel;
import com.gestionrh.sirh.model.PointageModel;
import com.gestionrh.sirh.repository.AbsenceRepository;
import com.gestionrh.sirh.repository.AnomaliePointageRepository;
import com.gestionrh.sirh.repository.DemandeCongeRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.JourneeTravailRepository;
import com.gestionrh.sirh.repository.PeriodePaieRepository;
import com.gestionrh.sirh.repository.PointageRepository;
import com.gestionrh.sirh.util.Formats;

/**
 * Bloc 6 §8 — calcul de la journée de travail : le prévu (planning ou régime) est rapproché du réalisé
 * (pointages valides) et des congés/absences validés du Bloc 7. Les anomalies sont créées ou résolues.
 * Le temps travaillé vient toujours des pointages, jamais du planning (RG-TT01).
 */
@Service
public class CalculTempsService {

    /** Anomalies qui, une fois acceptées ou classées, neutralisent la pénalité correspondante. */
    private static final Set<String> TRAITEES = Set.of("RESOLUE", "CLASSEE");

    private final EmployeRepository employes;
    private final PrevisionService previsions;
    private final PointageRepository pointages;
    private final JourneeTravailRepository journees;
    private final AnomaliePointageRepository anomalies;
    private final DemandeCongeRepository conges;
    private final AbsenceRepository absences;
    private final PeriodePaieRepository periodes;
    private final NotificationService notifications;
    private final JdbcTemplate jdbc;

    public CalculTempsService(EmployeRepository employes, PrevisionService previsions, PointageRepository pointages,
                              JourneeTravailRepository journees, AnomaliePointageRepository anomalies,
                              DemandeCongeRepository conges, AbsenceRepository absences,
                              PeriodePaieRepository periodes, NotificationService notifications, JdbcTemplate jdbc) {
        this.employes = employes;
        this.previsions = previsions;
        this.pointages = pointages;
        this.journees = journees;
        this.anomalies = anomalies;
        this.conges = conges;
        this.absences = absences;
        this.periodes = periodes;
        this.notifications = notifications;
        this.jdbc = jdbc;
    }

    /** Date de mise en service du pointage : aucun calcul avant (pas de pointage possible avant le Bloc 6). */
    public LocalDate dateMiseEnService() {
        List<String> v = jdbc.queryForList(
                "select valeur from parametre_temps where cle = 'DATE_MISE_EN_SERVICE'", String.class);
        return v.isEmpty() ? LocalDate.now() : LocalDate.parse(v.get(0));
    }

    public boolean periodeCloturee(LocalDate jour) {
        return periodes.existsByAnneeAndMoisAndStatut(jour.getYear(), jour.getMonthValue(), "CLOTUREE");
    }

    /** Traitement de nuit : tous les salariés actifs pour un jour donné. */
    @Transactional
    public int calculerJour(LocalDate jour) {
        int n = 0;
        for (EmployeModel e : employes.findByActifTrueOrderByNomAsc()) {
            if (calculer(e, jour) != null) n++;
        }
        return n;
    }

    /** RG-TT07 : recalcul d'une plage (jours passés, période non clôturée). */
    @Transactional
    public void recalculer(EmployeModel e, LocalDate debut, LocalDate fin) {
        LocalDate hier = LocalDate.now().minusDays(1);
        for (LocalDate d = debut; !d.isAfter(fin) && !d.isAfter(hier); d = d.plusDays(1)) {
            calculer(e, d);
        }
    }

    @Transactional
    public JourneeTravailModel calculer(EmployeModel e, LocalDate jour) {
        if (!jour.isBefore(LocalDate.now()) || jour.isBefore(dateMiseEnService()) || periodeCloturee(jour)) {
            return null; // journée en cours, avant la mise en service, ou période close (RG-VP02)
        }
        if (e.getDateEmbauche() != null && jour.isBefore(e.getDateEmbauche())) return null;
        if (e.getDateSortie() != null && jour.isAfter(e.getDateSortie())) return null;
        PrevisionService.Prevision p = previsions.prevision(e, jour);
        if (p.regime() == null) return null;

        JourneeTravailModel j = journees.findByEmploye_IdAndJour(e.getId(), jour).orElseGet(JourneeTravailModel::new);
        if ("CLOTUREE".equals(j.getStatut())) return j;
        reinitialiser(j, e, jour, p);

        // --- Bloc 7 : congés et absences validés (RG-TT06, RG-AB09) ---
        double partConge = partEnConge(e, jour);
        List<AbsenceModel> abs = absences.activesEntre(e.getId(), jour, jour);
        boolean absenceInjustifiee = abs.stream().anyMatch(a -> "INJUSTIFIEE".equals(a.getStatut()));
        boolean absenceCouverte = abs.stream().anyMatch(a -> !"INJUSTIFIEE".equals(a.getStatut()));
        List<PointageModel> liste = pointagesDuJour(e, jour, p.modele());
        Map<String, String> detectees = new LinkedHashMap<>();

        if ("SUSPENDU".equals(e.getStatutAdministratif()) || partConge >= 1 || absenceCouverte) {
            j.setTypeJour(partConge >= 1 ? "CONGE" : "ABSENCE");
            j.setStatut("ABSENCE_JUSTIFIEE");
            return terminer(j, e, jour, detectees);
        }
        if (absenceInjustifiee && p.travaille()) {
            j.setMinutesAbsence(p.minutesPrevues());
            j.setStatut("CALCULEE");
            return terminer(j, e, jour, detectees);
        }
        int prevues = (int) Math.round(p.minutesPrevues() * (1 - partConge));
        j.setMinutesPrevues(prevues);

        if (!p.regime().isPointageObligatoire()) {           // RG-PT07 : pas de pointage, pas d'anomalie
            j.setMinutesTravaillees(prevues);
            j.setStatut(prevues > 0 ? "CALCULEE" : "REPOS");
            return terminer(j, e, jour, detectees);
        }

        // --- Appariement ENTREE → SORTIE ---
        List<LocalDateTime[]> paires = new ArrayList<>();
        LocalDateTime entree = null;
        for (PointageModel pt : liste) {
            if ("ENTREE".equals(pt.getSens())) {
                if (entree != null) detectees.put("POINTAGE_INCOHERENT", "Deux entrées successives sans sortie.");
                entree = pt.getHorodatage();
            } else if (entree == null) {
                detectees.put(paires.isEmpty() ? "POINTAGE_MANQUANT_ENTREE" : "POINTAGE_INCOHERENT",
                        "Sortie à " + pt.getHeureFr() + " sans entrée.");
            } else {
                paires.add(new LocalDateTime[] { entree, pt.getHorodatage() });
                entree = null;
            }
        }
        if (entree != null) {
            detectees.put("POINTAGE_MANQUANT_SORTIE", "Entrée à " + entree.toLocalTime().withNano(0) + " sans sortie.");
        }
        int presence = paires.stream().mapToInt(x -> (int) ChronoUnit.MINUTES.between(x[0], x[1])).sum();
        // RG-TT02 : sans pointage de pause, la pause prévue est déduite d'une présence continue.
        if (p.modele() != null && paires.size() == 1 && presence > 6 * 60) {
            presence = Math.max(0, presence - p.modele().getDureePauseMinutes());
        }
        j.setMinutesPresence(presence);
        Map<String, String> existantes = statutsExistants(e, jour);

        if (p.travaille()) {
            ModeleHoraireModel m = p.modele();
            if (liste.isEmpty()) {
                if (prevues > 0) {
                    detectees.put("ABSENCE_NON_JUSTIFIEE", "Aucun pointage pour " + JourneeTravailModel.hm(prevues) + " prévues.");
                    if (!traitee(existantes.get("ABSENCE_NON_JUSTIFIEE"))) j.setMinutesAbsence(prevues);
                }
            } else {
                LocalDateTime debutPrevu = jour.atTime(m.getHeureDebut());
                LocalDateTime finPrevue = m.isEstNuit() ? jour.plusDays(1).atTime(m.getHeureFin()) : jour.atTime(m.getHeureFin());
                LocalDateTime premiere = liste.stream().filter(x -> "ENTREE".equals(x.getSens()))
                        .map(PointageModel::getHorodatage).findFirst().orElse(null);
                LocalDateTime derniere = liste.stream().filter(x -> "SORTIE".equals(x.getSens()))
                        .map(PointageModel::getHorodatage).reduce((a, b) -> b).orElse(null);
                if (partConge == 0 && premiere != null) {                         // RG-TT03
                    int retard = (int) ChronoUnit.MINUTES.between(debutPrevu, premiere);
                    if (retard > m.getToleranceRetardMinutes()) {
                        detectees.put("RETARD", "Arrivée à " + premiere.toLocalTime().withNano(0) + " pour " + m.getHeureDebut() + " (" + retard + " min).");
                        if (!traitee(existantes.get("RETARD"))) j.setMinutesRetard(retard);
                    }
                }
                if (partConge == 0 && derniere != null) {
                    int avance = (int) ChronoUnit.MINUTES.between(derniere, finPrevue);
                    if (avance > m.getToleranceDepartMinutes()) {
                        detectees.put("DEPART_ANTICIPE", "Départ à " + derniere.toLocalTime().withNano(0) + " pour " + m.getHeureFin() + " (" + avance + " min).");
                        if (!traitee(existantes.get("DEPART_ANTICIPE"))) j.setMinutesDepartAnticipe(avance);
                    }
                }
                // RG-HS01 / RG-TT04 : une heure au-delà du prévu n'est retenue que si elle est autorisée.
                boolean autoriseePosteriori = "RESOLUE".equals(existantes.get("HEURES_NON_AUTORISEES"));
                int autorisees = autoriseePosteriori ? Integer.MAX_VALUE : p.minutesSupPrevues();
                int excedent = presence - prevues;
                if (excedent > autorisees + m.getToleranceDepartMinutes()) {
                    detectees.put("HEURES_NON_AUTORISEES", JourneeTravailModel.hm(excedent) + " au-delà du prévu sans autorisation.");
                }
                int sup = p.regime().isHeuresSupAutorisees() ? Math.max(0, Math.min(excedent, autorisees)) : 0;
                j.setMinutesSup(sup);
                j.setMinutesTravaillees(Math.min(presence, prevues) + sup);
            }
        } else if (!liste.isEmpty()) {                                           // repos ou férié pointé
            if ("RESOLUE".equals(existantes.get("POINTAGE_HORS_PLANNING")) && p.regime().isHeuresSupAutorisees()) {
                j.setMinutesTravaillees(presence);
                j.setMinutesSup(presence);
            } else {
                detectees.put("POINTAGE_HORS_PLANNING", "Pointage un jour de " + ("FERIE".equals(p.typeJour()) ? "férié" : "repos") + ".");
            }
        }

        j.setMinutesNuit(minutesDeNuit(paires));
        if (jour.getDayOfWeek().getValue() == 7) j.setMinutesDimanche(j.getMinutesTravaillees());
        if (previsions.estFerie(jour)) j.setMinutesFerie(j.getMinutesTravaillees());
        j.setStatut(p.travaille() || !liste.isEmpty() ? "CALCULEE" : "REPOS");
        return terminer(j, e, jour, detectees);
    }

    // ------------------------------------------------------------ utilitaires

    /** Anomalie acceptée ou classée : la pénalité n'est pas retenue (Set.of refuse contains(null)). */
    private static boolean traitee(String statut) {
        return statut != null && TRAITEES.contains(statut);
    }

    private void reinitialiser(JourneeTravailModel j, EmployeModel e, LocalDate jour, PrevisionService.Prevision p) {
        j.setEmploye(e);
        j.setJour(jour);
        j.setIdPlanningLigne(p.idLigne());
        j.setSourcePrevision(p.source());
        j.setTypeJour(p.typeJour());
        j.setMinutesPrevues(p.minutesPrevues());
        j.setMinutesPresence(0);
        j.setMinutesTravaillees(0);
        j.setMinutesRetard(0);
        j.setMinutesDepartAnticipe(0);
        j.setMinutesSup(0);
        j.setMinutesNuit(0);
        j.setMinutesDimanche(0);
        j.setMinutesFerie(0);
        j.setMinutesAbsence(0);
        j.setDateCalcul(LocalDateTime.now());
    }

    /** Part de la journée couverte par un congé validé (1 = journée entière, 0,5 = demi-journée). */
    private double partEnConge(EmployeModel e, LocalDate jour) {
        double part = 0;
        for (DemandeCongeModel d : conges.valideesEntre(e.getId(), jour, jour)) {
            double p = 1;
            if (jour.equals(d.getDateDebut()) && "APRES_MIDI".equals(d.getDemiJourneeDebut())) p -= 0.5;
            if (jour.equals(d.getDateFin()) && "MATIN".equals(d.getDemiJourneeFin())) p -= 0.5;
            part += p;
        }
        return Math.min(1, part);
    }

    /** Fenêtre de pointage : la journée civile, ou midi → midi pour un horaire de nuit (RG-TT05). */
    private List<PointageModel> pointagesDuJour(EmployeModel e, LocalDate jour, ModeleHoraireModel m) {
        boolean nuit = m != null && m.isEstNuit();
        LocalDateTime debut = nuit ? jour.atTime(12, 0) : jour.atStartOfDay();
        return pointages.valides(e.getId(), debut, debut.plusDays(1));
    }

    private static int minutesDeNuit(List<LocalDateTime[]> paires) {
        int total = 0;
        for (LocalDateTime[] p : paires) {
            for (LocalDateTime t = p[0]; t.isBefore(p[1]); t = t.plusMinutes(1)) {
                LocalTime h = t.toLocalTime();
                if (!h.isBefore(LocalTime.of(22, 0)) || h.isBefore(LocalTime.of(5, 0))) total++;
            }
        }
        return total;
    }

    private Map<String, String> statutsExistants(EmployeModel e, LocalDate jour) {
        Map<String, String> m = new LinkedHashMap<>();
        for (AnomaliePointageModel a : anomalies.findByEmploye_IdAndJour(e.getId(), jour)) {
            m.put(a.getTypeAnomalie(), a.getStatut());
        }
        return m;
    }

    /** Rapproche les anomalies détectées des anomalies existantes, puis enregistre la journée. */
    private JourneeTravailModel terminer(JourneeTravailModel j, EmployeModel e, LocalDate jour, Map<String, String> detectees) {
        List<AnomaliePointageModel> existantes = anomalies.findByEmploye_IdAndJour(e.getId(), jour);
        boolean aTraiter = false;
        for (AnomaliePointageModel a : existantes) {
            if (a.isATraiter() && !detectees.containsKey(a.getTypeAnomalie())) {
                a.setStatut("RESOLUE");
                a.setDateTraitement(LocalDateTime.now());
                a.setCommentaireTraitement("Résolue au recalcul de la journée.");
                anomalies.save(a);
            } else if (a.isATraiter()) {
                a.setDetail(detectees.get(a.getTypeAnomalie()));
                anomalies.save(a);
                aTraiter = true;
            }
        }
        for (Map.Entry<String, String> d : detectees.entrySet()) {
            boolean connue = existantes.stream().anyMatch(a -> a.getTypeAnomalie().equals(d.getKey()));
            if (connue) continue;
            AnomaliePointageModel a = new AnomaliePointageModel();
            a.setEmploye(e);
            a.setJour(jour);
            a.setTypeAnomalie(d.getKey());
            a.setDetail(d.getValue());
            anomalies.save(a);
            aTraiter = true;
            notifications.notifier(e.getId(), "ANOMALIE", a.getId(), a.getTypeLibelle() + " le " + Formats.date(jour)
                    + " : justifiez-la depuis « Pointage » (RG-AN03).");
        }
        if (aTraiter) j.setStatut("A_REGULARISER");
        return journees.save(j);
    }
}
