package ru.job4j.urlshortcut.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.urlshortcut.dto.UrlRegistrationDto;
import ru.job4j.urlshortcut.dto.UrlRegistrationDtoMapper;
import ru.job4j.urlshortcut.dto.UrlStatisticsDto;
import ru.job4j.urlshortcut.model.Url;
import ru.job4j.urlshortcut.service.UrlService;
import ru.job4j.urlshortcut.util.AccessForbiddenException;
import ru.job4j.urlshortcut.util.EntityNotFoundException;

import javax.validation.Valid;
import java.net.MalformedURLException;
import java.security.Principal;
import java.util.UUID;

/** Controller class to handle requests for {@code Url} entities. */
@RequiredArgsConstructor
@RestController
@RequestMapping("api/urls")
public class UrlController {

    private final UrlService service;

    /**
     * Handles request for new URL registration.
     * @param dto data transfer object with registration information
     * @param principal JWT authenticated user
     * @return persisted {@code Url} entity with actual ID
     */
    @PostMapping("register")
    public ResponseEntity<Url> urlRegister(
            @Valid @RequestBody(required = false) UrlRegistrationDto dto, Principal principal) {
        try {
            Url url = service.create(UrlRegistrationDtoMapper.toEntity(dto), principal);
            return ResponseEntity.ok(url);
        } catch (MalformedURLException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL is incorrect");
        } catch (AccessForbiddenException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "URL '" + dto.url() + "' already registered");
        }
    }

    /**
     * Handles request to get {@code Url} entity.
     * @param uuid ID of the {@code Url}
     * @param principal JWT authenticated user
     * @return persisted {@code Url} entity with specified ID
     */
    @GetMapping("{uuid}")
    public ResponseEntity<UrlStatisticsDto> getUrlByUuid(
            @PathVariable String uuid, Principal principal) {
        try {
            UUID id = UUID.fromString(uuid);
            Url url = service.getById(id);
            if (!url.getUrl().getHost().equals(principal.getName())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            Long visited = service.getUrlVisitsCount(id);
            return ResponseEntity.ok(new UrlStatisticsDto(url, visited));
        } catch (EntityNotFoundException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "URL with ID " + uuid + " not found.");
        }
    }

    /**
     * Handles requests to delete {@code Url} entity.
     * @param uuid ID of the {@code Url}
     * @param principal JWT authenticated user
     * @return status of the performed operation
     */
    @DeleteMapping("{uuid}")
    public ResponseEntity<Void> deleteUrl(@PathVariable String uuid, Principal principal) {
        try {
            boolean success = service.deleteByIdAndPrincipal(UUID.fromString(uuid), principal);
            return success ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (EntityNotFoundException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "URL with ID " + uuid + " not found.");
        }
    }
}
