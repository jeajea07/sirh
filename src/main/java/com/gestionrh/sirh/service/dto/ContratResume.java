package com.gestionrh.sirh.service.dto;

import java.time.LocalDate;

import com.gestionrh.sirh.util.Formats;

public class ContratResume {

    private final Integer id;
    private final String reference;
    private final String employeNom;
    private final String typeContrat;
    private final LocalDate dateDebut;
    private final LocalDate dateFin;
    private final String statutWorkflow;
    private final String statutBadge;
    private final String salaireFr;
    private final long joursRestants;
    private final String typeOperationLibelle;

    public ContratResume(Integer id, String reference, String employeNom, String typeContrat,
                         LocalDate dateDebut, LocalDate dateFin, String statutWorkflow,
                         String statutBadge, String salaireFr, long joursRestants,
                         String typeOperationLibelle) {
        this.id = id;
        this.reference = reference;
        this.employeNom = employeNom;
        this.typeContrat = typeContrat;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statutWorkflow = statutWorkflow;
        this.statutBadge = statutBadge;
        this.salaireFr = salaireFr;
        this.joursRestants = joursRestants;
        this.typeOperationLibelle = typeOperationLibelle;
    }

    public Integer getId() { return id; }
    public String getReference() { return reference; }
    public String getEmployeNom() { return employeNom; }
    public String getTypeContrat() { return typeContrat; }
    public LocalDate getDateDebut() { return dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public String getStatutWorkflow() { return statutWorkflow; }
    public String getStatutBadge() { return statutBadge; }
    public String getSalaireFr() { return salaireFr; }
    public long getJoursRestants() { return joursRestants; }
    public String getTypeOperationLibelle() { return typeOperationLibelle; }
    public String getDateDebutFr() { return Formats.date(dateDebut); }
    public String getDateFinFr() { return Formats.date(dateFin); }
}
