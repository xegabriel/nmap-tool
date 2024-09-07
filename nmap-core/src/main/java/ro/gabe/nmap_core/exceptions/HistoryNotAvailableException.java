package ro.gabe.nmap_core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class HistoryNotAvailableException extends RuntimeException{

  public HistoryNotAvailableException() {
  }

  public HistoryNotAvailableException(String message) {
    super(message);
  }
}
