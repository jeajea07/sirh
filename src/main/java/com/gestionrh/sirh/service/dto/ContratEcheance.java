package com.gestionrh.sirh.service.dto;

public class ContratEcheance {

    private final ContratResume contrat;
    private final long joursRestants;

    public ContratEcheance(ContratResume contrat, long joursRestants) {
        this.contrat = contrat;
        this.joursRestants = joursRestants;
    }

    public ContratResume getContrat() { return contrat; }
    public long getJoursRestants() { return joursRestants; }

    public String getNiveauUrgence() {
        if (joursRestants <= 7) return "critique";
        if (joursRestants <= 30) return "attention";
        return "normal";
    }
}
