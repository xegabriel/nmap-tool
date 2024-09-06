package ro.gabe.nmap_core.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ScanRequestDTO {
  private List<String> targets;
}
