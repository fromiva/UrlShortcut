package ru.job4j.urlshortcut.dto;

import ru.job4j.urlshortcut.util.HostName;

import java.time.Instant;

/**
 * Data Transfer Object to transfer information about JSON Web Tokens.
 * @param host {@code Server} hostname the JWT is bound tp
 * @param created JWT creation timestamp
 * @param expired JWT expiration timestamp
 * @param token encoded JWT
 */
public record JwtDto(@HostName String host, Instant created, Instant expired, String token) { }
