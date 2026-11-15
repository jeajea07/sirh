-- Bloc 1 — Règle « Une demande de remplacement doit indiquer l'employé sortant ».
-- À exécuter après bloc1.sql (idempotent : peut être rejoué sur une base existante).
ALTER TABLE demande_besoin
    ADD COLUMN IF NOT EXISTS id_employe_sortant INTEGER REFERENCES employe(id_employe);
