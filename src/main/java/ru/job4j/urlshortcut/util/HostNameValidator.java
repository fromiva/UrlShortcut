package ru.job4j.urlshortcut.util;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.MalformedURLException;
import java.net.URL;

/** Validator for {@link HostName} custom Bean Validation annotation. */
public class HostNameValidator implements ConstraintValidator<HostName, String> {

    @Override
    public void initialize(HostName host) { }

    @Override
    public boolean isValid(String host, ConstraintValidatorContext context) {
        if (host == null || host.isBlank()) {
            return false;
        }
        try {
            return new URL("https://" + host).getHost().equals(host);
        } catch (MalformedURLException exception) {
            return false;
        }
    }
}
