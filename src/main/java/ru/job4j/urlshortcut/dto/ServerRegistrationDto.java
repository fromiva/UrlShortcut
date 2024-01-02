package ru.job4j.urlshortcut.dto;

import ru.job4j.urlshortcut.util.HostName;
import ru.job4j.urlshortcut.util.Password;

import javax.validation.constraints.Size;

/**
 * Data Transfer Object to transfer information for new {@code Server} registration.
 * @param host server host name
 * @param password server password
 * @param description optional server description
 */
public record ServerRegistrationDto(
        @HostName
        String host,
        @Password
        String password,
        @Size(max = 256, message = "Description maximum length must be 256 symbols")
        String description) { }
