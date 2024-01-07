package ru.job4j.urlshortcut.util;

import lombok.experimental.StandardException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Unchecked exception thrown when authentication is rejected. */
@StandardException
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AccessUnauthorizedException extends RuntimeException { }
