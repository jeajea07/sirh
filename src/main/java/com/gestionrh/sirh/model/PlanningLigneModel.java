package com.gestionrh.sirh.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Une case du planning : un salarié, un jour (RG-PL02). */
@Entity
@Table(name = "planning_ligne")
public class PlanningLigneModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_planning_ligne")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_planning", nullable = false)
    private PlanningModel planning;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_employe", nullable = false)
    private EmployeModel employe;

    @Column(name = "jour", nullable = false)
    private LocalDate jour;

    @Column(name = "type_jour", nullable = false)
    private String typeJour;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_modele_horaire")
    private ModeleHoraireModel modele;

    @Column(name = "minutes_prevues", nullable = false)
    private int minutesPrevues;

    @Column(name = "minutes_sup_prevues", nullable = false)
    private int minutesSupPrevues;

    @Column(name = "majore", nullable = false)
    private boolean majore;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public PlanningModel getPlanning() { return planning; }
    public void setPlanning(PlanningModel planning) { this.planning = planning; }

    public EmployeModel getEmploye() { return employe; }
    public void setEmploye(EmployeModel employe) { this.employe = employe; }

    public LocalDate getJour() { return jour; }
    public void setJour(LocalDate jour) { this.jour = jour; }

    public String getTypeJour() { return typeJour; }
    public void setTypeJour(String typeJour) { this.typeJour = typeJour; }

    public ModeleHoraireModel getModele() { return modele; }
    public void setModele(ModeleHoraireModel modele) { this.modele = modele; }

    public int getMinutesPrevues() { return minutesPrevues; }
    public void setMinutesPrevues(int minutesPrevues) { this.minutesPrevues = minutesPrevues; }

    public int getMinutesSupPrevues() { return minutesSupPrevues; }
    public void setMinutesSupPrevues(int minutesSupPrevues) { this.minutesSupPrevues = minutesSupPrevues; }

    public boolean isMajore() { return majore; }
    public void setMajore(boolean majore) { this.majore = majore; }
}
