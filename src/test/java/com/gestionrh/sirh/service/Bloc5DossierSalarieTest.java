package com.gestionrh.sirh.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.gestionrh.sirh.model.DocumentModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.HistoriqueSalaireModel;
import com.gestionrh.sirh.repository.AffectationRepository;
import com.gestionrh.sirh.repository.ContratRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.GrilleSalarialeRepository;
import com.gestionrh.sirh.repository.HistoriqueSalaireRepository;
import com.gestionrh.sirh.repository.UtilisateurRepository;

/** Règles de gestion du Bloc 5 — readme/Bloc5.md. */
class Bloc5DossierSalarieTest {

    private static final LocalDate AUJOURDHUI = LocalDate.now();

    // ------------------------------------------------------------ salaire (§9)

    private final HistoriqueSalaireRepository historiques = mock(HistoriqueSalaireRepository.class);
    private final EmployeRepository employes = mock(EmployeRepository.class);
    private final ContratRepository contrats = mock(ContratRepository.class);

    private SalaireService salaireService(BigDecimal salaireCourant, LocalDate debutCourant) {
        EmployeModel e = new EmployeModel();
        e.setId(1);
        when(employes.findById(1)).thenReturn(Optional.of(e));
        HistoriqueSalaireModel courant = new HistoriqueSalaireModel();
        courant.setSalaireBase(salaireCourant);
        courant.setDateEffet(debutCourant);
        when(historiques.enCours(1)).thenReturn(Optional.of(courant));
        when(historiques.save(any(HistoriqueSalaireModel.class))).thenAnswer(i -> i.getArgument(0));
        when(contrats.findByIdEmployeAndStatutWorkflowInOrderByDateDebutDesc(anyInt(), anyList())).thenReturn(List.of());
        return new SalaireService(historiques, employes, contrats, mock(GrilleSalarialeRepository.class),
                mock(CarriereService.class), mock(AuditService.class));
    }

    @Test
    void uneBaisseDeSalaireEstRefuseeSansAvenant() {
        SalaireService s = salaireService(new BigDecimal("1000000"), AUJOURDHUI.minusYears(1));
        assertThatThrownBy(() -> s.reviser(1, new BigDecimal("900000"), AUJOURDHUI, "AUGMENTATION_INDIVIDUELLE",
                null, new EmployeModel()))
                .isInstanceOf(Bloc4Exception.class).hasMessageContaining("RG-SA04");
    }

    @Test
    void uneRevisionClotLaLigneEnCoursSansLEcraser() {
        SalaireService s = salaireService(new BigDecimal("1000000"), AUJOURDHUI.minusYears(1));
        HistoriqueSalaireModel nouvelle = s.reviser(1, new BigDecimal("1100000"), AUJOURDHUI,
                "AUGMENTATION_INDIVIDUELLE", null, new EmployeModel());
        HistoriqueSalaireModel ancienne = historiques.enCours(1).orElseThrow();
        assertThat(ancienne.getDateFin()).isEqualTo(AUJOURDHUI.minusDays(1));
        assertThat(ancienne.getSalaireBase()).isEqualByComparingTo("1000000");
        assertThat(nouvelle.getDateFin()).isNull();
        assertThat(nouvelle.getSalaireBase()).isEqualByComparingTo("1100000");
    }

    @Test
    void laDateDEffetDoitSuivreLeSalaireEnVigueur() {
        SalaireService s = salaireService(new BigDecimal("1000000"), AUJOURDHUI);
        assertThatThrownBy(() -> s.reviser(1, new BigDecimal("1200000"), AUJOURDHUI, "PROMOTION",
                null, new EmployeModel()))
                .isInstanceOf(Bloc4Exception.class).hasMessageContaining("postérieure");
    }

    @Test
    void uneCorrectionExigeUnCommentaire() {
        SalaireService s = salaireService(new BigDecimal("1000000"), AUJOURDHUI.minusYears(1));
        assertThatThrownBy(() -> s.reviser(1, new BigDecimal("950000"), AUJOURDHUI, "CORRECTION",
                " ", new EmployeModel()))
                .isInstanceOf(Bloc4Exception.class).hasMessageContaining("commentaire");
    }

