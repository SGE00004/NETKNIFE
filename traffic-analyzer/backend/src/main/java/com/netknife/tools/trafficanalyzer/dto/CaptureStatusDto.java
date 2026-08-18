package com.netknife.tools.trafficanalyzer.dto;

public record CaptureStatusDto(boolean running, boolean available, String unavailableReasonCode, String unavailableMessage) {
}
