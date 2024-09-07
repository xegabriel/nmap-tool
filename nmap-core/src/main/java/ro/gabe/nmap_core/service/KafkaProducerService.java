package ro.gabe.nmap_core.service;


import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import ro.gabe.nmap_core.dto.ScansDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

  private static final String TOPIC = "scan-results";
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final PublishedTargetsCache publishedTargetsCache;

  public Set<String> publishTargetsForScan(ScansDTO scansDTO) {
    Set<String> publishedClients = new HashSet<>();
    for (String target : scansDTO.getTargets()) {
      String publishedTarget = publishTargetForScan(target);
      if (publishedTarget != null) {
        publishedClients.add(publishedTarget);
      }
    }
    return publishedClients;
  }

  public String publishTargetForScan(String target) {
    if(publishedTargetsCache.isCached(target)) {
      log.info("Target {} skipped as it was recently processed", target);
      return null;
    }
    ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, target);
    try {
      future.get();
      publishedTargetsCache.cache(target);
      return target;
    } catch (Exception e) {
      throw new RuntimeException("Failed to send message to Kafka", e);
    }
  }
}
