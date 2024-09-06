package ro.gabe.nmap_core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import ro.gabe.nmap_core.annotations.validators.IPsValidator;

@Constraint(validatedBy = IPsValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIPs {

  String message() default "At least one IP address is invalid";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
