package com.netknife.tools.phishingsimulator.dto;

import java.time.Instant;

public record SimulationResultDto(
        Long id,
        String templateId,
        String templateName,
        String recipientEmail,
        Instant sentAt,
        boolean clicked,
        Instant clickedAt
) {
}
