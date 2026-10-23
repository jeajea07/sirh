package com.gestionrh.sirh.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.DepartementModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.ModeleOnboardingModel;
import com.gestionrh.sirh.model.ModeleTacheOnboardingModel;
import com.gestionrh.sirh.model.PosteModel;
import com.gestionrh.sirh.repository.DepartementRepository;
import com.gestionrh.sirh.repository.ModeleOnboardingRepository;
import com.gestionrh.sirh.repository.ModeleTacheOnboardingRepository;
import com.gestionrh.sirh.repository.OnboardingRepository;
import com.gestionrh.sirh.repository.PosteRepository;
import com.gestionrh.sirh.service.dto.ModeleOnboardingForm;
import com.gestionrh.sirh.service.dto.ModeleOnboardingResume;
import com.gestionrh.sirh.service.dto.ModeleTacheForm;

@Service
public class ModeleOnboardingService {

    private final ModeleOnboardingRepository modeles;
    private final ModeleTacheOnboardingRepository taches;
    private final OnboardingRepository onboardings;
    private final DepartementRepository departements;
    private final PosteRepository postes;
    private final AuditService audit;

    public ModeleOnboardingService(ModeleOnboardingRepository modeles,
                                   ModeleTacheOnboardingRepository taches,
                                   OnboardingRepository onboardings,
                                   DepartementRepository departements,
                                   PosteRepository postes,
                                   AuditService audit) {
        this.modeles = modeles;
        this.taches = taches;
        this.onboardings = onboardings;
        this.departements = departements;
        this.postes = postes;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<ModeleOnboardingResume> lister() {
        return modeles.findAllByOrderByNomAscVersionDesc().stream().map(this::resume).toList();
    }

    @Transactional(readOnly = true)
    public ModeleOnboardingForm formulaireNouveau() {
        return new ModeleOnboardingForm();
    }

    @Transactional(readOnly = true)
    public ModeleOnboardingForm formulaireDe(Integer id) {
        ModeleOnboardingModel m = charger(id);
        ModeleOnboardingForm f = new ModeleOnboardingForm();
        f.setId(m.getId());
        f.setNom(m.getNom());
        f.setDescription(m.getDescription());
        f.setIdDepartement(m.getDepartement() == null ? null : m.getDepartement().getId());
        f.setIdPoste(m.getPoste() == null ? null : m.getPoste().getId());
        f.getTaches().clear();
        List<ModeleTacheOnboardingModel> source = new ArrayList<>(m.getTaches());
        for (int i = 0; i < source.size(); i++) {
            ModeleTacheOnboardingModel t = source.get(i);
            ModeleTacheForm tf = new ModeleTacheForm();
            tf.setId(t.getId());
            tf.setTitre(t.getTitre());
            tf.setDescription(t.getDescription());
            tf.setRoleResponsable(t.getRoleResponsable());
            tf.setConditionActivation(t.getConditionActivation());
            tf.setDelaiIndicatifJours(t.getDelaiIndicatifJours());
            tf.setOrdre(t.getOrdre());
            tf.setObligatoire(t.isObligatoire());
            tf.setBloquante(t.isBloquante());
            tf.setDependDeTacheId(t.getDependDeTache() == null ? null : t.getDependDeTache().getId());
            if (t.getDependDeTache() != null) {
                for (int j = 0; j < source.size(); j++) {
                    if (source.get(j).getId().equals(t.getDependDeTache().getId())) {
                        tf.setDependDeIndex(j);
                        break;
                    }
                }
            }
            f.getTaches().add(tf);
        }
        return f;
    }

    public List<String> verifier(ModeleOnboardingForm f) {
        List<String> erreurs = new ArrayList<>();
        if (f.getNom() == null || f.getNom().isBlank()) {
            erreurs.add("Le nom du modèle est obligatoire.");
        }
        if (f.getTaches() == null || f.getTaches().isEmpty()) {
            erreurs.add("Le modèle doit contenir au moins une tâche.");
            return erreurs;
        }
        int n = f.getTaches().size();
        for (int i = 0; i < n; i++) {
            ModeleTacheForm t = f.getTaches().get(i);
            String prefixe = "Tâche " + (i + 1) + " : ";
            if (t.getTitre() == null || t.getTitre().isBlank()) {
                erreurs.add(prefixe + "le titre est obligatoire.");
            }
            if (t.getDelaiIndicatifJours() == null || t.getDelaiIndicatifJours() < 0) {
                erreurs.add(prefixe + "le délai doit être positif ou nul.");
            }
            if (t.getDependDeIndex() != null) {
                if (t.getDependDeIndex() < 0 || t.getDependDeIndex() >= n) {
                    erreurs.add(prefixe + "la dépendance est invalide.");
                } else if (t.getDependDeIndex() == i) {
                    erreurs.add(prefixe + "une tâche ne peut pas dépendre d'elle-même.");
                }
            }
        }
        if (erreurs.isEmpty() && aUnCycle(f)) {
            erreurs.add("Les dépendances des tâches forment un cycle.");
        }
        return erreurs;
    }

    @Transactional
    public ModeleOnboardingModel enregistrer(ModeleOnboardingForm f, EmployeModel auteur) {
        List<String> erreurs = verifier(f);
        if (!erreurs.isEmpty()) {
            throw new Bloc4Exception(String.join(" ", erreurs));
        }
        ModeleOnboardingModel m;
        if (f.getId() != null) {
            ModeleOnboardingModel existant = charger(f.getId());
            boolean utilise = onboardings.existsByIdModeleOnboarding(existant.getId());
            if (utilise) {
                m = new ModeleOnboardingModel();
                m.setNom(existant.getNom());
                m.setVersion(nextVersion(existant.getNom()));
                m.setActif(false);
                m.setCreePar(auteur);
                m.setDateCreation(LocalDateTime.now());
            } else {
                m = existant;
                m.getTaches().clear();
            }
        } else {
            m = new ModeleOnboardingModel();
            m.setNom(f.getNom().trim());
            m.setVersion(nextVersion(f.getNom()));
            m.setActif(true);
            m.setCreePar(auteur);
            m.setDateCreation(LocalDateTime.now());
        }
        m.setNom(f.getNom().trim());
        m.setDescription(f.getDescription());
        m.setDepartement(f.getIdDepartement() == null ? null
                : departements.findById(f.getIdDepartement())
                        .orElseThrow(() -> new Bloc4Exception("Département introuvable.")));
        m.setPoste(f.getIdPoste() == null ? null
                : postes.findById(f.getIdPoste())
                        .orElseThrow(() -> new Bloc4Exception("Poste introuvable.")));
        m.setDateMaj(LocalDateTime.now());

        List<ModeleTacheOnboardingModel> nouvelles = new ArrayList<>();
        for (ModeleTacheForm tf : f.getTaches()) {
            ModeleTacheOnboardingModel t = new ModeleTacheOnboardingModel();
            t.setModele(m);
            t.setTitre(tf.getTitre().trim());
            t.setDescription(tf.getDescription());
            t.setRoleResponsable(tf.getRoleResponsable() == null ? "RH" : tf.getRoleResponsable());
            t.setConditionActivation(tf.getConditionActivation() == null || tf.getConditionActivation().isBlank()
                    ? "TOUJOURS" : tf.getConditionActivation());
            t.setDelaiIndicatifJours(tf.getDelaiIndicatifJours() == null ? 0 : tf.getDelaiIndicatifJours());
            t.setOrdre(tf.getOrdre() == null ? 0 : tf.getOrdre());
            t.setObligatoire(tf.isObligatoire());
            t.setBloquante(tf.isBloquante());
            t.setDateCreation(LocalDateTime.now());
            m.getTaches().add(t);
            nouvelles.add(t);
        }
        m = modeles.save(m);
        List<ModeleTacheOnboardingModel> enregistrees = new ArrayList<>(m.getTaches());
        for (int i = 0; i < f.getTaches().size() && i < enregistrees.size(); i++) {
            Integer index = f.getTaches().get(i).getDependDeIndex();
            if (index != null && index >= 0 && index < enregistrees.size()) {
                enregistrees.get(i).setDependDeTache(enregistrees.get(index));
            }
        }
        taches.saveAll(enregistrees);
        audit.tracer("MODELE", m.getId(), f.getId() == null ? "CREATION" : "MODIFICATION",
                null, m.getNom() + " v" + m.getVersion(), null, auteur);
        return m;
    }

    @Transactional
    public void activer(Integer id, EmployeModel auteur) {
        ModeleOnboardingModel m = charger(id);
        modeles.findByNomOrderByVersionDesc(m.getNom()).forEach(v -> {
            v.setActif(v.getId().equals(m.getId()));
            v.setDateMaj(LocalDateTime.now());
            modeles.save(v);
        });
        audit.tracer("MODELE", id, "ACTIVATION", null, m.getNom() + " v" + m.getVersion(), null, auteur);
    }

    @Transactional
    public ModeleOnboardingModel dupliquer(Integer id, EmployeModel auteur) {
        ModeleOnboardingModel source = charger(id);
        ModeleOnboardingModel copie = new ModeleOnboardingModel();
        copie.setNom(source.getNom());
        copie.setDescription(source.getDescription());
        copie.setDepartement(source.getDepartement());
        copie.setPoste(source.getPoste());
        copie.setVersion(nextVersion(source.getNom()));
        copie.setActif(false);
        copie.setCreePar(auteur);
        copie.setDateCreation(LocalDateTime.now());
        copie.setDateMaj(LocalDateTime.now());
        Map<Integer, ModeleTacheOnboardingModel> correspondances = new HashMap<>();
        for (ModeleTacheOnboardingModel t : source.getTaches()) {
            ModeleTacheOnboardingModel c = new ModeleTacheOnboardingModel();
            c.setModele(copie);
            c.setTitre(t.getTitre());
            c.setDescription(t.getDescription());
            c.setRoleResponsable(t.getRoleResponsable());
            c.setConditionActivation(t.getConditionActivation());
            c.setDelaiIndicatifJours(t.getDelaiIndicatifJours());
            c.setOrdre(t.getOrdre());
            c.setObligatoire(t.isObligatoire());
            c.setBloquante(t.isBloquante());
            c.setDateCreation(LocalDateTime.now());
            copie.getTaches().add(c);
            correspondances.put(t.getId(), c);
        }
        for (ModeleTacheOnboardingModel t : source.getTaches()) {
            if (t.getDependDeTache() != null) {
                correspondances.get(t.getId()).setDependDeTache(
                        correspondances.get(t.getDependDeTache().getId()));
            }
        }
        copie = modeles.save(copie);
        audit.tracer("MODELE", copie.getId(), "DUPLICATION", null,
                copie.getNom() + " v" + copie.getVersion(), null, auteur);
        return copie;
    }

    private ModeleOnboardingModel charger(Integer id) {
        return modeles.findAvecTachesById(id)
                .orElseThrow(() -> new Bloc4Exception("Modèle d'onboarding introuvable."));
    }

    private int nextVersion(String nom) {
        return modeles.findByNomOrderByVersionDesc(nom).stream()
                .map(ModeleOnboardingModel::getVersion)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo).orElse(0) + 1;
    }

