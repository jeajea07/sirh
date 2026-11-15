package com.gestionrh.sirh.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Paramétrage d'un type de pièce du dossier salarié (Bloc 5 §10.4). */
@Entity
@Table(name = "type_document_administratif")
public class TypeDocumentAdministratifModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_document_administratif")
    private Integer id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    /** Catégorie de la table document (ADMINISTRATIF, SANTE, SOCIAL…). */
    @Column(name = "categorie", nullable = false)
    private String categorie;

    @Column(name = "obligatoire", nullable = false)
    private boolean obligatoire;

    /** La pièce porte une date d'expiration (RG-DO04). */
    @Column(name = "expiration_requise", nullable = false)
    private boolean expirationRequise;

    /** Durée de validité à partir de la date d'émission, si la pièce n'indique pas d'expiration. */
    @Column(name = "duree_validite_jours")
    private Integer dureeValiditeJours;

    @Column(name = "delai_alerte_jours", nullable = false)
    private int delaiAlerteJours = 30;

    @Column(name = "niveau_confidentialite", nullable = false)
    private String niveauConfidentialite = "CONFIDENTIEL";

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    public Integer getId() { return id; }
    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
    public String getCategorie() { return categorie; }
    public boolean isObligatoire() { return obligatoire; }
    public boolean isExpirationRequise() { return expirationRequise; }
    public Integer getDureeValiditeJours() { return dureeValiditeJours; }
    public int getDelaiAlerteJours() { return delaiAlerteJours; }
    public String getNiveauConfidentialite() { return niveauConfidentialite; }
    public boolean isActif() { return actif; }
}
