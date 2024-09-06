package ro.gabe.nmap_core.dto;

import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ScanDTO {

  private String id;
  private String ip;
  private Set<PortDTO> ports;
}
