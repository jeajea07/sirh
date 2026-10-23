package com.gestionrh.sirh.dto;

public class NotificationVue {

    private final String message;
    private final String dateFr;
    private final String lien;
    private final boolean lue;

    public NotificationVue(String message, String dateFr, String lien, boolean lue) {
        this.message = message;
        this.dateFr = dateFr;
        this.lien = lien;
        this.lue = lue;
    }

    public String getMessage() { return message; }
    public String getDateFr() { return dateFr; }

    public String getLien() { return lien; }
    public boolean isLue() { return lue; }
}
