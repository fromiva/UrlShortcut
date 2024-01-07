package ru.job4j.urlshortcut.service;

import org.springframework.security.oauth2.jwt.Jwt;
import ru.job4j.urlshortcut.util.EntityNotFoundException;
import ru.job4j.urlshortcut.util.AccessUnauthorizedException;

import java.util.UUID;

/** Security-specific tasks service interface. */
public interface SecurityService {

    /**
     * Generates JSON Web Token to secure access in behalf of registered {@code Server}.
     * @param uuid {@code Server} UUID for which access token will be generated
     * @param password {@code Server} password
     * @return generated JSON Web Token
     * @throws EntityNotFoundException when {@code Server} cannot be found by specified UUID
     * @throws AccessUnauthorizedException when provided {@code password} is incorrect
     */
    Jwt generateToken(UUID uuid, String password);
}
