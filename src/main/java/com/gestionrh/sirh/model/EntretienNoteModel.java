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

/** Une question de la grille d'évaluation d'un entretien, avec la note du jury (1 à 5). */
@Entity
@Table(name = "b2_entretien_note")
public class EntretienNoteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_note")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_entretien", nullable = false)
    private EntretienModel entretien;

    @Column(nullable = false)
    private Short ordre;

    @Column(nullable = false)
    private String question;

    /** 1 = normale, 2 = importante, 3 = très importante. */
    @Column(nullable = false)
    private Short importance = 1;

    @Column(nullable = false)
    private boolean eliminatoire;

    @Column(name = "note_minimale")
    private BigDecimal noteMinimale;

    private BigDecimal note;

    public String getImportanceLibelle() {
        return importance == null ? "" : switch (importance) {
            case 3 -> "Très importante";
            case 2 -> "Importante";
            default -> "Normale";
        };
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public EntretienModel getEntretien() { return entretien; }
    public void setEntretien(EntretienModel entretien) { this.entretien = entretien; }
    public Short getOrdre() { return ordre; }
    public void setOrdre(Short ordre) { this.ordre = ordre; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public Short getImportance() { return importance; }
    public void setImportance(Short importance) { this.importance = importance; }
    public boolean isEliminatoire() { return eliminatoire; }
    public void setEliminatoire(boolean eliminatoire) { this.eliminatoire = eliminatoire; }
    public BigDecimal getNoteMinimale() { return noteMinimale; }
    public void setNoteMinimale(BigDecimal noteMinimale) { this.noteMinimale = noteMinimale; }
    public BigDecimal getNote() { return note; }
    public void setNote(BigDecimal note) { this.note = note; }
}
