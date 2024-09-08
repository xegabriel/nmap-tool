package ro.gabe.nmap_processor.unit.service;

import static org.junit.jupiter.api.Assertions.*;

import ch.qos.logback.core.read.ListAppender;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent; import ch.qos.logback.core.Appender;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.test.util.ReflectionTestUtils;
import ro.gabe.nmap_processor.dto.PortDTO;
import ro.gabe.nmap_processor.exceptions.NmapScanException;
import ro.gabe.nmap_processor.helpers.ExponentialBackoffHelper;
import ro.gabe.nmap_processor.service.NmapService;

public class NmapServiceTest {

  @InjectMocks
  private NmapService nmapService;

  private final ExponentialBackoffHelper backoffHelper = new ExponentialBackoffHelper(5, 1000); // 5 retries, 1 second initial delay
  @Mock
  private Appender<ILoggingEvent> logWatcher;
  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    // Using only the first 100 ports instead of all 65535
    ReflectionTestUtils.setField(nmapService, "totalPorts", 100);
    // Decreasing the number of threads to 5
    ReflectionTestUtils.setField(nmapService, "threadCount", 5);
    // Initialize the log appender
    logWatcher = new ListAppender<>();
    logWatcher.start();
    ((Logger) LoggerFactory.getLogger(NmapService.class)).addAppender(logWatcher);
  }

  @AfterEach
  void teardown() {
    ((Logger) LoggerFactory.getLogger(NmapService.class)).detachAndStopAllAppenders();
  }

  @Test
  void testPerformNmapScan_Success() throws Exception {
    // Given
    String ipAddress = "example.com";

    // When
    Set<PortDTO> result = backoffHelper.retry(() -> nmapService.performNmapScan(ipAddress));

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.contains(PortDTO.builder().port(80L).state("open").service("http").build()));
  }

  @Test
  void testPerformNmapScan_InvalidIp() {
    // Given
    String invalidIpAddress = "invalid_ip";

    // When/Then
    assertThrows(NmapScanException.class, () -> nmapService.performNmapScan(invalidIpAddress));
  }

  @Test
  void testPerformNmapScan_NoOpenPorts() throws Exception {
    // Given
    String ipAddress = "example.com";
    ReflectionTestUtils.setField(nmapService, "totalPorts", 10);

    // When
    Set<PortDTO> result = backoffHelper.retry(() -> nmapService.performNmapScan(ipAddress));

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    assertPortsIntervalsFor10Ports5Threads();
  }

  private void assertPortsIntervalsFor10Ports5Threads() {
    List<String> expectedMessages = Arrays.asList(
        "Submitting range scan 1-2 for example.com",
        "Submitting range scan 3-4 for example.com",
        "Submitting range scan 5-6 for example.com",
        "Submitting range scan 7-8 for example.com",
        "Submitting range scan 9-10 for example.com"
    );

    // Fetch the log entries
    List<ILoggingEvent> logEvents = ((ListAppender<ILoggingEvent>) logWatcher).list;

    // Check that all expected messages are present in the log events
    for (String expectedMessage : expectedMessages) {
      boolean messageFound = logEvents.stream()
          .anyMatch(event -> event.getFormattedMessage().contains(expectedMessage));
      assertTrue(messageFound, "Expected log message not found: " + expectedMessage);
    }
  }

  @Test
  void testPerformNmapScan_MultipleOpenPorts() throws Exception {
    // Given
    ReflectionTestUtils.setField(nmapService, "totalPorts", 443);
    String ipAddress = "example.com";

    Set<PortDTO> ports = new HashSet<>();
    ports.add(PortDTO.builder().port(80L).state("open").service("http").build());
    ports.add(PortDTO.builder().port(443L).state("open").service("https").build());

    // When
    Set<PortDTO> result = backoffHelper.retry(() -> nmapService.performNmapScan(ipAddress));

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.contains(PortDTO.builder().port(80L).state("open").service("http").build()));
    assertTrue(result.contains(PortDTO.builder().port(443L).state("open").service("https").build()));
  }

}