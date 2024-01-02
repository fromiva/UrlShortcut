package ru.job4j.urlshortcut.util;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.regex.Pattern.compile;

/** Validator for {@link Password} custom Bean Validation annotation */
public class PasswordValidator implements ConstraintValidator<Password, String> {

    /** Regular expression to validate a password. */
    private final String re = "[a-zA-Z0-9]{8,256}";

    @Override
    public void initialize(Password password) { }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        return password != null && compile(re).matcher(password).matches();
    }
}
