package com.netknife.tools.hygienechecklist;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import com.netknife.common.exception.ResourceNotFoundException;
import com.netknife.tools.hygienechecklist.check.HygieneCheck;
import com.netknife.tools.hygienechecklist.dto.HygieneChecklistDto;
import com.netknife.tools.hygienechecklist.dto.HygieneItemDto;
import com.netknife.tools.hygienechecklist.dto.UpdateHygieneItemRequest;
import com.netknife.tools.hygienechecklist.model.HygieneManualStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HygieneChecklistServiceTest {

    @Mock
    private HygieneCheck automaticOkCheck;
    @Mock
    private HygieneCheck automaticDangerCheck;
    @Mock
    private HygieneCheck manualCheck;
    @Mock
    private HygieneManualStatusRepository manualStatusRepository;

    private HygieneChecklistService service;

    @BeforeEach
    void setUp() {
        lenient().when(automaticOkCheck.id()).thenReturn("auto-ok");
        lenient().when(automaticOkCheck.title()).thenReturn("Comprobacion automatica en orden");
        lenient().when(automaticOkCheck.whyItMatters()).thenReturn("porque si");
        lenient().when(automaticOkCheck.isAutomatic()).thenReturn(true);
        lenient().when(automaticOkCheck.evaluate()).thenReturn(CheckResult.ok("todo bien"));

        lenient().when(automaticDangerCheck.id()).thenReturn("auto-danger");
        lenient().when(automaticDangerCheck.title()).thenReturn("Comprobacion automatica en peligro");
        lenient().when(automaticDangerCheck.whyItMatters()).thenReturn("porque si");
        lenient().when(automaticDangerCheck.isAutomatic()).thenReturn(true);
        lenient().when(automaticDangerCheck.evaluate())
                .thenReturn(new CheckResult(CheckStatus.PELIGRO, "mal", "detalle", "arreglo"));

        lenient().when(manualCheck.id()).thenReturn("manual-item");
        lenient().when(manualCheck.title()).thenReturn("Pregunta manual");
        lenient().when(manualCheck.whyItMatters()).thenReturn("porque si");
        lenient().when(manualCheck.isAutomatic()).thenReturn(false);
        lenient().when(manualCheck.evaluate())
                .thenReturn(new CheckResult(CheckStatus.NO_VERIFICABLE, "responde manualmente", null, "guia"));

        service = new HygieneChecklistService(
                List.of(automaticOkCheck, automaticDangerCheck, manualCheck), manualStatusRepository);
    }

    @Test
    void checklistAggregatesTotalsAndWorstStatus() {
        when(manualStatusRepository.findById("manual-item")).thenReturn(Optional.empty());

        HygieneChecklistDto checklist = service.getChecklist();

        assertThat(checklist.totalItems()).isEqualTo(3);
        assertThat(checklist.itemsInGoodShape()).isEqualTo(1);
        assertThat(checklist.overallStatus()).isEqualTo(CheckStatus.PELIGRO);
    }

    @Test
    void manualItemWithoutAnAnswerYetShowsNotVerifiablePrompt() {
        when(manualStatusRepository.findById("manual-item")).thenReturn(Optional.empty());

        HygieneChecklistDto checklist = service.getChecklist();

        HygieneItemDto manualItem = checklist.items().stream()
                .filter(item -> item.id().equals("manual-item")).findFirst().orElseThrow();
        assertThat(manualItem.status()).isEqualTo(CheckStatus.NO_VERIFICABLE);
        assertThat(manualItem.automatic()).isFalse();
    }

    @Test
    void manualItemWithAStoredAnswerUsesItInsteadOfEvaluate() {
        when(manualStatusRepository.findById("manual-item"))
                .thenReturn(Optional.of(new HygieneManualStatus("manual-item", CheckStatus.OK, java.time.Instant.now())));

        HygieneChecklistDto checklist = service.getChecklist();

        HygieneItemDto manualItem = checklist.items().stream()
                .filter(item -> item.id().equals("manual-item")).findFirst().orElseThrow();
        assertThat(manualItem.status()).isEqualTo(CheckStatus.OK);
        assertThat(manualItem.lastUpdated()).isNotNull();
    }

    @Test
    void updatingAManualItemPersistsTheAnswer() {
        // findById() debe reflejar lo que save() acaba de guardar, como haria una
        // tabla real: updateManualItem() lee de nuevo el repositorio tras guardar
        // para construir el DTO de respuesta.
        HygieneManualStatus[] stored = new HygieneManualStatus[1];
        when(manualStatusRepository.findById("manual-item"))
                .thenAnswer(invocation -> Optional.ofNullable(stored[0]));
        when(manualStatusRepository.save(org.mockito.ArgumentMatchers.any(HygieneManualStatus.class)))
                .thenAnswer(invocation -> {
                    stored[0] = invocation.getArgument(0);
                    return stored[0];
                });

        HygieneItemDto result = service.updateManualItem("manual-item", new UpdateHygieneItemRequest(CheckStatus.PELIGRO));

        assertThat(result.status()).isEqualTo(CheckStatus.PELIGRO);
    }

    @Test
    void cannotManuallyOverrideAnAutomaticItem() {
        assertThatThrownBy(() -> service.updateManualItem("auto-ok", new UpdateHygieneItemRequest(CheckStatus.OK)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void notVerifiableIsNotAValidManualAnswer() {
        // NO_VERIFICABLE representa "sin responder", no una respuesta valida del usuario.
        assertThatThrownBy(() -> service.updateManualItem("manual-item",
                new UpdateHygieneItemRequest(CheckStatus.NO_VERIFICABLE)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updatingAnUnknownItemThrowsNotFound() {
        assertThatThrownBy(() -> service.updateManualItem("does-not-exist", new UpdateHygieneItemRequest(CheckStatus.OK)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
