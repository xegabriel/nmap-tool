package ro.gabe.nmap_core.exceptions;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ro.gabe.nmap_core.dto.ValidationErrorsDTO;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MethodArgumentNotValidExceptionHandler {

  private static final String VALIDATION_ERROR_MESSAGE = "Validation error!";

  @ResponseStatus(BAD_REQUEST)
  @ResponseBody
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ValidationErrorsDTO methodArgumentNotValidException(MethodArgumentNotValidException ex) {
    BindingResult result = ex.getBindingResult();
    List<FieldError> fieldErrors = result.getFieldErrors();
    ValidationErrorsDTO error = new ValidationErrorsDTO(BAD_REQUEST.value(), VALIDATION_ERROR_MESSAGE);
    for (org.springframework.validation.FieldError fieldError : fieldErrors) {
      error.addValidationError(fieldError.getDefaultMessage());
    }
    return error;
  }

  @ResponseStatus(BAD_REQUEST)
  @ResponseBody
  @ExceptionHandler(ConstraintViolationException.class)
  public ValidationErrorsDTO handleConstraintViolationException(ConstraintViolationException ex) {
    ValidationErrorsDTO errors = new ValidationErrorsDTO(BAD_REQUEST.value(), VALIDATION_ERROR_MESSAGE);
    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      errors.addValidationError(violation.getMessage());
    }
    return errors;
  }
}
