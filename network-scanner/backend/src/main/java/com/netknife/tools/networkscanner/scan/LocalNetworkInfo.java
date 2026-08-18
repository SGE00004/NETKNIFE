package com.netknife.tools.networkscanner.scan;

public record LocalNetworkInfo(
        String localIpAddress,
        short prefixLength,
        String cidrNotation,
        String interfaceName
) {
}
