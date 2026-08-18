package com.netknife.tools.digitalfootprint.check;

import java.util.List;

public record WhoisSummary(
        String registrar,
        String createdDate,
        String expiresDate,
        List<String> nameservers
) {
}
