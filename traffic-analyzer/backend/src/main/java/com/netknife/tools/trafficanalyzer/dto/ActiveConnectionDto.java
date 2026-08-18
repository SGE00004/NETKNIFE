package com.netknife.tools.trafficanalyzer.dto;

import java.time.Instant;

public record ActiveConnectionDto(
        String remoteIp,
        Integer remotePort,
        String protocol,
        boolean encrypted,
        String processName,
        Long pid,
        String country,
        String city,
        String isp,
        String org,
        Instant firstSeen,
        Instant lastSeen,
        boolean isNew
) {
}
