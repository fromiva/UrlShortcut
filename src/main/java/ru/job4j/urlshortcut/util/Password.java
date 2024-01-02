package ru.job4j.urlshortcut.util;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Custom Bean Validation annotation to validate a {@code Server} password. */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {
    String message()
            default "Password length must be 8-256 symbols and contains latin and digits only";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
