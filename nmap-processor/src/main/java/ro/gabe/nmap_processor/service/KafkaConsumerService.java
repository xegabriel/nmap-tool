package ro.gabe.nmap_processor.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ro.gabe.nmap_processor.dto.ScanDTO;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

  private static final String SCAN_RESULTS_TOPIC = "scan-results";
  private final ScanService scanService;
  private final ExecutorService executor = Executors.newFixedThreadPool(10);

  @KafkaListener(topics = SCAN_RESULTS_TOPIC, groupId = "scan-consumers")
  public void consumeTargetToBeScanned(String target) {
    log.info("Received target from Kafka topic '{}' for scanning: {}", SCAN_RESULTS_TOPIC, target);
    executor.submit(() -> {
      try {
        ScanDTO scanDTO = scanService.performScan(target);
        log.info("Scan completed successfully for target: {}. The following ports were found: {}.", target,
            scanDTO.getPorts());
      } catch (Exception e) {
        log.error("Error occurred while scanning target: {}. Error: {}", target, e.getMessage(), e);
      }
    });
  }

  @PreDestroy
  public void shutdownExecutor() {
    log.info("Shutting down executor service");
    executor.shutdown();
    try {
      if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
