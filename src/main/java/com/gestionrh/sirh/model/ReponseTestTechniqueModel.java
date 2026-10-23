package com.gestionrh.sirh.model;

import java.math.BigDecimal;

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
@Table(name = "reponse_test_technique")
public class ReponseTestTechniqueModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reponse")
    private Long idReponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_test_technique", nullable = false)
    private TestTechniqueModel testTechnique;

    @Column(name = "question_ref", nullable = false)
    private String questionRef;

    @Column(name = "type_question", nullable = false)
    private String typeQuestion = "CHOIX";

    @Column(name = "reponse_textuelle")
    private String reponseTextuelle;

    @Column(name = "note")
    private BigDecimal note = BigDecimal.ZERO;

    @Column(name = "corrigee")
    private boolean corrigee = false;

    @Column(name = "correct")
    private boolean correct = false;

    public Long getIdReponse() {
        return idReponse;
    }

    public void setIdReponse(Long idReponse) {
        this.idReponse = idReponse;
    }

    public TestTechniqueModel getTestTechnique() {
        return testTechnique;
    }

    public void setTestTechnique(TestTechniqueModel testTechnique) {
        this.testTechnique = testTechnique;
    }

    public String getQuestionRef() {
        return questionRef;
    }

    public void setQuestionRef(String questionRef) {
        this.questionRef = questionRef;
    }

    public String getTypeQuestion() {
        return typeQuestion;
    }

    public void setTypeQuestion(String typeQuestion) {
        this.typeQuestion = typeQuestion;
    }

    public String getReponseTextuelle() {
        return reponseTextuelle;
    }

    public void setReponseTextuelle(String reponseTextuelle) {
        this.reponseTextuelle = reponseTextuelle;
    }

    public BigDecimal getNote() {
        return note;
    }

    public void setNote(BigDecimal note) {
        this.note = note;
    }

    public boolean isCorrigee() {
        return corrigee;
    }

    public void setCorrigee(boolean corrigee) {
        this.corrigee = corrigee;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
