package com.gestionrh.sirh.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.repository.ContratRepository;
import com.gestionrh.sirh.repository.DeclarationSocialeRepository;
import com.gestionrh.sirh.repository.DocumentRepository;
import com.gestionrh.sirh.repository.OnboardingRepository;
import com.gestionrh.sirh.repository.OnboardingTacheRepository;
import com.gestionrh.sirh.service.dto.ContratEcheance;
import com.gestionrh.sirh.service.dto.OnboardingResume;
import com.gestionrh.sirh.service.dto.TableauBordRh;
import com.gestionrh.sirh.service.dto.TacheVue;

@Service
public class TableauBordRhService {

    private final ContratRepository contrats;
    private final OnboardingRepository onboardings;
    private final OnboardingTacheRepository taches;
    private final DeclarationSocialeRepository declarations;
    private final DocumentRepository documents;
    private final ContratService contratService;
    private final OnboardingService onboardingService;

    public TableauBordRhService(ContratRepository contrats, OnboardingRepository onboardings,
                                OnboardingTacheRepository taches,
                                DeclarationSocialeRepository declarations,
                                DocumentRepository documents, ContratService contratService,
                                OnboardingService onboardingService) {
        this.contrats = contrats;
        this.onboardings = onboardings;
        this.taches = taches;
        this.declarations = declarations;
        this.documents = documents;
        this.contratService = contratService;
        this.onboardingService = onboardingService;
    }

    @Transactional(readOnly = true)
    public TableauBordRh resume() {
        TableauBordRh r = new TableauBordRh();
        r.setContratsEnPreparation(contrats.countByStatutWorkflow("BROUILLON"));
        r.setContratsAValider(contrats.countByStatutWorkflow("A_VALIDER"));
        r.setContratsASigner(contrats.countByStatutWorkflow("A_SIGNER"));
        List<ContratEcheance> echeances = contratService.echeances(30);
        r.setContratsEcheance30j(echeances.size());
        r.setArrivees30j(contrats.arriveesPrevues(LocalDate.now(), LocalDate.now().plusDays(30)).size());
        r.setOnboardingsEnCours(onboardings.countByStatutIn(
                List.of("PREPARATION", "PRET", "EN_COURS", "BLOQUE", "A_VERIFIER")));
        r.setOnboardingsACloturer(onboardings.countByStatutIn(List.of("A_VERIFIER")));
        r.setTachesEnRetard(taches.enRetard(LocalDate.now()).size());
        r.setFormalitesEnAttente(declarations.countByStatutIn(
                List.of("A_PREPARER", "PREPAREE", "TRANSMISE")));
        r.setDocumentsARevoir(documents.countByStatutValidationIn(List.of("RECU")));
        r.setProchainesEcheances(echeances.stream().limit(10).toList());
        r.setOnboardingsRecents(onboardingService.lister(null, 0, 8).getContent());
        r.setTachesEnRetardListe(onboardingService.tachesEnRetard().stream().limit(10).toList());
        return r;
    }
}
