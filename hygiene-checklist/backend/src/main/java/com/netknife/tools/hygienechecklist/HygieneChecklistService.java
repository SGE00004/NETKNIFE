package com.netknife.tools.hygienechecklist;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import com.netknife.common.exception.ResourceNotFoundException;
import com.netknife.tools.hygienechecklist.check.HygieneCheck;
import com.netknife.tools.hygienechecklist.dto.HygieneChecklistDto;
import com.netknife.tools.hygienechecklist.dto.HygieneItemDto;
import com.netknife.tools.hygienechecklist.dto.UpdateHygieneItemRequest;
import com.netknife.tools.hygienechecklist.model.HygieneManualStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class HygieneChecklistService {

    private final List<HygieneCheck> checks;
    private final HygieneManualStatusRepository manualStatusRepository;

    /**
     * Spring inyecta aqui automaticamente todos los beans que implementan
     * HygieneCheck: anadir una comprobacion nueva es tan simple como crear una
     * clase @Component que implemente la interfaz, sin tocar este servicio.
     */
    public HygieneChecklistService(List<HygieneCheck> checks, HygieneManualStatusRepository manualStatusRepository) {
        this.checks = checks;
        this.manualStatusRepository = manualStatusRepository;
    }

    @Transactional(readOnly = true)
    public HygieneChecklistDto getChecklist() {
        List<HygieneItemDto> items = checks.stream().map(this::toDto).toList();
        int okCount = (int) items.stream().filter(item -> item.status() == CheckStatus.OK).count();
        CheckStatus overall = CheckStatus.worstOf(items.stream().map(HygieneItemDto::status).toList());
        return new HygieneChecklistDto(items, items.size(), okCount, overall);
    }

    /**
     * Re-ejecuta las comprobaciones automaticas. En este diseno no hay estado
     * intermedio que "lanzar": las automaticas se evaluan en vivo en cada
     * peticion, asi que POST /check y GET /checklist devuelven el mismo
     * resultado. Se ofrecen ambos endpoints porque tienen intencion distinta
     * (accion explicita del usuario vs. carga de pantalla).
     */
    @Transactional(readOnly = true)
    public HygieneChecklistDto runChecks() {
        return getChecklist();
    }

    @Transactional
    public HygieneItemDto updateManualItem(String itemId, UpdateHygieneItemRequest request) {
        HygieneCheck check = checks.stream()
                .filter(c -> c.id().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No existe ningun item de higiene con id " + itemId));

        if (check.isAutomatic()) {
            throw new IllegalArgumentException(
                    "El item '" + itemId + "' se evalua automaticamente y no se puede modificar manualmente.");
        }
        if (request.status() == null || request.status() == CheckStatus.NO_VERIFICABLE) {
            throw new IllegalArgumentException("Indica un estado valido para tu respuesta (OK, ATENCION o PELIGRO).");
        }

        Instant now = Instant.now();
        HygieneManualStatus manualStatus = manualStatusRepository.findById(itemId)
                .orElseGet(() -> new HygieneManualStatus(itemId, request.status(), now));
        manualStatus.setStatus(request.status());
        manualStatus.setUpdatedAt(now);
        manualStatusRepository.save(manualStatus);

        return toDto(check);
    }

    private HygieneItemDto toDto(HygieneCheck check) {
        if (!check.isAutomatic()) {
            Optional<HygieneManualStatus> manual = manualStatusRepository.findById(check.id());
            if (manual.isPresent()) {
                HygieneManualStatus status = manual.get();
                return new HygieneItemDto(check.id(), check.title(), check.whyItMatters(), false,
                        status.getStatus(), null, null, status.getUpdatedAt());
            }
        }
        CheckResult result = check.evaluate();
        return new HygieneItemDto(check.id(), check.title(), check.whyItMatters(), check.isAutomatic(),
                result.status(), result.detail(), result.howToFix(), null);
    }
}
