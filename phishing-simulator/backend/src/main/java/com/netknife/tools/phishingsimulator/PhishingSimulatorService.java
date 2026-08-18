package com.netknife.tools.phishingsimulator;

import com.netknife.common.exception.MailDeliveryException;
import com.netknife.common.exception.ResourceNotFoundException;
import com.netknife.tools.phishingsimulator.dto.SendSimulationRequest;
import com.netknife.tools.phishingsimulator.dto.SimulationResultDto;
import com.netknife.tools.phishingsimulator.model.PhishingSimulation;
import com.netknife.tools.phishingsimulator.template.PhishingTemplate;
import com.netknife.tools.phishingsimulator.template.PhishingTemplateCatalog;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PhishingSimulatorService {

    /**
     * Limite maximo de destinatarios por envio, validado aqui ademas de en la
     * anotacion @Size del DTO: defensa en profundidad frente a saltarse la
     * validacion de Bean Validation por cualquier via (ej. una llamada directa
     * al servicio desde un test o una futura integracion).
     */
    static final int MAX_RECIPIENTS = 5;

    private final PhishingTemplateCatalog templateCatalog;
    private final PhishingSimulationRepository repository;
    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String trackingBaseUrl;

    public PhishingSimulatorService(
            PhishingTemplateCatalog templateCatalog,
            PhishingSimulationRepository repository,
            JavaMailSender mailSender,
            @Value("${netknife.phishing.from-address:}") String fromAddress,
            @Value("${netknife.phishing.tracking-base-url:http://localhost:8080}") String trackingBaseUrl) {
        this.templateCatalog = templateCatalog;
        this.repository = repository;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.trackingBaseUrl = trackingBaseUrl;
    }

    public List<PhishingTemplate> listTemplates() {
        return templateCatalog.listAll();
    }

    @Transactional
    public List<SimulationResultDto> send(SendSimulationRequest request) {
        PhishingTemplate template = templateCatalog.findById(request.templateId())
                .orElseThrow(() -> new IllegalArgumentException("No existe la plantilla '" + request.templateId() + "'"));

        if (!request.consentConfirmed()) {
            throw new IllegalArgumentException(
                    "Debes confirmar que tienes consentimiento para enviar esta simulacion.");
        }
        if (request.recipients() == null || request.recipients().isEmpty()) {
            throw new IllegalArgumentException("Indica al menos un destinatario.");
        }
        if (request.recipients().size() > MAX_RECIPIENTS) {
            throw new IllegalArgumentException("Como maximo " + MAX_RECIPIENTS + " destinatarios por envio.");
        }

        List<SimulationResultDto> results = new ArrayList<>();
        for (String recipient : request.recipients()) {
            PhishingSimulation simulation = new PhishingSimulation(template.id(), recipient, Instant.now());
            repository.save(simulation);
            sendEmail(template, simulation);
            results.add(toDto(simulation, template));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public List<SimulationResultDto> getResults() {
        return repository.findAllByOrderBySentAtDesc().stream()
                .map(simulation -> toDto(simulation, templateCatalog.findById(simulation.getTemplateId()).orElse(null)))
                .toList();
    }

    /** @return la ruta (relativa) a la que debe redirigirse al destinatario tras registrar el clic. */
    @Transactional
    public String trackClickAndGetRedirectPath(Long simulationId) {
        PhishingSimulation simulation = repository.findById(simulationId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe ninguna simulacion con id " + simulationId));
        if (!simulation.isClicked()) {
            simulation.setClicked(true);
            simulation.setClickedAt(Instant.now());
            repository.save(simulation);
        }
        return "/phishing/learn/" + simulation.getTemplateId();
    }

    private void sendEmail(PhishingTemplate template, PhishingSimulation simulation) {
        String trackingUrl = trackingBaseUrl + "/api/phishing/track/" + simulation.getId();
        String body = template.bodyHtml().replace("{{TRACKING_URL}}", trackingUrl);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(simulation.getRecipientEmail());
            helper.setFrom(fromAddress);
            helper.setSubject("[Simulacion NETKNIFE] " + template.subject());
            helper.setText(body, true);
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new MailDeliveryException(
                    "No se ha podido enviar el correo a " + simulation.getRecipientEmail() + ". Comprueba la "
                            + "configuracion SMTP (variables de entorno SMTP_HOST, SMTP_PORT, SMTP_USERNAME, "
                            + "SMTP_PASSWORD).",
                    e);
        }
    }

    private SimulationResultDto toDto(PhishingSimulation simulation, PhishingTemplate template) {
        return new SimulationResultDto(
                simulation.getId(),
                simulation.getTemplateId(),
                template != null ? template.name() : simulation.getTemplateId(),
                simulation.getRecipientEmail(),
                simulation.getSentAt(),
                simulation.isClicked(),
                simulation.getClickedAt());
    }
}
