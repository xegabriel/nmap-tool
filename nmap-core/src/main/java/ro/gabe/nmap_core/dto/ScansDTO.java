package ro.gabe.nmap_core.dto;

import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ro.gabe.nmap_core.annotations.ValidIPs;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScansDTO {

  @ValidIPs
  @NotEmpty
  @Size(max = 10, message = "The maximum number of targets is 10")
  private Set<String> targets;
}
