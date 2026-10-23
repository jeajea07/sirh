// Bloc 3
package com.gestionrh.sirh.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gestionrh.sirh.model.recrutement.Candidat;
import com.gestionrh.sirh.model.ContratModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.PropositionModel;
import com.gestionrh.sirh.model.RecrutementModel;
import com.gestionrh.sirh.model.SelectionModel;
import com.gestionrh.sirh.repository.recrutement.CandidatRepository;
import com.gestionrh.sirh.service.dto.EmbaucheDTO;

/**
 * Adaptateur Bloc 3 -> Bloc 4 : construit un contrat depuis un recrutement confirmé.
 * Les entités Bloc 3 ne franchissent jamais la frontière ; seul l'EmbaucheDTO est transmis.
 */
@Service
public class RecrutementAdapter {

    private final SelectionService selections;
    private final CandidatRepository candidats;
    private final ContratService contrats;

    public RecrutementAdapter(SelectionService selections, CandidatRepository candidats,
                              ContratService contrats) {
        this.selections = selections;
        this.candidats = candidats;
        this.contrats = contrats;
    }

    public record ResultatTransfert(String reference, boolean dejaTransmis) {
    }

    public ResultatTransfert transmettre(Integer idSelection, Integer idPoste,
                                         Integer idDepartement, EmployeModel auteur) {
        RecrutementModel recrutement = selections.recrutement(idSelection);
        if (recrutement == null) {
            throw new Bloc3Exception("Confirmez le recrutement avant de le transférer au Bloc 4.");
        }
        if ("TRANSMIS".equals(code(recrutement.getEtatTransfert()))) {
            return new ResultatTransfert(recrutement.getReferenceBloc4(), true);
        }
        if (!"CONFIRME".equals(code(recrutement.getEtatRecrutement()))) {
            throw new Bloc3Exception("Le recrutement doit être confirmé avant son transfert.");
        }

        SelectionModel selection = selections.selection(idSelection);
        PropositionModel proposition = propositionAcceptee(idSelection);
        if (!reponseAcceptee(proposition)) {
            throw new Bloc3Exception("La proposition d'embauche doit être acceptée par le candidat.");
        }
        Candidat candidat = candidats.findById(selection.getIdCandidatB2())
                .orElseThrow(() -> new Bloc3Exception("Candidat introuvable pour ce recrutement."));

        EmbaucheDTO dto = construireDto(recrutement, selection, proposition, candidat,
                idPoste, idDepartement);
        try {
            ContratModel contrat = contrats.creerDepuisEmbauche(dto, auteur);
            selections.enregistrerTransfert(idSelection, true, contrat.getReference(), null,
                    auteur.getId());
            return new ResultatTransfert(contrat.getReference(), false);
        } catch (Bloc4Exception e) {
            selections.enregistrerTransfert(idSelection, false, null, e.getMessage(), auteur.getId());
            throw new Bloc3Exception("Transfert Bloc 4 impossible : " + e.getMessage());
        }
    }

    private PropositionModel propositionAcceptee(Integer idSelection) {
        List<PropositionModel> propositions = selections.propositions(idSelection);
        return propositions.stream()
                .filter(p -> "ACCEPTEE".equals(code(p.getEtatProposition())))
                .findFirst()
                .orElseThrow(() -> new Bloc3Exception(
                        "Aucune proposition acceptée à transférer pour ce recrutement."));
    }

    private boolean reponseAcceptee(PropositionModel proposition) {
        return selections.reponses(proposition.getIdProposition()).stream()
                .anyMatch(r -> "ACCEPTEE".equals(code(r.getTypeReponse())));
    }

    private EmbaucheDTO construireDto(RecrutementModel recrutement, SelectionModel selection,
                                      PropositionModel proposition, Candidat candidat,
                                      Integer idPoste, Integer idDepartement) {
        EmbaucheDTO dto = new EmbaucheDTO();
        dto.setIdRecrutement(recrutement.getIdRecrutement());
        dto.setIdSelection(selection.getIdSelection());
        dto.setIdCandidatureB2(selection.getIdCandidatureB2());
        dto.setIdOffreB2(selection.getIdOffreB2());
        dto.setIdCandidatB2(selection.getIdCandidatB2());
        dto.setNom(candidat.getNom());
        dto.setPrenom(candidat.getPrenom());
        dto.setEmail(candidat.getEmail());
        dto.setTelephone(candidat.getTelephone());
        dto.setCodeTypeContrat(proposition.getTypeContrat() == null
                ? null : proposition.getTypeContrat().getCode());
        dto.setSalaire(proposition.getSalaireMensuel());
        dto.setDateDebut(proposition.getDateDebutPrevue());
        dto.setDureeMois(proposition.getDureeMois());
        dto.setFonction(proposition.getIntitulePoste());
        dto.setNombrePostes(recrutement.getNombrePostes());
        dto.setDateConfirmation(recrutement.getConfirmeLe());
        dto.setIdPoste(idPoste);
        dto.setIdDepartement(idDepartement);
        return dto;
    }

    private static String code(com.gestionrh.sirh.model.ReferenceModel reference) {
        return reference == null ? null : reference.getCode();
    }
}
