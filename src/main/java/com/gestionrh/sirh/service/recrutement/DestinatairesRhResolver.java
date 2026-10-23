package com.gestionrh.sirh.service.recrutement;

import java.util.List;

/** Point d'intégration remplaçable lorsque les rôles RH seront disponibles dans le modèle commun. */
public interface DestinatairesRhResolver {
    List<Integer> trouverDestinataires();
}
