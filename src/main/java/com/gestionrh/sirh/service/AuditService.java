package com.gestionrh.sirh.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.EvenementAuditModel;
import com.gestionrh.sirh.repository.EvenementAuditRepository;

@Service
public class AuditService {

    private final EvenementAuditRepository evenements;

    public AuditService(EvenementAuditRepository evenements) {
        this.evenements = evenements;
    }

    @Transactional
    public void tracer(String typeObjet, Integer idObjet, String action,
                       String ancienneValeur, String nouvelleValeur,
                       String commentaire, EmployeModel auteur) {
        if (auteur == null) {
            throw new Bloc4Exception("Aucun auteur identifié pour l'événement d'audit.");
        }
        EvenementAuditModel e = new EvenementAuditModel();
        e.setTypeObjet(typeObjet);
        e.setIdObjet(idObjet);
        e.setAction(action);
        e.setAncienneValeur(ancienneValeur);
        e.setNouvelleValeur(nouvelleValeur);
        e.setCommentaire(commentaire);
        e.setAuteur(auteur);
        e.setDateEvenement(LocalDateTime.now());
        evenements.save(e);
    }

    @Transactional(readOnly = true)
    public List<EvenementAuditModel> historique(String typeObjet, Integer idObjet) {
        return evenements.findByTypeObjetAndIdObjetOrderByDateEvenementDesc(typeObjet, idObjet);
    }

    @Transactional(readOnly = true)
    public Page<EvenementAuditModel> journal(Pageable pageable) {
        return evenements.findAllByOrderByDateEvenementDesc(pageable);
    }
}
