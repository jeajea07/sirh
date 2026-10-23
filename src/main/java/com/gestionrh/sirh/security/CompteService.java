package com.gestionrh.sirh.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gestionrh.sirh.model.CandidatModel;
import com.gestionrh.sirh.model.UtilisateurModel;
import com.gestionrh.sirh.repository.CandidatRepository;
import com.gestionrh.sirh.repository.UtilisateurRepository;

/**
 * Lit les comptes RH/DG/Finance/Employé dans la table utilisateur,
 * et les comptes candidat dans la table candidat.
 */
@Service
public class CompteService implements UserDetailsService {

    private final UtilisateurRepository repo;
    private final CandidatRepository candidats;

    public CompteService(UtilisateurRepository repo, CandidatRepository candidats) {
        this.repo = repo;
        this.candidats = candidats;
    }

    @Override
    public UserDetails loadUserByUsername(String identifiant) throws UsernameNotFoundException {
        String id = identifiant == null ? "" : identifiant.trim();

        if (id.isBlank()) {
            throw new UsernameNotFoundException("Compte introuvable : " + id);
        }

        UtilisateurModel employe = repo.trouverParEmail(id).orElse(null);
        if (employe != null) {
            return User.withUsername(id)
                    .password(employe.getMotDePasse())
                    .roles(employe.getRole())
                    .disabled(!employe.isActif())
                    .build();
        }

        CandidatModel candidat = candidats.findByEmail(id)
                .or(() -> candidats.findByIdentifiant(id))
                .orElseThrow(() -> new UsernameNotFoundException("Compte introuvable : " + id));

        return User.withUsername(candidat.getEmail() != null ? candidat.getEmail() : candidat.getIdentifiant())
                .password(candidat.getMotDePasse())
                .roles("CANDIDAT")
                .disabled(!candidat.isActif())
                .build();
    }
}
