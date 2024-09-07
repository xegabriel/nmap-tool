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

  private final ScanService scanService;
  private final ExecutorService executor = Executors.newFixedThreadPool(10);

  @KafkaListener(topics = "scan-results", groupId = "scan-consumers")
  public void consumeTargetToBeScanned(String target) {
    log.info("Consumed message: {}", target);
    executor.submit(() -> {
      ScanDTO scanDTO = scanService.performScan(target);
      log.info("The following ports were found for {}: {}", target, scanDTO);
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
