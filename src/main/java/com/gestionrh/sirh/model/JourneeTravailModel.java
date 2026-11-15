package com.gestionrh.sirh.model;

import com.gestionrh.sirh.util.Formats;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Journée calculée : prévu (planning ou régime) rapproché du réalisé (pointages) — Bloc 6 §8. */
@Entity
@Table(name = "journee_travail")
public class JourneeTravailModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_journee_travail")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_employe", nullable = false)
    private EmployeModel employe;

    @Column(name = "jour", nullable = false)
    private LocalDate jour;

    @Column(name = "id_planning_ligne")
    private Integer idPlanningLigne;

    @Column(name = "source_prevision", nullable = false)
    private String sourcePrevision;

    @Column(name = "type_jour", nullable = false)
    private String typeJour;

    @Column(name = "minutes_prevues", nullable = false)
    private int minutesPrevues;

    @Column(name = "minutes_presence", nullable = false)
    private int minutesPresence;

    @Column(name = "minutes_travaillees", nullable = false)
    private int minutesTravaillees;

    @Column(name = "minutes_retard", nullable = false)
    private int minutesRetard;

    @Column(name = "minutes_depart_anticipe", nullable = false)
    private int minutesDepartAnticipe;

    @Column(name = "minutes_sup", nullable = false)
    private int minutesSup;

    @Column(name = "minutes_nuit", nullable = false)
    private int minutesNuit;

    @Column(name = "minutes_dimanche", nullable = false)
    private int minutesDimanche;

    @Column(name = "minutes_ferie", nullable = false)
    private int minutesFerie;

    @Column(name = "minutes_absence", nullable = false)
    private int minutesAbsence;

    @Column(name = "statut", nullable = false)
    private String statut;

    @Column(name = "date_calcul", nullable = false)
    private LocalDateTime dateCalcul = java.time.LocalDateTime.now();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public EmployeModel getEmploye() { return employe; }
    public void setEmploye(EmployeModel employe) { this.employe = employe; }

    public LocalDate getJour() { return jour; }
    public void setJour(LocalDate jour) { this.jour = jour; }

    public Integer getIdPlanningLigne() { return idPlanningLigne; }
    public void setIdPlanningLigne(Integer idPlanningLigne) { this.idPlanningLigne = idPlanningLigne; }

    public String getSourcePrevision() { return sourcePrevision; }
    public void setSourcePrevision(String sourcePrevision) { this.sourcePrevision = sourcePrevision; }

    public String getTypeJour() { return typeJour; }
    public void setTypeJour(String typeJour) { this.typeJour = typeJour; }

    public int getMinutesPrevues() { return minutesPrevues; }
    public void setMinutesPrevues(int minutesPrevues) { this.minutesPrevues = minutesPrevues; }

    public int getMinutesPresence() { return minutesPresence; }
    public void setMinutesPresence(int minutesPresence) { this.minutesPresence = minutesPresence; }

    public int getMinutesTravaillees() { return minutesTravaillees; }
    public void setMinutesTravaillees(int minutesTravaillees) { this.minutesTravaillees = minutesTravaillees; }

    public int getMinutesRetard() { return minutesRetard; }
    public void setMinutesRetard(int minutesRetard) { this.minutesRetard = minutesRetard; }

    public int getMinutesDepartAnticipe() { return minutesDepartAnticipe; }
    public void setMinutesDepartAnticipe(int minutesDepartAnticipe) { this.minutesDepartAnticipe = minutesDepartAnticipe; }

    public int getMinutesSup() { return minutesSup; }
    public void setMinutesSup(int minutesSup) { this.minutesSup = minutesSup; }

    public int getMinutesNuit() { return minutesNuit; }
    public void setMinutesNuit(int minutesNuit) { this.minutesNuit = minutesNuit; }

    public int getMinutesDimanche() { return minutesDimanche; }
    public void setMinutesDimanche(int minutesDimanche) { this.minutesDimanche = minutesDimanche; }

    public int getMinutesFerie() { return minutesFerie; }
    public void setMinutesFerie(int minutesFerie) { this.minutesFerie = minutesFerie; }

    public int getMinutesAbsence() { return minutesAbsence; }
    public void setMinutesAbsence(int minutesAbsence) { this.minutesAbsence = minutesAbsence; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateCalcul() { return dateCalcul; }
    public void setDateCalcul(LocalDateTime dateCalcul) { this.dateCalcul = dateCalcul; }

    public String getJourFr() { return Formats.date(jour); }
    public static String hm(int minutes) {
        if (minutes == 0) return "—";
        return (minutes / 60) + " h " + String.format("%02d", minutes % 60);
    }
    public String getPrevuesFr() { return hm(minutesPrevues); }
    public String getTravailleesFr() { return hm(minutesTravaillees); }
    public String getRetardFr() { return hm(minutesRetard); }
    public String getSupFr() { return hm(minutesSup); }
    public String getAbsenceFr() { return hm(minutesAbsence); }
    public String getStatutLibelle() {
        return switch (statut) {
            case "CALCULEE" -> "Calculée";
            case "A_REGULARISER" -> "À régulariser";
            case "ABSENCE_JUSTIFIEE" -> "Absence justifiée";
            case "REPOS" -> "Repos";
            case "CLOTUREE" -> "Clôturée";
            default -> statut;
        };
    }
    public String getStatutBadge() {
        return switch (statut) {
            case "CALCULEE" -> "badge-ok";
            case "A_REGULARISER" -> "badge-bad";
            case "ABSENCE_JUSTIFIEE" -> "badge-info";
            default -> "";
        };
    }
}
