package ru.job4j.urlshortcut.util;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Custom Bean Validation annotation to validate a {@code Server} hostname. */
@Documented
@Constraint(validatedBy = HostNameValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HostName {
    String message() default "Invalid host name";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
