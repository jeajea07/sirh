package com.gestionrh.sirh.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.gestionrh.sirh.model.UtilisateurModel;
import com.gestionrh.sirh.model.recrutement.Candidat;
import com.gestionrh.sirh.repository.UtilisateurRepository;
import com.gestionrh.sirh.repository.recrutement.CandidatRepository;

class CompteServiceTest {

    @Test
    void accepteLesRolesPersonnelAjoutes() {
        UtilisateurRepository repo = mock(UtilisateurRepository.class);
        CandidatRepository candidats = mock(CandidatRepository.class);
        CompteService service = new CompteService(repo, candidats);

        UtilisateurModel responsable = new UtilisateurModel();
        responsable.setRole("RESPONSABLE");
        responsable.setMotDePasse("hash01");
        responsable.setActif(true);
        when(repo.trouverParEmail("resp@example.com")).thenReturn(Optional.of(responsable));

        UserDetails details = service.loadUserByUsername("RESPONSABLE|resp@example.com");

        assertThat(details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList()).contains("ROLE_RESPONSABLE");

        UtilisateurModel technicien = new UtilisateurModel();
        technicien.setRole("TECHNICIEN");
        technicien.setMotDePasse("hash02");
        technicien.setActif(true);
        when(repo.trouverParEmail("tech@example.com")).thenReturn(Optional.of(technicien));

        UserDetails detailsTech = service.loadUserByUsername("TECHNICIEN|tech@example.com");

        assertThat(detailsTech.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList()).contains("ROLE_TECHNICIEN");
    }

    @Test
    void reconnaitLeCompteCandidatSansPrefixeDeRole() {
        UtilisateurRepository repo = mock(UtilisateurRepository.class);
        CandidatRepository candidats = mock(CandidatRepository.class);
        CompteService service = new CompteService(repo, candidats);

        Candidat candidat = new Candidat();
        candidat.setEmail("candidat@example.com");
        candidat.setMotDePasse("hash-candidat");
        candidat.setActif(true);
        when(candidats.findByEmailIgnoreCase("candidat@example.com")).thenReturn(Optional.of(candidat));

        UserDetails details = service.loadUserByUsername("candidat@example.com");

        assertThat(details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList()).contains("ROLE_CANDIDAT");
        assertThat(details.getUsername()).isEqualTo("candidat@example.com");
    }
}
