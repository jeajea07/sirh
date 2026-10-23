// Bloc 3
package com.gestionrh.sirh.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "b3_historique")
public class HistoriqueModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique")
    private Integer idHistorique;
    @Column(name = "type_element", nullable = false)
    private String typeElement;
    @Column(name = "id_element", nullable = false)
    private Integer idElement;
    @Column(nullable = false)
    private String action;
    @Column(name = "auteur_id")
    private Integer auteurId;
    private String commentaire;
    @Column(name = "date_action", nullable = false)
    private LocalDateTime dateAction = LocalDateTime.now();

    public Integer getIdHistorique() { return idHistorique; }
    public String getTypeElement() { return typeElement; }
    public void setTypeElement(String v) { typeElement = v; }
    public Integer getIdElement() { return idElement; }
    public void setIdElement(Integer v) { idElement = v; }
    public String getAction() { return action; }
    public void setAction(String v) { action = v; }
    public Integer getAuteurId() { return auteurId; }
    public void setAuteurId(Integer v) { auteurId = v; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String v) { commentaire = v; }
    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime v) { dateAction = v; }
}
