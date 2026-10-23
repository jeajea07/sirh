package com.gestionrh.sirh.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.gestionrh.sirh.model.DocumentModel;
import com.gestionrh.sirh.repository.ContratRepository;
import com.gestionrh.sirh.repository.DocumentRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.OnboardingTacheRepository;
import com.gestionrh.sirh.repository.UtilisateurRepository;
import com.gestionrh.sirh.service.dto.DocumentForm;

class Bloc4DocumentServiceTest {

    @TempDir
    Path dossier;

    private DocumentService service() {
        DocumentRepository documents = mock(DocumentRepository.class);
        when(documents.save(any(DocumentModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        return new DocumentService(documents, mock(EmployeRepository.class),
                mock(ContratRepository.class), mock(OnboardingTacheRepository.class),
                mock(UtilisateurRepository.class), mock(AuditService.class), dossier.toString());
    }

    @Test
    void refuseUneExtensionNonAutorisee() {
        DocumentForm form = formulaireValide();
        MockMultipartFile fichier = new MockMultipartFile("fichier", "note.txt",
                "text/plain", "contenu".getBytes());

        assertThatThrownBy(() -> service().enregistrer(fichier, form, null))
                .isInstanceOf(Bloc4Exception.class)
                .hasMessageContaining("Format de fichier non autorisé");
    }

    @Test
    void refuseUnTypeDeDocumentInconnu() {
        DocumentForm form = formulaireValide();
        form.setTypeDocument("INCONNU");
        MockMultipartFile fichier = new MockMultipartFile("fichier", "piece.pdf",
                "application/pdf", "pdf".getBytes());

        assertThatThrownBy(() -> service().enregistrer(fichier, form, null))
                .isInstanceOf(Bloc4Exception.class)
                .hasMessage("Type de document invalide.");
    }

    @Test
    void enregistreUnDocumentValideEtConserveLeFichier() throws Exception {
        DocumentForm form = formulaireValide();
        MockMultipartFile fichier = new MockMultipartFile("fichier", "piece.pdf",
                "application/pdf", "contenu-pdf".getBytes());

        DocumentModel document = service().enregistrer(fichier, form, null);

        assertThat(document.getStatutValidation()).isEqualTo("RECU");
        assertThat(document.getNiveauConfidentialite()).isEqualTo("INTERNE");
        assertThat(document.getNomFichier()).isEqualTo("piece.pdf");
        assertThat(document.getTailleOctets()).isEqualTo(fichier.getSize());
        assertThat(dossier.resolve(document.getCheminStockage())).exists();
        assertThat(Files.readString(dossier.resolve(document.getCheminStockage())))
                .isEqualTo("contenu-pdf");
    }

    private DocumentForm formulaireValide() {
        DocumentForm form = new DocumentForm();
        form.setTitre("Pièce justificative");
        form.setTypeDocument("ADMINISTRATIF");
        form.setNiveauConfidentialite("INTERNE");
        return form;
    }
}
