package com.gestionrh.sirh.enums.recrutement;

/** Situation d'une candidature (ne saute jamais d'etape). Apres DOSSIER_VALIDE : mis a jour par les blocs 2B et 3. */
public enum CandidatureStatut {
    RECUE,
    A_ETUDIER,
    DOSSIER_VALIDE,
    TEST_TECHNIQUE,
    ENTRETIEN_RH,
    ENTRETIEN_TECHNICIEN,
    ENTRETIEN_DG,
    CONFIRMATION_FINALE,
    RECRUTE,
    NON_RETENUE,
    LISTE_ATTENTE,
    RETIREE
}
