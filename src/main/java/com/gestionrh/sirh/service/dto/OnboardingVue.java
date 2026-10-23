package com.gestionrh.sirh.service.dto;

public class OnboardingVue {

    private Integer id;
    private String reference;
    private Integer idEmploye;
    private String employeNom;
    private String employeMatricule;
    private String poste;
    private String departement;
    private Integer idContrat;
    private String contratReference;
    private String modeleNom;
    private Integer versionModele;
    private String dateDebutPrevueFr;
    private String dateDebutReelleFr;
    private String dateFinPrevueFr;
    private String dateClotureFr;
    private String statut;
    private String statutLibelle;
    private String statutBadge;
    private String responsableRhNom;
    private String clotureParNom;
    private String motifAnnulation;
    private int progression;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public Integer getIdEmploye() { return idEmploye; }
    public void setIdEmploye(Integer idEmploye) { this.idEmploye = idEmploye; }
    public String getEmployeNom() { return employeNom; }
    public void setEmployeNom(String employeNom) { this.employeNom = employeNom; }
    public String getEmployeMatricule() { return employeMatricule; }
    public void setEmployeMatricule(String employeMatricule) { this.employeMatricule = employeMatricule; }
    public String getPoste() { return poste; }
    public void setPoste(String poste) { this.poste = poste; }
    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }
    public Integer getIdContrat() { return idContrat; }
    public void setIdContrat(Integer idContrat) { this.idContrat = idContrat; }
    public String getContratReference() { return contratReference; }
    public void setContratReference(String contratReference) { this.contratReference = contratReference; }
    public String getModeleNom() { return modeleNom; }
    public void setModeleNom(String modeleNom) { this.modeleNom = modeleNom; }
    public Integer getVersionModele() { return versionModele; }
    public void setVersionModele(Integer versionModele) { this.versionModele = versionModele; }
    public String getDateDebutPrevueFr() { return dateDebutPrevueFr; }
    public void setDateDebutPrevueFr(String dateDebutPrevueFr) { this.dateDebutPrevueFr = dateDebutPrevueFr; }
    public String getDateDebutReelleFr() { return dateDebutReelleFr; }
    public void setDateDebutReelleFr(String dateDebutReelleFr) { this.dateDebutReelleFr = dateDebutReelleFr; }
    public String getDateFinPrevueFr() { return dateFinPrevueFr; }
    public void setDateFinPrevueFr(String dateFinPrevueFr) { this.dateFinPrevueFr = dateFinPrevueFr; }
    public String getDateClotureFr() { return dateClotureFr; }
    public void setDateClotureFr(String dateClotureFr) { this.dateClotureFr = dateClotureFr; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getStatutLibelle() { return statutLibelle; }
    public void setStatutLibelle(String statutLibelle) { this.statutLibelle = statutLibelle; }
    public String getStatutBadge() { return statutBadge; }
    public void setStatutBadge(String statutBadge) { this.statutBadge = statutBadge; }
    public String getResponsableRhNom() { return responsableRhNom; }
    public void setResponsableRhNom(String responsableRhNom) { this.responsableRhNom = responsableRhNom; }
    public String getClotureParNom() { return clotureParNom; }
    public void setClotureParNom(String clotureParNom) { this.clotureParNom = clotureParNom; }
    public String getMotifAnnulation() { return motifAnnulation; }
    public void setMotifAnnulation(String motifAnnulation) { this.motifAnnulation = motifAnnulation; }
    public int getProgression() { return progression; }
    public void setProgression(int progression) { this.progression = progression; }
}