    private ModeleOnboardingResume resume(ModeleOnboardingModel m) {
        return new ModeleOnboardingResume(m.getId(), m.getNom(), m.getDescription(), m.getVersion(),
                m.getPoste() == null ? "Tous" : m.getPoste().getDesignation(),
                m.getDepartement() == null ? "Tous" : m.getDepartement().getDesignation(),
                m.isActif(), m.getNombreTaches(), m.getDureeIndicativeJours(), m.getDateMaj());
    }

    private boolean aUnCycle(ModeleOnboardingForm f) {
        Set<Integer> vus = new HashSet<>();
        Set<Integer> pile = new HashSet<>();
        for (int i = 0; i < f.getTaches().size(); i++) {
            if (cycle(i, f, vus, pile)) {
                return true;
            }
        }
        return false;
    }

    private boolean cycle(int i, ModeleOnboardingForm f, Set<Integer> vus, Set<Integer> pile) {
        if (pile.contains(i)) return true;
        if (!vus.add(i)) return false;
        Integer suivant = f.getTaches().get(i).getDependDeIndex();
        if (suivant == null || suivant < 0 || suivant >= f.getTaches().size()) return false;
        pile.add(i);
        boolean resultat = cycle(suivant, f, vus, pile);
        pile.remove(i);
        return resultat;
    }
}
