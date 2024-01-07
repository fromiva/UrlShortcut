package ru.job4j.urlshortcut.dto;

import ru.job4j.urlshortcut.util.Password;

/**
 * Data Transfer Object to transfer information for {@code Server} password.
 * @param password password
 */
public record PasswordDto(@Password String password) { }
