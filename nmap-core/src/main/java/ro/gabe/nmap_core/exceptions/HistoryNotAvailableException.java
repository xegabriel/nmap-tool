package ro.gabe.nmap_core.exceptions;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@NoArgsConstructor
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class HistoryNotAvailableException extends RuntimeException {
  public HistoryNotAvailableException(String message) {
    super(message);
  }
}
