package ro.gabe.nmap_core.dto;

import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.gabe.nmap_core.annotations.ValidIPs;

@Data
@NoArgsConstructor
public class ScanRequestDTO {
  @ValidIPs
  private Set<String> targets;
}
