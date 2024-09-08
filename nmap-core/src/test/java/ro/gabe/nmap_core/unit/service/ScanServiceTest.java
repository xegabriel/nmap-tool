package ro.gabe.nmap_core.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ro.gabe.nmap_core.dto.PortDTO;
import ro.gabe.nmap_core.dto.ScanDTO;
import ro.gabe.nmap_core.exceptions.HistoryNotAvailableException;
import ro.gabe.nmap_core.model.Port;
import ro.gabe.nmap_core.model.Scan;
import ro.gabe.nmap_core.repository.ScanRepository;
import ro.gabe.nmap_core.service.ScanService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ScanServiceTest {

  @Mock
  private ScanRepository scanRepository;

  @Mock
  private ModelMapper mapper;

  @InjectMocks
  private ScanService scanService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetScanResults() {
    // Given
    String ip = "192.168.1.1";
    Pageable pageable = PageRequest.of(0, 10);
    Scan scan = new Scan();
    scan.setIp(ip);
    Page<Scan> scanPage = new PageImpl<>(List.of(scan));

    when(scanRepository.findByIp(ip, pageable)).thenReturn(scanPage);
    when(mapper.map(any(Scan.class), eq(ScanDTO.class))).thenReturn(new ScanDTO());

    // When
    Page<ScanDTO> result = scanService.getScanResults(ip, pageable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(scanRepository).findByIp(ip, pageable);
    verify(mapper, times(1)).map(any(Scan.class), eq(ScanDTO.class));
  }

  @Test
  void testGetScanChangesResults_HistoryNotAvailable() {
    // Given
    String ip = "192.168.1.1";
    Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Scan> scanPage = new PageImpl<>(Collections.singletonList(new Scan()));

    when(scanRepository.findByIp(ip, pageable)).thenReturn(scanPage);

    // When & Then
    assertThrows(HistoryNotAvailableException.class, () -> scanService.getScanChangesResults(ip));
    verify(scanRepository).findByIp(ip, pageable);
  }

  @Test
  void testGetScanChangesResults_SuccessfulDiff() {
    // Given
    String ip = "192.168.1.1";
    Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));

    // Mocking two scans for comparison
    Set<Port> mostRecentPorts = new HashSet<>(Arrays.asList(
        Port.builder().port(80L).state("open").service("http").build(),
        Port.builder().port(443L).state("open").service("https").build()
    ));

    Set<Port> previousPorts = new HashSet<>(Collections.singletonList(
        Port.builder().port(80L).state("open").service("http").build()
    ));

    Scan mostRecentScan = new Scan();
    mostRecentScan.setIp(ip);
    mostRecentScan.setPorts(mostRecentPorts);

    Scan previousScan = new Scan();
    previousScan.setIp(ip);
    previousScan.setPorts(previousPorts);

    Page<Scan> scanPage = new PageImpl<>(Arrays.asList(mostRecentScan, previousScan));

    when(scanRepository.findByIp(ip, pageable)).thenReturn(scanPage);

    // Mapping Port to PortDTO inside ScanDTO
    when(mapper.map(any(Scan.class), eq(ScanDTO.class))).thenAnswer(invocation -> {
      Scan scan = invocation.getArgument(0);
      ScanDTO scanDTO = new ScanDTO();
      scanDTO.setIp(scan.getIp());

      // Converting ports to PortDTO
      Set<PortDTO> portDTOs = new HashSet<>();
      for (Port port : scan.getPorts()) {
        portDTOs.add(PortDTO.builder()
            .port(port.getPort())
            .state(port.getState())
            .service(port.getService())
            .build());
      }
      scanDTO.setPorts(portDTOs);
      return scanDTO;
    });

    // When
    ScanDTO result = scanService.getScanChangesResults(ip);

    // Then
    assertNotNull(result);
    assertEquals(ip, result.getIp());
    assertEquals(1, result.getPorts().size()); // Only the new port (443) should be in the diff
    assertTrue(result.getPorts().contains(PortDTO.builder().port(443L).state("open").service("https").build()));
    verify(scanRepository).findByIp(ip, pageable);
    verify(mapper, times(1)).map(any(Scan.class), eq(ScanDTO.class));
  }

  @Test
  void testCalculateDiff() {
    // Given
    Set<Port> mostRecentPorts = new HashSet<>(Arrays.asList(
        Port.builder().port(80L).state("open").service("http").build(),
        Port.builder().port(443L).state("open").service("https").build()
    ));

    Set<Port> previousPorts = new HashSet<>(Collections.singletonList(
        Port.builder().port(80L).state("open").service("http").build()
    ));

    Scan mostRecentScan = new Scan();
    mostRecentScan.setPorts(mostRecentPorts);

    Scan previousScan = new Scan();
    previousScan.setPorts(previousPorts);

    // Mocking the mapper behavior
    when(mapper.map(any(Scan.class), eq(ScanDTO.class))).thenAnswer(invocation -> {
      Scan scan = invocation.getArgument(0);
      ScanDTO scanDTO = new ScanDTO();

      // Converting ports to PortDTO
      Set<PortDTO> portDTOs = new HashSet<>();
      for (Port port : scan.getPorts()) {
        portDTOs.add(PortDTO.builder()
            .port(port.getPort())
            .state(port.getState())
            .service(port.getService())
            .build());
      }
      scanDTO.setPorts(portDTOs);
      return scanDTO;
    });

    // When
    ScanDTO result = scanService.calculateDiff(mostRecentScan, previousScan);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getPorts().size()); // Only port 443 should remain
    assertTrue(result.getPorts().contains(PortDTO.builder().port(443L).state("open").service("https").build()));
    verify(mapper).map(any(Scan.class), eq(ScanDTO.class));
  }
}