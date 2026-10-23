package com.gestionrh.sirh.service.dto;

public class ModeleTacheForm {

    private Integer id;
    private String titre;
    private String description;
    private String roleResponsable = "RH";
    private String conditionActivation = "TOUJOURS";
    private Integer delaiIndicatifJours = 0;
    private Integer ordre = 0;
    private Integer dependDeTacheId;
    private Integer dependDeIndex;
    private boolean obligatoire = true;
    private boolean bloquante = false;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRoleResponsable() { return roleResponsable; }
    public void setRoleResponsable(String roleResponsable) { this.roleResponsable = roleResponsable; }
    public String getConditionActivation() { return conditionActivation; }
    public void setConditionActivation(String conditionActivation) { this.conditionActivation = conditionActivation; }
    public Integer getDelaiIndicatifJours() { return delaiIndicatifJours; }
    public void setDelaiIndicatifJours(Integer delaiIndicatifJours) { this.delaiIndicatifJours = delaiIndicatifJours; }
    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }
    public Integer getDependDeTacheId() { return dependDeTacheId; }
    public void setDependDeTacheId(Integer dependDeTacheId) { this.dependDeTacheId = dependDeTacheId; }
    public Integer getDependDeIndex() { return dependDeIndex; }
    public void setDependDeIndex(Integer dependDeIndex) { this.dependDeIndex = dependDeIndex; }
    public boolean isObligatoire() { return obligatoire; }
    public void setObligatoire(boolean obligatoire) { this.obligatoire = obligatoire; }
    public boolean isBloquante() { return bloquante; }
    public void setBloquante(boolean bloquante) { this.bloquante = bloquante; }
}
