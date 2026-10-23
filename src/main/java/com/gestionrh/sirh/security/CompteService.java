package com.gestionrh.sirh.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gestionrh.sirh.model.UtilisateurModel;
import com.gestionrh.sirh.repository.UtilisateurRepository;

/**
 * Dit à Spring Security où lire les comptes : table utilisateur,
 * retrouvée par l'email ou le matricule de l'employé.
 */
@Service
public class CompteService implements UserDetailsService {

    private final UtilisateurRepository repo;

    public CompteService(UtilisateurRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String identifiant) throws UsernameNotFoundException {
        String id = identifiant == null ? "" : identifiant.trim();

        UtilisateurModel u = repo.trouverParEmail(id)
                            .orElseThrow(() -> new UsernameNotFoundException("Compte introuvable : " + id));

        return User.withUsername(id)
                .password(u.getMotDePasse())      // hash BCrypt stocké en base
                .roles(u.getRole())               // "RH" devient l'autorité ROLE_RH
                .disabled(!u.isActif())
                .build();
    }
}
