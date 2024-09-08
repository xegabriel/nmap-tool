package ro.gabe.nmap_processor.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NmapScanException extends RuntimeException {

  public NmapScanException(String message) {
    super(message);
  }

  public NmapScanException(String message, Throwable cause) {
    super(message, cause);
  }
}
