package ru.job4j.urlshortcut.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;

/**
 * Model class to transfer information about server errors.
 * Main purpose of the class is to use as a model to JSON response body generation,
 * instead of the standard {@link ResponseStatusException} one.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ErrorDetail {

    /** Date and time when the error happens. */
    private ZonedDateTime timestamp = ZonedDateTime.now();

    /** Error status code according the equivalent HTTP response status code. */
    private int status;

    /** Error name according the equivalent HTTP response error name. */
    private String error;

    /** Error description message. */
    private String message;

    /** Relative path of the original HTTP request. */
    private String path;

    public ErrorDetail(HttpStatus httpStatus, String path) {
        this.status = httpStatus.value();
        this.error = httpStatus.getReasonPhrase();
        this.path = path;
    }

    public ErrorDetail(HttpStatus httpStatus, String path, String message) {
        this.status = httpStatus.value();
        this.error = httpStatus.getReasonPhrase();
        this.path = path;
        this.message = message;
    }
}
