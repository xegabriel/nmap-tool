package ro.gabe.nmap_core.dto;

import java.util.Set;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.gabe.nmap_core.annotations.ValidIPs;

@Data
@NoArgsConstructor
public class ScansDTO {

  @ValidIPs
  @NotEmpty
  private Set<String> targets;
}
