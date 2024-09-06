package ro.gabe.nmap_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortDTO {

  private Long port;
  private String state;
  private String service;
}

