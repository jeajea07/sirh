package com.gestionrh.sirh.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "grille_salariale")
public class GrilleSalarialeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grille_salariale")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_poste", nullable = false)
    private PosteModel poste;

    @ManyToOne
    @JoinColumn(name = "id_departement")
    private DepartementModel departement;

    @ManyToOne
    @JoinColumn(name = "id_type_contrat", nullable = false)
    private TypeContratModel typeContrat;

    @Column(name = "niveau")
    private String niveau;

    @Column(name = "salaire", nullable = false)
    private BigDecimal salaire;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public PosteModel getPoste() { return poste; }
    public void setPoste(PosteModel poste) { this.poste = poste; }

    public DepartementModel getDepartement() { return departement; }
    public void setDepartement(DepartementModel departement) { this.departement = departement; }

    public TypeContratModel getTypeContrat() { return typeContrat; }
    public void setTypeContrat(TypeContratModel typeContrat) { this.typeContrat = typeContrat; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public BigDecimal getSalaire() { return salaire; }
    public void setSalaire(BigDecimal salaire) { this.salaire = salaire; }
}
