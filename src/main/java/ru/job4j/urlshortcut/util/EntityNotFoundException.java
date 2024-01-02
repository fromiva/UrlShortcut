package ru.job4j.urlshortcut.util;

import lombok.experimental.StandardException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Unchecked exception thrown when an entity cannot be found or retrieved. */
@StandardException
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends RuntimeException { }
