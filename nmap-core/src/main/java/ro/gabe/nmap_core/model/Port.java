package ro.gabe.nmap_core.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Port {

  private Long port;
  private String state;
  private String service;
}