    // ------------------------------------------------------------ validité des pièces (§10)

    private static DocumentModel pieceExpirantLe(LocalDate expiration) {
        DocumentModel d = new DocumentModel();
        d.setStatutValidation("VERIFIE");
        d.setDateExpiration(expiration);
        return d;
    }

    @Test
    void unePieceSansExpirationResteValide() {
        assertThat(DossierDocumentService.statutSelonDate(pieceExpirantLe(null), AUJOURDHUI)).isEqualTo("VERIFIE");
    }

    @Test
    void unePieceProcheDeSonExpirationEstARenouveler() {
        assertThat(DossierDocumentService.statutSelonDate(pieceExpirantLe(AUJOURDHUI.plusDays(10)), AUJOURDHUI))
                .isEqualTo("A_RENOUVELER");
        assertThat(DossierDocumentService.statutSelonDate(pieceExpirantLe(AUJOURDHUI.plusDays(60)), AUJOURDHUI))
                .isEqualTo("VERIFIE");
    }

    @Test
    void unePieceDontLaDateEstDepasseeEstExpiree() {
        assertThat(DossierDocumentService.statutSelonDate(pieceExpirantLe(AUJOURDHUI.minusDays(1)), AUJOURDHUI))
                .isEqualTo("EXPIRE");
        assertThat(DossierDocumentService.statutSelonDate(pieceExpirantLe(AUJOURDHUI), AUJOURDHUI))
                .isEqualTo("A_RENOUVELER");
    }

    // ------------------------------------------------------------ sortie (§12)

    private StatutSalarieService statutService(EmployeModel e) {
        EmployeRepository repo = mock(EmployeRepository.class);
        when(repo.findById(e.getId())).thenReturn(Optional.of(e));
        return new StatutSalarieService(repo, mock(ContratRepository.class), mock(AffectationRepository.class),
                mock(UtilisateurRepository.class), mock(ContratService.class), mock(CarriereService.class),
                mock(NotificationService.class), mock(AuditService.class));
    }

    private static EmployeModel salarieActif() {
        EmployeModel e = new EmployeModel();
        e.setId(7);
        e.setStatutAdministratif("ACTIF");
        return e;
    }

    @Test
    void uneSortieFutureMetLeSalarieEnPreavisSansLeDesactiver() {
        EmployeModel e = salarieActif();
        EmployeModel rh = new EmployeModel();
        rh.setId(1);
        statutService(e).enregistrerSortie(7, "DEMISSION", null, AUJOURDHUI, AUJOURDHUI.plusMonths(1), rh);
        assertThat(e.getStatutAdministratif()).isEqualTo("EN_PREAVIS");
        assertThat(e.isActif()).isTrue();
        assertThat(e.getMotifSortie()).isEqualTo("Démission");
    }

    @Test
    void laDateDeSortieNePeutPasPrecederLaNotification() {
        assertThatThrownBy(() -> statutService(salarieActif()).enregistrerSortie(7, "DEMISSION", null,
                AUJOURDHUI, AUJOURDHUI.minusDays(1), new EmployeModel()))
                .isInstanceOf(Bloc4Exception.class).hasMessageContaining("précéder");
    }

    @Test
    void seulUnSalarieActifPeutEtreSuspendu() {
        EmployeModel e = salarieActif();
        e.setStatutAdministratif("EN_PREAVIS");
        assertThatThrownBy(() -> statutService(e).suspendre(7, "Maladie", AUJOURDHUI, new EmployeModel()))
                .isInstanceOf(Bloc4Exception.class);
    }

    @Test
    void uneSortieAnnuleeRemetLeSalarieActif() {
        EmployeModel e = salarieActif();
        EmployeModel rh = new EmployeModel();
        rh.setId(1);
        StatutSalarieService s = statutService(e);
        s.enregistrerSortie(7, "DEMISSION", null, AUJOURDHUI, AUJOURDHUI.plusMonths(1), rh);
        s.annulerSortie(7, "Le salarié revient sur sa démission", rh);
        assertThat(e.getStatutAdministratif()).isEqualTo("ACTIF");
        assertThat(e.getDateSortie()).isNull();
    }
}
