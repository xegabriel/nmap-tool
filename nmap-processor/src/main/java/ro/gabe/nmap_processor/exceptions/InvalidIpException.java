package ro.gabe.nmap_processor.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InvalidIpException extends RuntimeException {

  public InvalidIpException(String message) {
    super(message);
  }
}
