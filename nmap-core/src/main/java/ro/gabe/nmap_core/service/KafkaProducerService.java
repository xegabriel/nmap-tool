package ro.gabe.nmap_core.service;


import java.util.HashSet;
import java.util.Set;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import ro.gabe.nmap_core.dto.ScansDTO;

@Service
public class KafkaProducerService {

  private static final String TOPIC = "scan-results";
  private final KafkaTemplate<String, String> kafkaTemplate;

  public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public Set<String> publishTargetsForScan(ScansDTO scansDTO) {
    Set<String> publishedClients = new HashSet<>();
    for (String target : scansDTO.getTargets()) {
      publishedClients.add(publishTargetForScan(target));
    }
    return publishedClients;
  }

  public String publishTargetForScan(String target) {
    ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, target);

    try {
      future.get();
      return target;
    } catch (Exception e) {
      throw new RuntimeException("Failed to send message to Kafka", e);
    }
  }
}
