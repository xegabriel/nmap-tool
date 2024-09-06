package ro.gabe.nmap_core.annotations.validators;

import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import ro.gabe.nmap_core.annotations.ValidIPs;

public class IPsValidator implements ConstraintValidator<ValidIPs, Set<String>> {

  @Override
  public void initialize(ValidIPs constraintAnnotation) {
  }

  @Override
  public boolean isValid(Set<String> ips, ConstraintValidatorContext context) {
    return ips.stream().allMatch(IPValidatorUtil::isIpValid);
  }
}