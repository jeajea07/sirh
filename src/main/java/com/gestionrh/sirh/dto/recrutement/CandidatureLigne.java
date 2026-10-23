package com.gestionrh.sirh.dto.recrutement;

import com.gestionrh.sirh.enums.recrutement.CandidatureStatut;
import com.gestionrh.sirh.enums.recrutement.Classement;
import java.time.LocalDateTime;

public record CandidatureLigne(Integer id, String numero, String candidat, String email, String offre,
        Classement classement, CandidatureStatut statut, LocalDateTime dateReception) { }
