package com.gestionrh.sirh.service.dto;

import java.time.LocalDate;

import com.gestionrh.sirh.util.Formats;

public class OnboardingResume {

    private final Integer id;
    private final String reference;
    private final String employeNom;
    private final String poste;
    private final String departement;
    private final LocalDate dateDebutPrevue;
    private final int progression;
    private final int tachesEnRetard;
    private final int tachesTerminees;
    private final int nombreTaches;
    private final String statut;
    private final String statutLibelle;
    private final String statutBadge;
    private final String responsableRhNom;

    public OnboardingResume(Integer id, String reference, String employeNom, String poste,
                            String departement, LocalDate dateDebutPrevue, int progression,
                            int tachesEnRetard, int tachesTerminees, int nombreTaches,
                            String statut, String statutLibelle, String statutBadge,
                            String responsableRhNom) {
        this.id = id;
        this.reference = reference;
        this.employeNom = employeNom;
        this.poste = poste;
        this.departement = departement;
        this.dateDebutPrevue = dateDebutPrevue;
        this.progression = progression;
        this.tachesEnRetard = tachesEnRetard;
        this.tachesTerminees = tachesTerminees;
        this.nombreTaches = nombreTaches;
        this.statut = statut;
        this.statutLibelle = statutLibelle;
        this.statutBadge = statutBadge;
        this.responsableRhNom = responsableRhNom;
    }

    public Integer getId() { return id; }
    public String getReference() { return reference; }
    public String getEmployeNom() { return employeNom; }
    public String getPoste() { return poste; }
    public String getDepartement() { return departement; }
    public LocalDate getDateDebutPrevue() { return dateDebutPrevue; }
    public String getDateDebutPrevueFr() { return Formats.date(dateDebutPrevue); }
    public int getProgression() { return progression; }
    public int getTachesEnRetard() { return tachesEnRetard; }
    public int getTachesTerminees() { return tachesTerminees; }
    public int getNombreTaches() { return nombreTaches; }
    public String getStatut() { return statut; }
    public String getStatutLibelle() { return statutLibelle; }
    public String getStatutBadge() { return statutBadge; }
    public String getResponsableRhNom() { return responsableRhNom; }
}
