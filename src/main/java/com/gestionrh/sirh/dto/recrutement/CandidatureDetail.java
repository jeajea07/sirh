package com.gestionrh.sirh.dto.recrutement;

import com.gestionrh.sirh.enums.recrutement.CandidatureStatut;
import com.gestionrh.sirh.enums.recrutement.Classement;
import com.gestionrh.sirh.enums.recrutement.NiveauEtude;
import com.gestionrh.sirh.service.recrutement.ResultatCritereClassement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CandidatureDetail(Integer id, String numero, String nom, String prenom, String email,
        String telephone, String adresse, String offre, CandidatureStatut statut, Classement classementAuto,
        Classement classementFinal, String motif, NiveauEtude niveauEtude, Integer anneesExperience,
        String formations, String experiences, String langues, LocalDate disponibilite,
        String cvNomFichier, List<String> competences, LocalDateTime dateReception,
        List<ResultatCritereClassement> criteresObligatoires, List<ResultatCritereClassement> criteresSouhaites) { }
