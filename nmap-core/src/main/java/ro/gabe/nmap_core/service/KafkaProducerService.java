package ro.gabe.nmap_core.service;


import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import ro.gabe.nmap_core.dto.ScansDTO;
import ro.gabe.nmap_core.exceptions.KafkaDispatchingException;
import ro.gabe.nmap_core.exceptions.TooManyRequestsException;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

  public static final String TOPIC = "scan-results";
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final PublishedTargetsCache publishedTargetsCache;

  public ScansDTO publishTargetsForScan(ScansDTO scansDTO) {
    Set<String> publishedTargets = new HashSet<>();
    for (String target : scansDTO.getTargets()) {
      String publishedTarget = publishTargetForScan(target);
      if (publishedTarget != null) {
        publishedTargets.add(publishedTarget);
      }
    }
    if (publishedTargets.size() == 0) {
      throw new TooManyRequestsException(
          "Scan request too soon. Please wait at least " + PublishedTargetsCache.TARGET_TTL_SECONDS
              + " seconds before scanning the same IP again.");
    }
    return ScansDTO.builder().targets(publishedTargets).build();
  }

  public String publishTargetForScan(String target) {
    if (publishedTargetsCache.isCached(target)) {
      log.info("Target {} skipped as it was recently processed", target);
      return null;
    }
    ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, target);
    try {
      future.get();
      publishedTargetsCache.cache(target);
      return target;
    } catch (ExecutionException e) {
      log.error("Kafka dispatching failed. Target: {}, Error: {}", target, e.getLocalizedMessage());
      throw new KafkaDispatchingException("Failed to send " + target + " message to Kafka: " + e.getLocalizedMessage());
    } catch (InterruptedException e) {
      log.error("Thread interrupted while attempting to send message to Kafka. Target: {}, Error: {}", target,
          e.getLocalizedMessage());
      Thread.currentThread().interrupt();
      throw new KafkaDispatchingException("Failed to send " + target + " message to Kafka: " + e.getLocalizedMessage());
    }
  }
}
