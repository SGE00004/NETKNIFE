package com.netknife.tools.phishingsimulator;

import com.netknife.tools.phishingsimulator.dto.SendSimulationRequest;
import com.netknife.tools.phishingsimulator.dto.SimulationResultDto;
import com.netknife.tools.phishingsimulator.template.PhishingTemplate;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/phishing")
public class PhishingSimulatorController {

    private final PhishingSimulatorService service;

    public PhishingSimulatorController(PhishingSimulatorService service) {
        this.service = service;
    }

    @GetMapping("/templates")
    public List<PhishingTemplate> templates() {
        return service.listTemplates();
    }

    @PostMapping("/send")
    public List<SimulationResultDto> send(@Valid @RequestBody SendSimulationRequest request) {
        return service.send(request);
    }

    @GetMapping("/results")
    public List<SimulationResultDto> results() {
        return service.getResults();
    }

    /**
     * Endpoint publico sin autenticacion (como el resto de la app: es de un solo
     * usuario, sin login). Lo abre el destinatario de la simulacion, no el
     * usuario de NETKNIFE, asi que no puede requerir sesion.
     */
    @GetMapping("/track/{simulationId}")
    public ResponseEntity<Void> track(@PathVariable Long simulationId) {
        String redirectPath = service.trackClickAndGetRedirectPath(simulationId);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectPath)).build();
    }
}
