package com.gestionrh.sirh.model.recrutement;

import com.gestionrh.sirh.enums.recrutement.CandidatureStatut;
import com.gestionrh.sirh.enums.recrutement.Classement;
import com.gestionrh.sirh.enums.recrutement.NiveauEtude;
import com.gestionrh.sirh.enums.recrutement.TypeCv;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Candidature avec CV numerique et fichier du CV. offre == null et spontanee == true : candidature dans la reserve. */
@Entity
@Table(name = "candidature")
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCandidature;

    @Column(nullable = false, unique = true, length = 30)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_candidat", nullable = false)
    private Candidat candidat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_offre")
    private Offre offre;

    @Column(nullable = false)
    private Boolean spontanee = Boolean.FALSE;

    private LocalDateTime dateRattachement;

    @Column(nullable = false)
    private LocalDateTime dateReception = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private NiveauEtude niveauEtude;

    @PositiveOrZero
    private Integer anneesExperience;

    @Column(columnDefinition = "text")
    private String formations;

    @Column(columnDefinition = "text")
    private String experiences;

    @Column(length = 300)
    private String langues;

    private LocalDate disponibilite;

    @Column(nullable = false, length = 255)
    private String cvNomFichier;

    @Column(nullable = false, length = 500)
    private String cvChemin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private TypeCv cvType;

    @Column(nullable = false)
    private Long cvTailleOctets;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CandidatureStatut statut = CandidatureStatut.RECUE;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private Classement classementAuto;

    @Column(columnDefinition = "text")
    private String motifClassement;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private Classement classementFinal;

    private Integer idValidateur;

    private LocalDateTime dateValidation;

    @OneToMany(mappedBy = "candidature", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CvCompetence> competences = new ArrayList<>();

    public Integer getIdCandidature() { return idCandidature; }
    public void setIdCandidature(Integer idCandidature) { this.idCandidature = idCandidature; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public Candidat getCandidat() { return candidat; }
    public void setCandidat(Candidat candidat) { this.candidat = candidat; }

    public Offre getOffre() { return offre; }
    public void setOffre(Offre offre) { this.offre = offre; }

    public Boolean getSpontanee() { return spontanee; }
    public void setSpontanee(Boolean spontanee) { this.spontanee = spontanee; }

    public LocalDateTime getDateRattachement() { return dateRattachement; }
    public void setDateRattachement(LocalDateTime dateRattachement) { this.dateRattachement = dateRattachement; }

    public LocalDateTime getDateReception() { return dateReception; }
    public void setDateReception(LocalDateTime dateReception) { this.dateReception = dateReception; }

    public NiveauEtude getNiveauEtude() { return niveauEtude; }
    public void setNiveauEtude(NiveauEtude niveauEtude) { this.niveauEtude = niveauEtude; }

    public Integer getAnneesExperience() { return anneesExperience; }
    public void setAnneesExperience(Integer anneesExperience) { this.anneesExperience = anneesExperience; }

    public String getFormations() { return formations; }
    public void setFormations(String formations) { this.formations = formations; }

    public String getExperiences() { return experiences; }
    public void setExperiences(String experiences) { this.experiences = experiences; }

    public String getLangues() { return langues; }
    public void setLangues(String langues) { this.langues = langues; }

    public LocalDate getDisponibilite() { return disponibilite; }
    public void setDisponibilite(LocalDate disponibilite) { this.disponibilite = disponibilite; }

    public String getCvNomFichier() { return cvNomFichier; }
    public void setCvNomFichier(String cvNomFichier) { this.cvNomFichier = cvNomFichier; }

    public String getCvChemin() { return cvChemin; }
    public void setCvChemin(String cvChemin) { this.cvChemin = cvChemin; }

    public TypeCv getCvType() { return cvType; }
    public void setCvType(TypeCv cvType) { this.cvType = cvType; }

    public Long getCvTailleOctets() { return cvTailleOctets; }
    public void setCvTailleOctets(Long cvTailleOctets) { this.cvTailleOctets = cvTailleOctets; }

    public CandidatureStatut getStatut() { return statut; }
    public void setStatut(CandidatureStatut statut) { this.statut = statut; }

    public Classement getClassementAuto() { return classementAuto; }
    public void setClassementAuto(Classement classementAuto) { this.classementAuto = classementAuto; }

    public String getMotifClassement() { return motifClassement; }
    public void setMotifClassement(String motifClassement) { this.motifClassement = motifClassement; }

    public Classement getClassementFinal() { return classementFinal; }
    public void setClassementFinal(Classement classementFinal) { this.classementFinal = classementFinal; }

    public Integer getIdValidateur() { return idValidateur; }
    public void setIdValidateur(Integer idValidateur) { this.idValidateur = idValidateur; }

    public LocalDateTime getDateValidation() { return dateValidation; }
    public void setDateValidation(LocalDateTime dateValidation) { this.dateValidation = dateValidation; }

    public List<CvCompetence> getCompetences() { return competences; }
    public void setCompetences(List<CvCompetence> competences) { this.competences = competences; }

    public void addCompetence(CvCompetence c) { competences.add(c); c.setCandidature(this); }
    public void removeCompetence(CvCompetence c) { competences.remove(c); c.setCandidature(null); }
}
