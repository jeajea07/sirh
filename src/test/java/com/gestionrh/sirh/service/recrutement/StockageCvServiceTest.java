package com.gestionrh.sirh.service.recrutement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.gestionrh.sirh.enums.recrutement.TypeCv;
import com.gestionrh.sirh.exception.recrutement.RegleMetierException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class StockageCvServiceTest {

    @TempDir Path repertoire;

    @Test
    void accepteUnPdfEtProduitUnNomDeStockageAleatoire() throws Exception {
        StockageCvService service = new StockageCvService(repertoire.toString());
        MockMultipartFile fichier = new MockMultipartFile("fichierCv", "mon-cv.pdf", "application/pdf",
                "%PDF-1.7 contenu".getBytes());
        CvStocke cv = service.stocker(fichier);
        assertEquals(TypeCv.PDF, cv.type());
        assertEquals("mon-cv.pdf", cv.nomOriginal());
        assertTrue(Files.isRegularFile(Path.of(cv.chemin())));
        assertFalse(Path.of(cv.chemin()).getFileName().toString().contains("mon-cv"));
        service.supprimer(cv.chemin());
        assertFalse(Files.exists(Path.of(cv.chemin())));
    }

    @Test
    void rejetteUneExtensionMimeOuSignatureIncoherente() {
        StockageCvService service = new StockageCvService(repertoire.toString());
        assertThrows(RegleMetierException.class, () -> service.stocker(
                new MockMultipartFile("fichierCv", "cv.docx", "application/pdf", "PK".getBytes())));
        assertThrows(RegleMetierException.class, () -> service.stocker(
                new MockMultipartFile("fichierCv", "cv.pdf", "application/pdf", "pas un pdf".getBytes())));
    }

    @Test
    void rejetteLesFichiersDePlusDeCinqMegaOctets() {
        StockageCvService service = new StockageCvService(repertoire.toString());
        byte[] tropGros = new byte[(int) StockageCvService.TAILLE_MAX + 1];
        assertThrows(RegleMetierException.class, () -> service.stocker(
                new MockMultipartFile("fichierCv", "cv.pdf", "application/pdf", tropGros)));
    }
}
