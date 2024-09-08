package ro.gabe.nmap_core.exceptions;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@NoArgsConstructor
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class KafkaDispatchingException extends RuntimeException{

  public KafkaDispatchingException(String message) {
    super(message);
  }
}
