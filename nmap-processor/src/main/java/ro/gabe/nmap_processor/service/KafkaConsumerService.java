package ro.gabe.nmap_processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

  private final ScanService scanService;

  @KafkaListener(topics = "scan-results", groupId = "scan-consumers")
  public void consumeScanResult(String target) {
    log.info("Consumed message: {}", target);
    scanService.performScan(target);
  }
}
