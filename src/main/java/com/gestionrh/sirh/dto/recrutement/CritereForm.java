package com.gestionrh.sirh.dto.recrutement;

import com.gestionrh.sirh.enums.recrutement.NatureCritere;
import com.gestionrh.sirh.enums.recrutement.NiveauEtude;
import com.gestionrh.sirh.enums.recrutement.TypeCritere;

/**
 * Ajout d'un critere a une offre. Selon la nature :
 * NIVEAU_ETUDE -> niveauEtude ; EXPERIENCE -> valeurMin (annees) ; COMPETENCE / LANGUE -> libelle.
 */
public class CritereForm {

    private TypeCritere typeCritere = TypeCritere.OBLIGATOIRE;
    private NatureCritere nature;
    private String libelle;
    private NiveauEtude niveauEtude;
    private Integer valeurMin;

    public TypeCritere getTypeCritere() { return typeCritere; }
    public void setTypeCritere(TypeCritere typeCritere) { this.typeCritere = typeCritere; }
    public NatureCritere getNature() { return nature; }
    public void setNature(NatureCritere nature) { this.nature = nature; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public NiveauEtude getNiveauEtude() { return niveauEtude; }
    public void setNiveauEtude(NiveauEtude niveauEtude) { this.niveauEtude = niveauEtude; }
    public Integer getValeurMin() { return valeurMin; }
    public void setValeurMin(Integer valeurMin) { this.valeurMin = valeurMin; }
}
