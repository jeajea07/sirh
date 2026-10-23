package com.gestionrh.sirh.service.dto;

public class DocumentResume {

    private final Integer id;
    private final String titre;
    private final String typeDocument;
    private final String typeLibelle;
    private final String employeNom;
    private final String contratReference;
    private final String nomFichier;
    private final String tailleFr;
    private final Integer version;
    private final String confidentialite;
    private final String confidentialiteLibelle;
    private final String statut;
    private final String statutLibelle;
    private final String statutBadge;
    private final String dateDepotFr;
    private final String typeMime;
    private final String commentaire;
    private final String motifRejet;
    private final Integer idEmploye;
    private final Integer idContrat;
    private final Integer idTache;

    public DocumentResume(Integer id, String titre, String typeDocument, String typeLibelle,
                          String employeNom, String contratReference, String nomFichier,
                          String tailleFr, Integer version, String confidentialite,
                          String confidentialiteLibelle, String statut, String statutLibelle,
                          String statutBadge, String dateDepotFr, String typeMime,
                          String commentaire, String motifRejet, Integer idEmploye,
                          Integer idContrat, Integer idTache) {
        this.id = id;
        this.titre = titre;
        this.typeDocument = typeDocument;
        this.typeLibelle = typeLibelle;
        this.employeNom = employeNom;
        this.contratReference = contratReference;
        this.nomFichier = nomFichier;
        this.tailleFr = tailleFr;
        this.version = version;
        this.confidentialite = confidentialite;
        this.confidentialiteLibelle = confidentialiteLibelle;
        this.statut = statut;
        this.statutLibelle = statutLibelle;
        this.statutBadge = statutBadge;
        this.dateDepotFr = dateDepotFr;
        this.typeMime = typeMime;
        this.commentaire = commentaire;
        this.motifRejet = motifRejet;
        this.idEmploye = idEmploye;
        this.idContrat = idContrat;
        this.idTache = idTache;
    }

    public Integer getId() { return id; }
    public String getTitre() { return titre; }
    public String getTypeDocument() { return typeDocument; }
    public String getTypeLibelle() { return typeLibelle; }
    public String getEmployeNom() { return employeNom; }
    public String getContratReference() { return contratReference; }
    public String getNomFichier() { return nomFichier; }
    public String getTailleFr() { return tailleFr; }
    public Integer getVersion() { return version; }
    public String getConfidentialite() { return confidentialite; }
    public String getConfidentialiteLibelle() { return confidentialiteLibelle; }
    public String getStatut() { return statut; }
    public String getStatutLibelle() { return statutLibelle; }
    public String getStatutBadge() { return statutBadge; }
    public String getDateDepotFr() { return dateDepotFr; }
    public String getTypeMime() { return typeMime; }
    public String getCommentaire() { return commentaire; }
    public String getMotifRejet() { return motifRejet; }
    public Integer getIdEmploye() { return idEmploye; }
    public Integer getIdContrat() { return idContrat; }
    public Integer getIdTache() { return idTache; }
    public boolean isSensible() { return "SANTE".equals(typeDocument) || "SECRET".equals(confidentialite); }
}
