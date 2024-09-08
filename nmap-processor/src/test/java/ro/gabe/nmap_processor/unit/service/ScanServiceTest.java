package ro.gabe.nmap_processor.unit.service;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.HashSet;
import ro.gabe.nmap_processor.dto.PortDTO;
import ro.gabe.nmap_processor.dto.ScanDTO;
import ro.gabe.nmap_processor.model.Scan;
import ro.gabe.nmap_processor.repository.ScanRepository;
import ro.gabe.nmap_processor.service.NmapService;
import ro.gabe.nmap_processor.service.ScanService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ScanServiceTest {

  @Mock
  private ScanRepository scanRepository;

  @Mock
  private NmapService nmapService;

  @Mock
  private ModelMapper mapper;

  @InjectMocks
  private ScanService scanService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testPerformScan_Success() {
    // Given
    String target = "192.168.1.1";

    // Mocking NmapService to return a set of ports found
    Set<PortDTO> portsFound = new HashSet<>();
    portsFound.add(PortDTO.builder().port(80L).state("open").service("http").build());
    portsFound.add(PortDTO.builder().port(443L).state("open").service("https").build());

    // Mocking the ScanDTO and Scan entities
    ScanDTO scanDTO = ScanDTO.builder().ip(target).ports(portsFound).build();
    Scan scanModel = new Scan();

    // Mocking the behavior of the mapper
    when(nmapService.performNmapScan(target)).thenReturn(portsFound);
    when(mapper.map(any(ScanDTO.class), eq(Scan.class))).thenReturn(scanModel);
    when(scanRepository.save(any(Scan.class))).thenReturn(scanModel);
    when(mapper.map(any(Scan.class), eq(ScanDTO.class))).thenReturn(scanDTO);

    // When
    ScanDTO result = scanService.performScan(target);

    // Then
    assertNotNull(result);
    assertEquals(target, result.getIp());
    assertEquals(2, result.getPorts().size());
    verify(nmapService).performNmapScan(target);
    verify(scanRepository).save(scanModel);
    verify(mapper).map(any(ScanDTO.class), eq(Scan.class));
    verify(mapper).map(any(Scan.class), eq(ScanDTO.class));
  }

  @Test
  void testPerformScan_EmptyPortsFound() {
    // Given
    String target = "192.168.1.1";

    // Mocking NmapService to return an empty set of ports found
    Set<PortDTO> emptyPorts = new HashSet<>();

    // Mocking the ScanDTO and Scan entities
    ScanDTO scanDTO = ScanDTO.builder().ip(target).ports(emptyPorts).build();
    Scan scanModel = new Scan();

    // Mocking the behavior of the mapper
    when(nmapService.performNmapScan(target)).thenReturn(emptyPorts);
    when(mapper.map(any(ScanDTO.class), eq(Scan.class))).thenReturn(scanModel);
    when(scanRepository.save(any(Scan.class))).thenReturn(scanModel);
    when(mapper.map(any(Scan.class), eq(ScanDTO.class))).thenReturn(scanDTO);

    // When
    ScanDTO result = scanService.performScan(target);

    // Then
    assertNotNull(result);
    assertEquals(target, result.getIp());
    assertTrue(result.getPorts().isEmpty());
    verify(nmapService).performNmapScan(target);
    verify(scanRepository).save(scanModel);
    verify(mapper).map(any(ScanDTO.class), eq(Scan.class));
    verify(mapper).map(any(Scan.class), eq(ScanDTO.class));
  }

  @Test
  void testPerformScan_NmapServiceThrowsException() {
    // Given
    String target = "192.168.1.1";

    // Mock NmapService to throw an exception
    when(nmapService.performNmapScan(target)).thenThrow(new RuntimeException("Nmap failed"));

    // When & Then
    RuntimeException exception = assertThrows(RuntimeException.class, () -> scanService.performScan(target));
    assertEquals("Nmap failed", exception.getMessage());
    verify(nmapService).performNmapScan(target);
    verify(scanRepository, never()).save(any());
    verify(mapper, never()).map(any(), any());
  }
}
