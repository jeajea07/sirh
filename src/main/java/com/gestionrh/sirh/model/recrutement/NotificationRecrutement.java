package com.gestionrh.sirh.recrutement.model;

import com.gestionrh.sirh.recrutement.enums.CanalNotification;
import com.gestionrh.sirh.recrutement.enums.StatutEnvoi;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/** Notification ou e-mail du recrutement. Exactement un destinataire : un employe (idDestinataireEmploye) OU un candidat (idCandidat, canal EMAIL). */
@Entity
@Table(name = "notification_recrutement")
public class NotificationRecrutement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idNotificationRecrutement;

    private Integer idDestinataireEmploye;

    private Integer idCandidat;

    private Integer idOffre;

    private Integer idCandidature;

    @Column(nullable = false, length = 40)
    private String typeNotification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CanalNotification canal = CanalNotification.INTERNE;

    @Column(length = 200)
    private String objet;

    @NotBlank
    @Column(columnDefinition = "text")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StatutEnvoi statutEnvoi = StatutEnvoi.EN_ATTENTE;

    @Column(columnDefinition = "text")
    private String erreur;

    @Column(nullable = false)
    private Boolean lu = Boolean.FALSE;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    private LocalDateTime dateEnvoi;

    public Integer getIdNotificationRecrutement() { return idNotificationRecrutement; }
    public void setIdNotificationRecrutement(Integer idNotificationRecrutement) { this.idNotificationRecrutement = idNotificationRecrutement; }

    public Integer getIdDestinataireEmploye() { return idDestinataireEmploye; }
    public void setIdDestinataireEmploye(Integer idDestinataireEmploye) { this.idDestinataireEmploye = idDestinataireEmploye; }

    public Integer getIdCandidat() { return idCandidat; }
    public void setIdCandidat(Integer idCandidat) { this.idCandidat = idCandidat; }

    public Integer getIdOffre() { return idOffre; }
    public void setIdOffre(Integer idOffre) { this.idOffre = idOffre; }

    public Integer getIdCandidature() { return idCandidature; }
    public void setIdCandidature(Integer idCandidature) { this.idCandidature = idCandidature; }

    public String getTypeNotification() { return typeNotification; }
    public void setTypeNotification(String typeNotification) { this.typeNotification = typeNotification; }

    public CanalNotification getCanal() { return canal; }
    public void setCanal(CanalNotification canal) { this.canal = canal; }

    public String getObjet() { return objet; }
    public void setObjet(String objet) { this.objet = objet; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public StatutEnvoi getStatutEnvoi() { return statutEnvoi; }
    public void setStatutEnvoi(StatutEnvoi statutEnvoi) { this.statutEnvoi = statutEnvoi; }

    public String getErreur() { return erreur; }
    public void setErreur(String erreur) { this.erreur = erreur; }

    public Boolean getLu() { return lu; }
    public void setLu(Boolean lu) { this.lu = lu; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }
}
