package ro.gabe.nmap_core.annotations.validators;

import static ro.gabe.nmap_core.annotations.validators.IPValidatorUtil.isIpValid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import ro.gabe.nmap_core.annotations.ValidIP;

public class IPValidator implements ConstraintValidator<ValidIP, String> {

  @Override
  public void initialize(ValidIP constraintAnnotation) {
  }

  @Override
  public boolean isValid(String ip, ConstraintValidatorContext context) {
    return isIpValid(ip);
  }
}