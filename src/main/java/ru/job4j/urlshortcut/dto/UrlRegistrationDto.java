package ru.job4j.urlshortcut.dto;

import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

/**
 * Data Transfer Object to transfer information for new {@code Url} registration.
 * @param url URL address
 * @param expiration URL validity time in seconds or 0 if no expiration
 * @param description optional URL description
 */
public record UrlRegistrationDto(
        @Size(max = 8192, message = "URL maximum length must be 8192 symbols")
        String url,
        @PositiveOrZero(message = "Expiration time must be positive or 0 if no expiration")
        long expiration,
        @Size(max = 256, message = "Description maximum length must be 256 symbols")
        String description) { }
