package com.gestionrh.sirh.service.recrutement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.gestionrh.sirh.enums.recrutement.Classement;
import com.gestionrh.sirh.enums.recrutement.NiveauEtude;
import com.gestionrh.sirh.enums.recrutement.NatureCritere;
import com.gestionrh.sirh.enums.recrutement.TypeCritere;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.model.recrutement.CritereOffre;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassementCandidatureServiceTest {

    private final ClassementCandidatureService service = new ClassementCandidatureService(1, 1);

    @Test
    void classeLesFrontieresDeNiveauEtExperience() {
        Candidature c = new Candidature();
        c.setNiveauEtude(NiveauEtude.LICENCE);
        c.setAnneesExperience(2);
        assertEquals(Classement.A_REGARDER, classer(c,
                critere(NatureCritere.NIVEAU_ETUDE, "MASTER", null), critere(NatureCritere.EXPERIENCE, null, 3)).classement());
        c.setNiveauEtude(NiveauEtude.BAC);
        c.setAnneesExperience(1);
        assertEquals(Classement.NON_CONFORME, classer(c,
                critere(NatureCritere.NIVEAU_ETUDE, "MASTER", null), critere(NatureCritere.EXPERIENCE, null, 3)).classement());
        c.setNiveauEtude(null);
        c.setAnneesExperience(null);
        assertEquals(Classement.A_REGARDER, classer(c,
                critere(NatureCritere.NIVEAU_ETUDE, "MASTER", null), critere(NatureCritere.EXPERIENCE, null, 3)).classement());
    }

    @Test
    void evalueCompetenceEtLangueEtNeTientPasCompteDesSouhaits() {
        Candidature c = new Candidature();
        c.setFormations("Formation en Java");
        c.setLangues("Français, Anglais");
        List<CritereOffre> criteres = List.of(
                critere(NatureCritere.COMPETENCE, "Jáva", null),
                critere(NatureCritere.LANGUE, "anglais", null),
                critere(NatureCritere.COMPETENCE, "SQL", null, TypeCritere.SOUHAITE));
        ResultatClassement r = service.classer(c, criteres, List.of());
        assertEquals(Classement.A_REGARDER, r.classement());
        assertEquals(EtatCritereClassement.DOUTEUX, r.obligatoires().get(0).etat());
        assertEquals(EtatCritereClassement.OK, r.obligatoires().get(1).etat());
        assertEquals(1, r.souhaites().size());
    }

    @Test
    void competenceAbsenteEstKoSiLeCandidatEnDeclareDautresEtDouteuseSinon() {
        Candidature c = new Candidature();
        assertEquals(Classement.A_REGARDER, classer(c, critere(NatureCritere.COMPETENCE, "Java", null)).classement());
        assertEquals(Classement.NON_CONFORME,
                service.classer(c, List.of(critere(NatureCritere.COMPETENCE, "Java", null)), List.of("Python")).classement());
    }

    @Test
    void unKoPrimeSurUnDoute() {
        Candidature c = new Candidature();
        c.setAnneesExperience(0);
        c.setLangues("");
        assertEquals(Classement.NON_CONFORME, classer(c,
                critere(NatureCritere.EXPERIENCE, null, 3), critere(NatureCritere.LANGUE, "Français", null)).classement());
    }

    private ResultatClassement classer(Candidature c, CritereOffre... criteres) {
        return service.classer(c, List.of(criteres), List.of());
    }

    private CritereOffre critere(NatureCritere nature, String libelle, Integer minimum) {
        return critere(nature, libelle, minimum, TypeCritere.OBLIGATOIRE);
    }

    private CritereOffre critere(NatureCritere nature, String libelle, Integer minimum, TypeCritere type) {
        CritereOffre c = new CritereOffre();
        c.setTypeCritere(type);
        c.setNature(nature);
        c.setLibelle(libelle);
        c.setValeurMin(minimum);
        return c;
    }
}
