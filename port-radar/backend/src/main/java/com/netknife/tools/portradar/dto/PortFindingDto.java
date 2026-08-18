package com.netknife.tools.portradar.dto;

import com.netknife.common.dto.CheckStatus;
import com.netknife.tools.portradar.model.PortScanFinding;

public record PortFindingDto(
        int port,
        String protocolLabel,
        String banner,
        CheckStatus status,
        String summary,
        String howToFix
) {
    public static PortFindingDto fromEntity(PortScanFinding finding) {
        return new PortFindingDto(
                finding.getPort(),
                finding.getProtocolLabel(),
                finding.getBanner(),
                finding.getStatus(),
                finding.getSummary(),
                finding.getHowToFix());
    }
}
