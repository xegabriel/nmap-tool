package ro.gabe.nmap_processor.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ExecutorService;
import ro.gabe.nmap_processor.dto.ScanDTO;
import ro.gabe.nmap_processor.service.KafkaConsumerService;
import ro.gabe.nmap_processor.service.ScanService;

import static org.mockito.Mockito.*;

public class KafkaConsumerServiceTest {

  @Mock
  private ScanService scanService;

  @Mock
  private ExecutorService executorService;

  @InjectMocks
  private KafkaConsumerService kafkaConsumerService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testConsumeTargetToBeScanned_SuccessfulScan() {
    // Given
    String target = "192.168.1.1";
    ScanDTO scanDTO = new ScanDTO();
    scanDTO.setIp(target);

    // Mocking the executorService to run the task immediately
    doAnswer(invocation -> {
      Runnable task = invocation.getArgument(0);
      task.run();
      return null;
    }).when(executorService).submit(any(Runnable.class));

    // Mocking the ScanService to return a successful scan result
    when(scanService.performScan(target)).thenReturn(scanDTO);

    // When
    kafkaConsumerService.consumeTargetToBeScanned(target);

    // Then
    verify(scanService, times(1)).performScan(target);
  }
}