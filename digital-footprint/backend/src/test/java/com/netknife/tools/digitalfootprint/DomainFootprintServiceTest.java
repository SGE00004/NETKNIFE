package com.netknife.tools.digitalfootprint;

import com.netknife.common.dto.CheckStatus;
import com.netknife.common.geo.IpGeolocationLookup;
import com.netknife.tools.digitalfootprint.check.DiscoveredSubdomain;
import com.netknife.tools.digitalfootprint.check.DomainWhoisLookup;
import com.netknife.tools.digitalfootprint.check.SubdomainDiscoveryService;
import com.netknife.tools.digitalfootprint.check.WhoisSummary;
import com.netknife.tools.digitalfootprint.dto.DomainFootprintReportDto;
import com.netknife.tools.digitalfootprint.model.DomainFootprintReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainFootprintServiceTest {

    @Mock
    private SubdomainDiscoveryService subdomainDiscoveryService;
    @Mock
    private DomainWhoisLookup domainWhoisLookup;
    @Mock
    private IpGeolocationLookup ipGeolocationLookup;
    @Mock
    private DomainFootprintReportRepository repository;

    private DomainFootprintService service;

    @BeforeEach
    void setUp() {
        service = new DomainFootprintService(subdomainDiscoveryService, domainWhoisLookup, ipGeolocationLookup, repository);
        lenient().when(repository.save(any(DomainFootprintReport.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(domainWhoisLookup.lookup(anyString())).thenReturn(Optional.empty());
        lenient().when(ipGeolocationLookup.lookup(anyString())).thenReturn(Optional.empty());
    }

    @Test
    void rejectsABlankDomain() {
        assertThatThrownBy(() -> service.analyze(" ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aSensitiveSubdomainMakesTheOverallStatusAttention() {
        when(subdomainDiscoveryService.discover(anyString())).thenReturn(
                List.of(new DiscoveredSubdomain("admin.example.com", "1.2.3.4", CheckStatus.ATENCION)));

        DomainFootprintReportDto dto = service.analyze("example.com");

        assertThat(dto.subdomains()).hasSize(1);
        assertThat(dto.overallStatus()).isEqualTo(CheckStatus.ATENCION);
    }

    @Test
    void noSubdomainsFoundMeansOverallOk() {
        when(subdomainDiscoveryService.discover(anyString())).thenReturn(List.of());

        DomainFootprintReportDto dto = service.analyze("example.com");

        assertThat(dto.overallStatus()).isEqualTo(CheckStatus.OK);
    }

    @Test
    void includesWhoisDataWhenAvailable() {
        when(subdomainDiscoveryService.discover(anyString())).thenReturn(List.of());
        when(domainWhoisLookup.lookup(anyString())).thenReturn(
                Optional.of(new WhoisSummary("Registrar S.A.", "2020-01-01", "2027-01-01", List.of("ns1.example.com"))));

        DomainFootprintReportDto dto = service.analyze("example.com");

        assertThat(dto.whois()).isNotNull();
        assertThat(dto.whois().registrar()).isEqualTo("Registrar S.A.");
    }

    @Test
    void getLastReportReturnsEmptyWhenNeverAnalyzed() {
        when(repository.findTopByOrderByAnalyzedAtDesc()).thenReturn(Optional.empty());

        assertThat(service.getLastReport()).isEmpty();
    }
}
