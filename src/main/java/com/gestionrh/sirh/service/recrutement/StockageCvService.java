package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.enums.recrutement.TypeCv;
import com.gestionrh.sirh.exception.recrutement.RegleMetierException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Stockage privé des CV, avec validation de l'extension, du MIME et de la signature. */
@Service
public class StockageCvService {

    public static final long TAILLE_MAX = 5L * 1024 * 1024;
    private static final String MIME_PDF = "application/pdf";
    private static final String MIME_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private final Path dossier;

    public StockageCvService(@Value("${app.cv.dossier-stockage:${user.home}/sirh-cv}") String dossier) {
        this.dossier = Path.of(dossier).toAbsolutePath().normalize();
    }

    public CvStocke stocker(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) throw new RegleMetierException("Le fichier CV est obligatoire.");
        if (fichier.getSize() > TAILLE_MAX) throw new RegleMetierException("Le CV dépasse la taille maximale de 5 Mo.");
        String nom = fichier.getOriginalFilename();
        if (nom == null || nom.isBlank()) throw new RegleMetierException("Le nom du fichier CV est invalide.");
        TypeCv type = determinerType(nom, fichier.getContentType());
        byte[] contenu;
        try {
            contenu = fichier.getBytes();
        } catch (IOException e) {
            throw new RegleMetierException("Le fichier CV n'a pas pu être lu.");
        }
        verifierSignature(type, contenu);
        try {
            Files.createDirectories(dossier);
            String extension = type == TypeCv.PDF ? ".pdf" : ".docx";
            Path cible = dossier.resolve(UUID.randomUUID() + extension).normalize();
            if (!cible.startsWith(dossier)) throw new RegleMetierException("Chemin de stockage CV invalide.");
            Files.write(cible, contenu, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            return new CvStocke(nom, cible.toString(), type, contenu.length);
        } catch (IOException e) {
            throw new RegleMetierException("Le fichier CV n'a pas pu être stocké.");
        }
    }

    public void supprimer(String cheminStocke) {
        if (cheminStocke == null || cheminStocke.isBlank()) return;
        Path cible = Path.of(cheminStocke).toAbsolutePath().normalize();
        if (!cible.startsWith(dossier)) throw new RegleMetierException("Chemin de CV invalide.");
        try {
            Files.deleteIfExists(cible);
        } catch (IOException e) {
            throw new RegleMetierException("Le fichier CV temporaire n'a pas pu être supprimé.");
        }
    }

    public Path cheminControle(String cheminStocke) {
        if (cheminStocke == null || cheminStocke.isBlank()) throw new RegleMetierException("Chemin de CV invalide.");
        Path cible = Path.of(cheminStocke).toAbsolutePath().normalize();
        try {
            Path dossierReel = dossier.toRealPath();
            Path fichierReel = cible.toRealPath();
            if (!fichierReel.startsWith(dossierReel) || !Files.isRegularFile(fichierReel)) {
                throw new RegleMetierException("Fichier CV introuvable.");
            }
            return fichierReel;
        } catch (IOException e) {
            throw new RegleMetierException("Fichier CV introuvable.");
        }
    }

    private TypeCv determinerType(String nom, String mime) {
        String extension = nom.toLowerCase(Locale.ROOT);
        if (extension.endsWith(".pdf") && MIME_PDF.equalsIgnoreCase(mime)) return TypeCv.PDF;
        if (extension.endsWith(".docx") && MIME_DOCX.equalsIgnoreCase(mime)) return TypeCv.DOCX;
        throw new RegleMetierException("Le CV doit être un fichier PDF ou DOCX avec un type MIME valide.");
    }

    private void verifierSignature(TypeCv type, byte[] contenu) {
        if (type == TypeCv.PDF) {
            if (contenu.length < 4 || contenu[0] != '%' || contenu[1] != 'P' || contenu[2] != 'D' || contenu[3] != 'F') {
                throw new RegleMetierException("La signature du fichier PDF est invalide.");
            }
        } else if (contenu.length < 2 || contenu[0] != 'P' || contenu[1] != 'K') {
            throw new RegleMetierException("La signature du fichier DOCX est invalide.");
        }
    }
}
