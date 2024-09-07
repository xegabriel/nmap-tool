package ro.gabe.nmap_processor.dto;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanDTO {

  private String id;
  private String ip;
  private Set<PortDTO> ports;
  private LocalDateTime createdAt;
}