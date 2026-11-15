package com.gestionrh.sirh.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.EvenementCarriereModel;
import com.gestionrh.sirh.repository.EvenementCarriereRepository;

/**
 * Bloc 5 §11 — historique de carrière. Alimenté uniquement par les autres actions du dossier
 * (contrat, affectation, salaire, statut) : il ne se saisit pas à la main (RG-HI01, RG-HI02).
 */
@Service
public class CarriereService {

    private final EvenementCarriereRepository evenements;

    public CarriereService(EvenementCarriereRepository evenements) {
        this.evenements = evenements;
    }

    @Transactional
    public EvenementCarriereModel tracer(EmployeModel employe, String type, LocalDate dateEffet, String libelle,
                                         String ancienneValeur, String nouvelleValeur,
                                         String objetSource, Integer idObjetSource, EmployeModel auteur) {
        if (employe == null || auteur == null) {
            throw new Bloc4Exception("Événement de carrière sans salarié ou sans auteur.");
        }
        EvenementCarriereModel e = new EvenementCarriereModel();
        e.setEmploye(employe);
        e.setTypeEvenement(type);
        e.setDateEffet(dateEffet == null ? LocalDate.now() : dateEffet);
        e.setLibelle(libelle.length() > 255 ? libelle.substring(0, 255) : libelle);
        e.setAncienneValeur(ancienneValeur);
        e.setNouvelleValeur(nouvelleValeur);
        e.setObjetSource(objetSource);
        e.setIdObjetSource(idObjetSource);
        e.setAuteur(auteur);
        return evenements.save(e);
    }

    @Transactional(readOnly = true)
    public List<EvenementCarriereModel> chronologie(Integer idEmploye) {
        return evenements.chronologie(idEmploye);
    }

    @Transactional(readOnly = true)
    public boolean aDejaQuitteLEntreprise(Integer idEmploye) {
        return evenements.chronologie(idEmploye).stream().anyMatch(e -> "SORTIE".equals(e.getTypeEvenement()));
    }
}
