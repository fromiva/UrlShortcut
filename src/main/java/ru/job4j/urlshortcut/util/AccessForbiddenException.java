package ru.job4j.urlshortcut.util;

import lombok.experimental.StandardException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Unchecked exception thrown when authorization is rejected. */
@StandardException
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessForbiddenException extends RuntimeException { }
