package com.netknife.tools.cryptojackingdetector.dto;

import java.util.List;

public record CryptojackingStatusDto(String overallStatus, List<CryptojackingAlertDto> activeAlerts) {
}
