package ro.gabe.nmap_core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScanDTO {

  private String id;
  private String ip;
  private Set<PortDTO> ports;
  private LocalDateTime createdAt;
}
