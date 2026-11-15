package com.gestionrh.sirh.model;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Type de congé ou d'absence paramétré (Bloc 7 §5). */
@Entity
@Table(name = "type_conge")
public class TypeCongeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_conge")
    private Integer id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Column(name = "categorie", nullable = false)
    private String categorie;

    @Column(name = "type_solde")
    private String typeSolde;

    @Column(name = "remuneration", nullable = false)
    private String remuneration;

    @Column(name = "mode_decompte", nullable = false)
    private String modeDecompte;

    @Column(name = "justificatif_obligatoire", nullable = false)
    private boolean justificatifObligatoire;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_type_justificatif")
    private TypeDocumentAdministratifModel typeJustificatif;

    @Column(name = "delai_justificatif_jours", nullable = false)
    private int delaiJustificatifJours;

    @Column(name = "delai_prevenance_jours", nullable = false)
    private int delaiPrevenanceJours;

    @Column(name = "duree_max_jours")
    private Integer dureeMaxJours;

    @Column(name = "validation_manager", nullable = false)
    private boolean validationManager;

    @Column(name = "assimile_travail", nullable = false)
    private boolean assimileTravail;

    @Column(name = "urgent", nullable = false)
    private boolean urgent;

    @Column(name = "saisie_salarie", nullable = false)
    private boolean saisieSalarie;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getTypeSolde() { return typeSolde; }
    public void setTypeSolde(String typeSolde) { this.typeSolde = typeSolde; }

    public String getRemuneration() { return remuneration; }
    public void setRemuneration(String remuneration) { this.remuneration = remuneration; }

    public String getModeDecompte() { return modeDecompte; }
    public void setModeDecompte(String modeDecompte) { this.modeDecompte = modeDecompte; }

    public boolean isJustificatifObligatoire() { return justificatifObligatoire; }
    public void setJustificatifObligatoire(boolean justificatifObligatoire) { this.justificatifObligatoire = justificatifObligatoire; }

    public TypeDocumentAdministratifModel getTypeJustificatif() { return typeJustificatif; }
    public void setTypeJustificatif(TypeDocumentAdministratifModel typeJustificatif) { this.typeJustificatif = typeJustificatif; }

    public int getDelaiJustificatifJours() { return delaiJustificatifJours; }
    public void setDelaiJustificatifJours(int delaiJustificatifJours) { this.delaiJustificatifJours = delaiJustificatifJours; }

    public int getDelaiPrevenanceJours() { return delaiPrevenanceJours; }
    public void setDelaiPrevenanceJours(int delaiPrevenanceJours) { this.delaiPrevenanceJours = delaiPrevenanceJours; }

    public Integer getDureeMaxJours() { return dureeMaxJours; }
    public void setDureeMaxJours(Integer dureeMaxJours) { this.dureeMaxJours = dureeMaxJours; }

    public boolean isValidationManager() { return validationManager; }
    public void setValidationManager(boolean validationManager) { this.validationManager = validationManager; }

    public boolean isAssimileTravail() { return assimileTravail; }
    public void setAssimileTravail(boolean assimileTravail) { this.assimileTravail = assimileTravail; }

    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }

    public boolean isSaisieSalarie() { return saisieSalarie; }
    public void setSaisieSalarie(boolean saisieSalarie) { this.saisieSalarie = saisieSalarie; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public boolean isConge() { return "CONGE".equals(categorie); }
    public String getRemunerationLibelle() {
        return switch (remuneration) { case "PAYE" -> "Payé"; case "NON_PAYE" -> "Non payé"; default -> "Indemnisé"; };
    }
}
