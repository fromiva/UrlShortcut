package ru.job4j.urlshortcut.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.urlshortcut.dto.JwtDtoMapper;
import ru.job4j.urlshortcut.dto.LoginDto;
import ru.job4j.urlshortcut.dto.JwtDto;
import ru.job4j.urlshortcut.service.SecurityService;
import ru.job4j.urlshortcut.util.EntityNotFoundException;
import ru.job4j.urlshortcut.util.AccessUnauthorizedException;

import javax.validation.Valid;
import java.util.UUID;

/** Controller class to handle requests for JWT generation. */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/token")
public class SecurityController {

    private final SecurityService securityService;

    /**
     * Handles request for new JWT generation.
     * @param dto data transfer object with {@code Server} credentials
     * @return DTO with token and corresponding information
     */
    @PostMapping
    public ResponseEntity<JwtDto> generateToken(@Valid @RequestBody LoginDto dto) {
        try {
            Jwt jwt = securityService.generateToken(UUID.fromString(dto.uuid()), dto.password());
            return ResponseEntity.ok(JwtDtoMapper.toDto(jwt));
        } catch (EntityNotFoundException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Server with ID " + dto.uuid() + " not found.");
        } catch (AccessUnauthorizedException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage());
        }
    }
}
