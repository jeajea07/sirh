package com.gestionrh.sirh.recrutement.model;

import com.gestionrh.sirh.recrutement.enums.NatureCritere;
import com.gestionrh.sirh.recrutement.enums.TypeCritere;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Critere d'une offre : obligatoire (classement) ou souhaite (affichage). */
@Entity
@Table(name = "critere_offre")
public class CritereOffre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCritereOffre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_offre", nullable = false)
    private Offre offre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private TypeCritere typeCritere;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NatureCritere nature;

    @Column(length = 150)
    private String libelle;

    private Integer valeurMin;

    public Integer getIdCritereOffre() { return idCritereOffre; }
    public void setIdCritereOffre(Integer idCritereOffre) { this.idCritereOffre = idCritereOffre; }

    public Offre getOffre() { return offre; }
    public void setOffre(Offre offre) { this.offre = offre; }

    public TypeCritere getTypeCritere() { return typeCritere; }
    public void setTypeCritere(TypeCritere typeCritere) { this.typeCritere = typeCritere; }

    public NatureCritere getNature() { return nature; }
    public void setNature(NatureCritere nature) { this.nature = nature; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public Integer getValeurMin() { return valeurMin; }
    public void setValeurMin(Integer valeurMin) { this.valeurMin = valeurMin; }
}
