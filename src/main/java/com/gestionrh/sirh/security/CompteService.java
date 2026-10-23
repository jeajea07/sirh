package com.gestionrh.sirh.security;

import java.util.Locale;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
        String saisie = identifiant == null ? "" : identifiant.trim();
        int separateur = saisie.indexOf('|');
        if (separateur < 1 || separateur == saisie.length() - 1) {
            throw new UsernameNotFoundException("Sélectionnez un rôle et saisissez votre email.");
        }
        String roleChoisi = saisie.substring(0, separateur).toUpperCase(Locale.ROOT);
        String id = saisie.substring(separateur + 1).trim();
        if (!java.util.Set.of("EMPLOYE", "RH", "FINANCE", "DG").contains(roleChoisi)) {
            throw new UsernameNotFoundException("Rôle de connexion invalide.");
        }

        UtilisateurModel u = repo.trouverParEmail(id)
                            .orElseThrow(() -> new UsernameNotFoundException("Compte introuvable : " + id));

        boolean compteDg = "DG".equalsIgnoreCase(u.getRole());
        if (!compteDg && !roleChoisi.equalsIgnoreCase(u.getRole())) {
            throw new UsernameNotFoundException("Le rôle choisi ne correspond pas à ce compte.");
        }

        // Le DG peut choisir un espace ; il garde alors une marque ACCES_DG pour consulter les données de cet espace.
        java.util.List<GrantedAuthority> droits = new java.util.ArrayList<>();
        droits.add(new SimpleGrantedAuthority("ROLE_" + (compteDg ? roleChoisi : u.getRole())));
        if (compteDg) {
            droits.add(new SimpleGrantedAuthority("ACCES_DG"));
        }
        return User.withUsername(id)
                .password(u.getMotDePasse())      // hash BCrypt stocké en base
                .authorities(droits)
                .disabled(!u.isActif())
                .build();
    }
}
