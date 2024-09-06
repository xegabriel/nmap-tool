package ro.gabe.nmap_core.dto;

import lombok.Data;

@Data
public class PortDTO {
  private Long port;
  private String state;
  private String service;
}

