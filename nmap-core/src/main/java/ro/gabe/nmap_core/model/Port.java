package ro.gabe.nmap_core.model;

import lombok.Data;

@Data
public class Port {

  private Long port;
  private String state;
  private String service;
}
