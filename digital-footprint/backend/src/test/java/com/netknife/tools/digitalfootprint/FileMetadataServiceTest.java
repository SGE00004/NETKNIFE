package com.netknife.tools.digitalfootprint;

import com.netknife.common.dto.CheckStatus;
import com.netknife.tools.digitalfootprint.check.FileMetadataExtractor;
import com.netknife.tools.digitalfootprint.check.RawMetadataEntry;
import com.netknife.tools.digitalfootprint.dto.FileMetadataReportDto;
import com.netknife.tools.digitalfootprint.model.FileMetadataReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileMetadataServiceTest {

    @Mock
    private FileMetadataExtractor extractor;
    @Mock
    private FileMetadataReportRepository repository;

    private FileMetadataService service;

    @BeforeEach
    void setUp() {
        service = new FileMetadataService(extractor, repository);
        lenient().when(repository.save(any(FileMetadataReport.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void rejectsAnEmptyFile() {
        MockMultipartFile empty = new MockMultipartFile("file", "vacio.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> service.analyze(empty)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void gpsMetadataMakesTheOverallStatusDanger() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", "contenido".getBytes());
        when(extractor.extract(any(), anyString())).thenReturn(
                List.of(new RawMetadataEntry("Ubicacion GPS", "40.4,-3.7"), new RawMetadataEntry("Software usado", "Camara X")));

        FileMetadataReportDto dto = service.analyze(file);

        assertThat(dto.overallStatus()).isEqualTo(CheckStatus.PELIGRO);
        assertThat(dto.findings()).hasSize(2);
    }

    @Test
    void noMetadataFoundMeansOverallOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "limpio.pdf", "application/pdf", "contenido".getBytes());
        when(extractor.extract(any(), anyString())).thenReturn(List.of());

        FileMetadataReportDto dto = service.analyze(file);

        assertThat(dto.overallStatus()).isEqualTo(CheckStatus.OK);
        assertThat(dto.findings()).isEmpty();
    }

    @Test
    void getLastReportReturnsEmptyWhenNeverAnalyzed() {
        when(repository.findTopByOrderByAnalyzedAtDesc()).thenReturn(Optional.empty());

        assertThat(service.getLastReport()).isEmpty();
    }
}
