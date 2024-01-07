package ru.job4j.urlshortcut.dto;

/**
 * Data Transfer Object to transfer information for JSON Web Token request.
 * @param uuid {@code Server} ID
 * @param password {@code Server} password
 */
public record LoginDto(String uuid, String password) { }
