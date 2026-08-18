package com.netknife.tools.phishingsimulator;

import com.netknife.common.exception.MailDeliveryException;
import com.netknife.common.exception.ResourceNotFoundException;
import com.netknife.tools.phishingsimulator.dto.SendSimulationRequest;
import com.netknife.tools.phishingsimulator.dto.SimulationResultDto;
import com.netknife.tools.phishingsimulator.model.PhishingSimulation;
import com.netknife.tools.phishingsimulator.template.PhishingTemplateCatalog;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Nunca se envia un correo real en estos tests: JavaMailSender esta mockeado
 * por completo, incluido send(), asi que no hay conexion SMTP en ningun momento.
 */
@ExtendWith(MockitoExtension.class)
class PhishingSimulatorServiceTest {

    private static final String VALID_TEMPLATE_ID = "aviso-banco-falso";

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private PhishingSimulationRepository repository;

    private PhishingSimulatorService service;

    @BeforeEach
    void setUp() {
        service = new PhishingSimulatorService(
                new PhishingTemplateCatalog(), repository, mailSender, "netknife@example.com", "http://localhost:8080");
        lenient().when(mailSender.createMimeMessage()).thenAnswer(invocation -> new MimeMessage((jakarta.mail.Session) null));
        lenient().when(repository.save(any(PhishingSimulation.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void rejectsMoreThanFiveRecipientsAndNeverSendsAnything() {
        List<String> sixRecipients = List.of(
                "a@example.com", "b@example.com", "c@example.com",
                "d@example.com", "e@example.com", "f@example.com");
        SendSimulationRequest request = new SendSimulationRequest(sixRecipients, VALID_TEMPLATE_ID, true);

        assertThatThrownBy(() -> service.send(request)).isInstanceOf(IllegalArgumentException.class);
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void rejectsSendingWithoutConsentAndNeverSendsAnything() {
        SendSimulationRequest request = new SendSimulationRequest(List.of("a@example.com"), VALID_TEMPLATE_ID, false);

        assertThatThrownBy(() -> service.send(request)).isInstanceOf(IllegalArgumentException.class);
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void rejectsAnUnknownTemplateAndNeverSendsAnything() {
        SendSimulationRequest request = new SendSimulationRequest(List.of("a@example.com"), "no-existe", true);

        assertThatThrownBy(() -> service.send(request)).isInstanceOf(IllegalArgumentException.class);
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void rejectsAnEmptyRecipientListAndNeverSendsAnything() {
        SendSimulationRequest request = new SendSimulationRequest(List.of(), VALID_TEMPLATE_ID, true);

        assertThatThrownBy(() -> service.send(request)).isInstanceOf(IllegalArgumentException.class);
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendsOneEmailPerRecipientWithinTheLimit() {
        doNothing().when(mailSender).send(any(MimeMessage.class));
        SendSimulationRequest request = new SendSimulationRequest(
                List.of("a@example.com", "b@example.com"), VALID_TEMPLATE_ID, true);

        List<SimulationResultDto> results = service.send(request);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(SimulationResultDto::recipientEmail)
                .containsExactly("a@example.com", "b@example.com");
        assertThat(results).allMatch(r -> !r.clicked());
        verify(mailSender, times(2)).send(any(MimeMessage.class));
    }

    @Test
    void exactlyFiveRecipientsIsAllowed() {
        doNothing().when(mailSender).send(any(MimeMessage.class));
        List<String> five = List.of("a@example.com", "b@example.com", "c@example.com", "d@example.com", "e@example.com");
        SendSimulationRequest request = new SendSimulationRequest(five, VALID_TEMPLATE_ID, true);

        List<SimulationResultDto> results = service.send(request);

        assertThat(results).hasSize(5);
    }

    @Test
    void aSmtpFailureIsTranslatedToAClearMailDeliveryException() {
        doThrow(new MailSendException("no se pudo conectar")).when(mailSender).send(any(MimeMessage.class));
        SendSimulationRequest request = new SendSimulationRequest(List.of("a@example.com"), VALID_TEMPLATE_ID, true);

        assertThatThrownBy(() -> service.send(request))
                .isInstanceOf(MailDeliveryException.class)
                .hasMessageContaining("SMTP");
    }

    @Test
    void firstClickIsRecorded() {
        PhishingSimulation simulation = new PhishingSimulation(VALID_TEMPLATE_ID, "a@example.com", Instant.now());
        when(repository.findById(1L)).thenReturn(Optional.of(simulation));

        String redirectPath = service.trackClickAndGetRedirectPath(1L);

        assertThat(redirectPath).isEqualTo("/phishing/learn/" + VALID_TEMPLATE_ID);
        assertThat(simulation.isClicked()).isTrue();
        assertThat(simulation.getClickedAt()).isNotNull();
        verify(repository).save(simulation);
    }

    @Test
    void aSecondClickDoesNotOverwriteTheOriginalClickTimestamp() {
        Instant firstClickTime = Instant.now().minusSeconds(3600);
        PhishingSimulation simulation = new PhishingSimulation(VALID_TEMPLATE_ID, "a@example.com", Instant.now().minusSeconds(7200));
        simulation.setClicked(true);
        simulation.setClickedAt(firstClickTime);
        when(repository.findById(1L)).thenReturn(Optional.of(simulation));

        service.trackClickAndGetRedirectPath(1L);

        assertThat(simulation.getClickedAt()).isEqualTo(firstClickTime);
        verify(repository, never()).save(any(PhishingSimulation.class));
    }

    @Test
    void trackingAnUnknownSimulationThrowsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trackClickAndGetRedirectPath(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listTemplatesExposesExactlyThreeCuratedTemplates() {
        assertThat(service.listTemplates()).hasSize(3);
    }
}
