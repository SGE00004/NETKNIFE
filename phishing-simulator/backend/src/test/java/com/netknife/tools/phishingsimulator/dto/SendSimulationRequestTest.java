package com.netknife.tools.phishingsimulator.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SendSimulationRequestTest {

    private final Validator validator;

    SendSimulationRequestTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void aValidRequestHasNoViolations() {
        SendSimulationRequest request = new SendSimulationRequest(List.of("a@example.com"), "plantilla", true);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void moreThanFiveRecipientsIsRejected() {
        List<String> six = List.of("a@e.com", "b@e.com", "c@e.com", "d@e.com", "e@e.com", "f@e.com");
        SendSimulationRequest request = new SendSimulationRequest(six, "plantilla", true);

        Set<ConstraintViolation<SendSimulationRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void emptyRecipientListIsRejected() {
        SendSimulationRequest request = new SendSimulationRequest(List.of(), "plantilla", true);
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void malformedEmailIsRejected() {
        SendSimulationRequest request = new SendSimulationRequest(List.of("esto no es un email"), "plantilla", true);
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void missingConsentIsRejected() {
        SendSimulationRequest request = new SendSimulationRequest(List.of("a@example.com"), "plantilla", false);
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void blankTemplateIdIsRejected() {
        SendSimulationRequest request = new SendSimulationRequest(List.of("a@example.com"), " ", true);
        assertThat(validator.validate(request)).isNotEmpty();
    }
}
