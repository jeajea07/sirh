package com.gestionrh.sirh.dto.recrutement;

import java.time.LocalDateTime;

public record CandidatureReserveResume(Integer id, String numero, String nom, String prenom, String email,
        LocalDateTime dateReception) { }
