package com.gestionrh.sirh.service.dto;

/** État d'une pièce obligatoire dans le dossier d'un salarié (Bloc 5 §10, RG-DO02 / RG-DO08). */
public class PieceDossierVue {

    private final String code;
    private final String libelle;
    private final String etat;
    private final String dateExpirationFr;
    private final Integer idDocument;
    private final Integer idType;

    public PieceDossierVue(String code, String libelle, String etat, String dateExpirationFr,
                           Integer idDocument, Integer idType) {
        this.code = code;
        this.libelle = libelle;
        this.etat = etat;
        this.dateExpirationFr = dateExpirationFr;
        this.idDocument = idDocument;
        this.idType = idType;
    }

    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
    /** VALIDE, A_RENOUVELER, EN_VERIFICATION, EXPIRE, REJETE, MANQUANT. */
    public String getEtat() { return etat; }
    public String getDateExpirationFr() { return dateExpirationFr; }
    public Integer getIdDocument() { return idDocument; }
    public Integer getIdType() { return idType; }

    /** RG-DO08 : la pièce rend le dossier incomplet. */
    public boolean isBloquante() {
        return "MANQUANT".equals(etat) || "EXPIRE".equals(etat) || "REJETE".equals(etat);
    }

    public String getEtatLibelle() {
        return switch (etat) {
            case "VALIDE" -> "Valide";
            case "A_RENOUVELER" -> "À renouveler";
            case "EN_VERIFICATION" -> "En vérification";
            case "EXPIRE" -> "Expirée";
            case "REJETE" -> "Rejetée";
            default -> "Manquante";
        };
    }

    public String getEtatBadge() {
        return switch (etat) {
            case "VALIDE" -> "badge-ok";
            case "A_RENOUVELER" -> "badge-warn";
            case "EN_VERIFICATION" -> "badge-info";
            default -> "badge-bad";
        };
    }
}
