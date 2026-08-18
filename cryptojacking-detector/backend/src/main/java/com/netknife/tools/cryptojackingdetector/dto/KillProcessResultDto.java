package com.netknife.tools.cryptojackingdetector.dto;

public record KillProcessResultDto(long pid, boolean success, String message) {
}
