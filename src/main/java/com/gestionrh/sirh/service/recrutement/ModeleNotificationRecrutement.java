package com.gestionrh.sirh.service.recrutement;

/** Textes centraux des notifications du recrutement. */
public final class ModeleNotificationRecrutement {

    private ModeleNotificationRecrutement() { }

    public static String accuseReception(String numero) {
        return "Bonjour, votre dossier " + numero + " a bien été reçu. Votre dossier est en cours d'étude.";
    }

    public static String nouvelleCandidature(String numero) {
        return "Une nouvelle candidature (" + numero + ") attend l'étude du dossier.";
    }

    public static String nouvelleOffre(String reference) {
        return "L'offre " + reference + " a été créée et attend votre validation.";
    }

    public static String rappelDossiersEnRetard(String liste) {
        return "Les candidatures suivantes dépassent le délai de traitement de cinq jours ouvrables :\n" + liste;
    }

    public static String reponseNonConforme(String numero) {
        return "Après étude de votre dossier " + numero + ", nous ne pouvons pas donner une suite favorable à votre candidature.";
    }

    public static String offreFermee(String numero) {
        return "L'offre associée à votre candidature " + numero + " a été clôturée. Votre dossier est conservé.";
    }
}
