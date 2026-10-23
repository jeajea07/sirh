package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.enums.recrutement.Classement;
import com.gestionrh.sirh.enums.recrutement.NiveauEtude;
import com.gestionrh.sirh.enums.recrutement.NatureCritere;
import com.gestionrh.sirh.enums.recrutement.TypeCritere;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.model.recrutement.CritereOffre;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Classement pur du dossier : ne dépend ni du web ni des repositories. */
@Service
public class ClassementCandidatureService {

    private final int toleranceExperience;
    private final int toleranceNiveau;

    public ClassementCandidatureService(
            @Value("${app.recrutement.tolerance-experience-annees:1}") int toleranceExperience,
            @Value("${app.recrutement.tolerance-niveau-etude:1}") int toleranceNiveau) {
        this.toleranceExperience = Math.max(0, toleranceExperience);
        this.toleranceNiveau = Math.max(0, toleranceNiveau);
    }

    public ResultatClassement classer(Candidature candidature, List<CritereOffre> criteres, List<String> competences) {
        List<ResultatCritereClassement> obligatoires = new ArrayList<>();
        List<ResultatCritereClassement> souhaites = new ArrayList<>();
        for (CritereOffre critere : criteres) {
            ResultatCritereClassement resultat = evaluer(candidature, critere, competences);
            if (critere.getTypeCritere() == TypeCritere.OBLIGATOIRE) obligatoires.add(resultat);
            else souhaites.add(resultat);
        }
        Classement classement = obligatoires.stream().anyMatch(c -> c.etat() == EtatCritereClassement.KO)
                ? Classement.NON_CONFORME
                : obligatoires.stream().anyMatch(c -> c.etat() == EtatCritereClassement.DOUTEUX)
                        ? Classement.A_REGARDER : Classement.CONFORME;
        String motif = obligatoires.isEmpty() ? "Aucun critère obligatoire défini."
                : String.join("\n", obligatoires.stream().map(ResultatCritereClassement::detail).toList());
        return new ResultatClassement(classement, motif, List.copyOf(obligatoires), List.copyOf(souhaites));
    }

    private ResultatCritereClassement evaluer(Candidature c, CritereOffre critere, List<String> competences) {
        return switch (critere.getNature()) {
            case NIVEAU_ETUDE -> evaluerNiveau(c, critere);
            case EXPERIENCE -> evaluerExperience(c, critere);
            case COMPETENCE -> evaluerCompetence(c, critere, competences);
            case LANGUE -> evaluerLangue(c, critere);
        };
    }

    private ResultatCritereClassement evaluerNiveau(Candidature c, CritereOffre critere) {
        String valeur = c.getNiveauEtude() == null ? "non renseigné" : c.getNiveauEtude().name();
        if (c.getNiveauEtude() == null) return resultat(critere, EtatCritereClassement.DOUTEUX, valeur);
        try {
            int requis = NiveauEtude.valueOf(critere.getLibelle().trim().toUpperCase(Locale.ROOT)).ordinal();
            int ecart = requis - c.getNiveauEtude().ordinal();
            EtatCritereClassement etat = ecart <= 0 ? EtatCritereClassement.OK
                    : ecart <= toleranceNiveau ? EtatCritereClassement.DOUTEUX : EtatCritereClassement.KO;
            return resultat(critere, etat, valeur);
        } catch (RuntimeException e) {
            return resultat(critere, EtatCritereClassement.DOUTEUX, valeur + " (critère d'étude invalide)");
        }
    }

    private ResultatCritereClassement evaluerExperience(Candidature c, CritereOffre critere) {
        String valeur = c.getAnneesExperience() == null ? "non renseignée" : c.getAnneesExperience() + " an(s)";
        if (c.getAnneesExperience() == null) return resultat(critere, EtatCritereClassement.DOUTEUX, valeur);
        int ecart = critere.getValeurMin() - c.getAnneesExperience();
        EtatCritereClassement etat = ecart <= 0 ? EtatCritereClassement.OK
                : ecart <= toleranceExperience ? EtatCritereClassement.DOUTEUX : EtatCritereClassement.KO;
        return resultat(critere, etat, valeur);
    }

    private ResultatCritereClassement evaluerCompetence(Candidature c, CritereOffre critere, List<String> competences) {
        String cible = normaliser(critere.getLibelle());
        boolean presente = competences.stream().anyMatch(k -> normaliser(k).equals(cible));
        if (presente) return resultat(critere, EtatCritereClassement.OK, "présente dans les compétences déclarées");
        String texte = normaliser(nullToEmpty(c.getFormations()) + " " + nullToEmpty(c.getExperiences()));
        if (!cible.isEmpty() && texte.contains(cible)) {
            return resultat(critere, EtatCritereClassement.DOUTEUX, "citée dans les formations ou expériences");
        }
        return resultat(critere, competences.isEmpty() ? EtatCritereClassement.DOUTEUX : EtatCritereClassement.KO,
                competences.isEmpty() ? "aucune compétence déclarée" : "absente des compétences déclarées");
    }

    private ResultatCritereClassement evaluerLangue(Candidature c, CritereOffre critere) {
        String langues = c.getLangues();
        if (langues == null || langues.isBlank()) return resultat(critere, EtatCritereClassement.DOUTEUX, "non renseigné");
        boolean presente = normaliser(langues).contains(normaliser(critere.getLibelle()));
        return resultat(critere, presente ? EtatCritereClassement.OK : EtatCritereClassement.KO, langues);
    }

    private ResultatCritereClassement resultat(CritereOffre critere, EtatCritereClassement etat, String valeur) {
        String libelle = critere.getNature() == NatureCritere.EXPERIENCE
                ? critere.getValeurMin() + " an(s) minimum" : critere.getLibelle();
        String detail = critere.getNature() + " — " + libelle + " : " + etat + " (candidat : " + valeur + ")";
        return new ResultatCritereClassement(critere.getNature().name(), libelle, etat, valeur, detail);
    }

    private String normaliser(String texte) {
        if (texte == null) return "";
        return Normalizer.normalize(texte, Normalizer.Form.NFD).replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    private String nullToEmpty(String valeur) { return valeur == null ? "" : valeur; }
}
