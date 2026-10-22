package com.gestionrh.sirh.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "notification")
public class NotificationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notification")
    private Integer id;

    @Column(name = "id_destinataire", nullable = false)
    private Integer idDestinataire;

    @Column(name = "id_demande_besoin")
    private Integer idDemandeBesoin;

    @Column(name = "messages", nullable = false)
    private String messages;

    @Column(name = "date_envoi", nullable = false)
    private LocalDateTime dateEnvoi;

    @Column(name = "lue", nullable = false)
    private boolean lue = false;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getIdDestinataire() { return idDestinataire; }
    public void setIdDestinataire(Integer idDestinataire) { this.idDestinataire = idDestinataire; }

    public Integer getIdDemandeBesoin() { return idDemandeBesoin; }
    public void setIdDemandeBesoin(Integer idDemandeBesoin) { this.idDemandeBesoin = idDemandeBesoin; }

    public String getMessages() { return messages; }
    public void setMessages(String messages) { this.messages = messages; }

    public boolean isLue() { return lue; }
    public void setLue(boolean lue) { this.lue = lue; }

    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }

}
