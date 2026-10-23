package com.gestionrh.sirh.service.dto;

import java.util.List;

public class ContratDetail {

    private ContratVue contrat;
    private List<SignatureVue> signatures;
    private List<DocumentResume> documents;
    private List<ContratResume> avenants;
    private List<EvenementAuditVue> historique;
    private List<ValidationVue> validations;
    private boolean peutSoumettre;
    private boolean peutValider;
    private boolean peutSigner;
    private boolean peutActiver;
    private boolean peutTerminer;
    private boolean peutAnnuler;
    private boolean peutCreerAvenant;
    private boolean peutRenouveler;

    public ContratVue getContrat() { return contrat; }
    public void setContrat(ContratVue contrat) { this.contrat = contrat; }
    public List<SignatureVue> getSignatures() { return signatures; }
    public void setSignatures(List<SignatureVue> signatures) { this.signatures = signatures; }
    public List<DocumentResume> getDocuments() { return documents; }
    public void setDocuments(List<DocumentResume> documents) { this.documents = documents; }
    public List<ContratResume> getAvenants() { return avenants; }
    public void setAvenants(List<ContratResume> avenants) { this.avenants = avenants; }
    public List<EvenementAuditVue> getHistorique() { return historique; }
    public void setHistorique(List<EvenementAuditVue> historique) { this.historique = historique; }
    public List<ValidationVue> getValidations() { return validations; }
    public void setValidations(List<ValidationVue> validations) { this.validations = validations; }
    public boolean isPeutSoumettre() { return peutSoumettre; }
    public void setPeutSoumettre(boolean peutSoumettre) { this.peutSoumettre = peutSoumettre; }
    public boolean isPeutValider() { return peutValider; }
    public void setPeutValider(boolean peutValider) { this.peutValider = peutValider; }
    public boolean isPeutSigner() { return peutSigner; }
    public void setPeutSigner(boolean peutSigner) { this.peutSigner = peutSigner; }
    public boolean isPeutActiver() { return peutActiver; }
    public void setPeutActiver(boolean peutActiver) { this.peutActiver = peutActiver; }
    public boolean isPeutTerminer() { return peutTerminer; }
    public void setPeutTerminer(boolean peutTerminer) { this.peutTerminer = peutTerminer; }
    public boolean isPeutAnnuler() { return peutAnnuler; }
    public void setPeutAnnuler(boolean peutAnnuler) { this.peutAnnuler = peutAnnuler; }
    public boolean isPeutCreerAvenant() { return peutCreerAvenant; }
    public void setPeutCreerAvenant(boolean peutCreerAvenant) { this.peutCreerAvenant = peutCreerAvenant; }
    public boolean isPeutRenouveler() { return peutRenouveler; }
    public void setPeutRenouveler(boolean peutRenouveler) { this.peutRenouveler = peutRenouveler; }
}
