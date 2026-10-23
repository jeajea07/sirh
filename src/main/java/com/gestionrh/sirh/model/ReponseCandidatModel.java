// Bloc 3
package com.gestionrh.sirh.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "b3_reponse_candidat")
public class ReponseCandidatModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reponse")
    private Integer idReponse;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_proposition", nullable = false)
    private PropositionModel proposition;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_type_reponse", nullable = false)
    private TypeReponseModel typeReponse;
    @Column(name = "reponse_le", nullable = false)
    private LocalDate reponseLe;
    private String commentaire;

    public Integer getIdReponse() { return idReponse; }
    public PropositionModel getProposition() { return proposition; }
    public void setProposition(PropositionModel v) { proposition = v; }
    public TypeReponseModel getTypeReponse() { return typeReponse; }
    public void setTypeReponse(TypeReponseModel v) { typeReponse = v; }
    public LocalDate getReponseLe() { return reponseLe; }
    public void setReponseLe(LocalDate v) { reponseLe = v; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String v) { commentaire = v; }
}
