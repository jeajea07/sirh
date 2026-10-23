package com.gestionrh.sirh.dto.recrutement;

import com.gestionrh.sirh.enums.recrutement.Classement;
import jakarta.validation.constraints.Size;

public class DecisionCandidatureForm {
    private Classement classement;
    @Size(max = 2000) private String commentaire;
    public Classement getClassement() { return classement; }
    public void setClassement(Classement classement) { this.classement = classement; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}
