package com.gestionrh.sirh.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.AffectationRepository;
import com.gestionrh.sirh.repository.EmployeRepository;

/**
 * Manager d'un salarié (Blocs 6 et 7) : le responsable de son affectation active (Bloc 5, RG-DC08),
 * à défaut un RESPONSABLE de son département, à défaut la DG (Bloc7.md §7.2). Jamais le salarié lui-même.
 */
@Service
public class ManagerService {

    private final AffectationRepository affectations;
    private final EmployeRepository employes;

    public ManagerService(AffectationRepository affectations, EmployeRepository employes) {
        this.affectations = affectations;
        this.employes = employes;
    }

    @Transactional(readOnly = true)
    public EmployeModel managerDe(EmployeModel e) {
        EmployeModel responsable = affectations.findFirstByIdEmployeAndStatutOrderByDateDebutDesc(e.getId(), "ACTIVE")
                .map(a -> a.getResponsable()).orElse(null);
        if (responsable != null && !responsable.getId().equals(e.getId()) && responsable.isActif()) {
            return responsable;
        }
        if (e.getDepartement() != null) {
            EmployeModel resp = employes.validateursDuDepartement("RESPONSABLE", e.getDepartement().getId()).stream()
                    .filter(r -> !r.getId().equals(e.getId())).findFirst().orElse(null);
            if (resp != null) {
                return resp;
            }
        }
        return employes.validateursParRole("DG").stream()
                .filter(d -> !d.getId().equals(e.getId())).findFirst().orElse(null);
    }

    /** Équipe d'un manager : salariés actifs dont il est le manager résolu. */
    @Transactional(readOnly = true)
    public List<EmployeModel> equipeDe(EmployeModel manager) {
        return employes.findByActifTrueOrderByNomAsc().stream()
                .filter(e -> !e.getId().equals(manager.getId()))
                .filter(e -> {
                    EmployeModel m = managerDe(e);
                    return m != null && Objects.equals(m.getId(), manager.getId());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean estManagerDe(EmployeModel manager, EmployeModel salarie) {
        EmployeModel m = managerDe(salarie);
        return m != null && m.getId().equals(manager.getId());
    }

    @Transactional(readOnly = true)
    public String roleDe(EmployeModel e) {
        return employes.roleDe(e.getId()).orElse("EMPLOYE");
    }
}
