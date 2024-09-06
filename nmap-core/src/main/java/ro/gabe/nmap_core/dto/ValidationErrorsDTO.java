package ro.gabe.nmap_core.dto;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ValidationErrorsDTO {

  private final int status;
  private final String message;
  private Set<String> validationErrors = new HashSet<>();

  public void addValidationError(String message) {
    validationErrors.add(message);
  }
}
