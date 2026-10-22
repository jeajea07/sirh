package com.gestionrh.sirh.service;

import java.time.LocalDateTime;
import java.util.List;

import com.gestionrh.sirh.model.DemandeBesoinModel;

public class SuiviDemande {

    public static class Etape {
        private final String symbole;
        private final String libelle;
        private final String etat;
        private final String info;

        public Etape(String symbole, String libelle, String etat, String info) {
            this.symbole = symbole;
            this.libelle = libelle;
            this.etat = etat;
            this.info = info;
        }

        public String getSymbole() {
            return symbole;
        }

        public String getLibelle() {
            return libelle;
        }

        public String getEtat() {
            return etat;
        }

        public String getInfo() {
            return info;
        }
    }

    public static class Evenement {
        private final LocalDateTime date;
        private final String dateFr;
        private final String titre;
        private final String detail;

        public Evenement(LocalDateTime date, String dateFr, String titre, String detail) {
            this.date = date;
            this.dateFr = dateFr;
            this.titre = titre;
            this.detail = detail;
        }

        public LocalDateTime getDate() {
            return date;
        }

        public String getDateFr() {
            return dateFr;
        }

        public String getTitre() {
            return titre;
        }

        public String getDetail() {
            return detail;
        }
    }

    private final DemandeBesoinModel demande;
    private final List<Etape> etapes;
    private final List<Evenement> evenements;
    private final String enCours;
    private final String commentaireDecision;

    public SuiviDemande(DemandeBesoinModel demande, List<Etape> etapes, List<Evenement> evenements,
                        String enCours, String commentaireDecision) {
        this.demande = demande;
        this.etapes = etapes;
        this.evenements = evenements;
        this.enCours = enCours;
        this.commentaireDecision = commentaireDecision;
    }

    public DemandeBesoinModel getDemande() {
        return demande;
    }

    public List<Etape> getEtapes() {
        return etapes;
    }

    public List<Evenement> getEvenements() {
        return evenements;
    }

    public String getEnCours() {
        return enCours;
    }

    public String getCommentaireDecision() {
        return commentaireDecision;
    }
}
