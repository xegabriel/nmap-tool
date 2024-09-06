package ro.gabe.nmap_processor.model;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "nmap-scans")
public class Scan {

  @Id
  private String id;

  private String ip;

  private Set<Port> ports;
}

