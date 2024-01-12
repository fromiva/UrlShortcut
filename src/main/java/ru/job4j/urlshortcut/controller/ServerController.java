package ru.job4j.urlshortcut.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.urlshortcut.dto.PasswordDto;
import ru.job4j.urlshortcut.dto.ServerRegistrationDto;
import ru.job4j.urlshortcut.dto.ServerRegistrationDtoMapper;
import ru.job4j.urlshortcut.dto.ServerStatisticsDto;
import ru.job4j.urlshortcut.model.Server;
import ru.job4j.urlshortcut.model.Url;
import ru.job4j.urlshortcut.service.ServerService;
import ru.job4j.urlshortcut.service.UrlService;
import ru.job4j.urlshortcut.util.EntityNotFoundException;
import ru.job4j.urlshortcut.util.AccessUnauthorizedException;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

/** Controller class to handle requests for {@code Server} entities. */
@RequiredArgsConstructor
@RestController
@RequestMapping("api/servers")
public class ServerController {

    private final ServerService serverService;
    private final UrlService urlService;

    /**
     * Handles request for new server registration.
     * @param dto data transfer object with registration information
     * @return persisted {@code Server} entity with actual ID
     */
    @PostMapping("register")
    public ResponseEntity<Server> serverRegister(@Valid @RequestBody ServerRegistrationDto dto) {
        Server server = ServerRegistrationDtoMapper.toEntity(dto);
        try {
            server = serverService.create(server);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Server with host '" + dto.host() + "' already registered");
        }
        return ResponseEntity.ok(server);
    }

    /**
     * Handles request to get {@code Server} entity.
     * @param uuid ID of the {@code Server}
     * @param principal JWT authenticated user
     * @return persisted server entity with specified ID
     */
    @GetMapping("{uuid}")
    public ResponseEntity<ServerStatisticsDto> getServerByUuid(
            @PathVariable String uuid, Principal principal) {
        try {
            UUID id = UUID.fromString(uuid);
            Server server = serverService.getByIdAndHost(id, principal.getName());
            List<Url> urls = urlService.getAllByServerId(id);
            return ResponseEntity.ok(new ServerStatisticsDto(server, urls));
        } catch (EntityNotFoundException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Server with ID " + uuid + " not found.");
        }
    }

    /**
     * Handles requests to change server password.
     * @param uuid ID of the {@code Server}
     * @param dto data transfer object with security information
     * @param principal JWT authenticated user
     * @return status of the performed operation
     */
    @PatchMapping("{uuid}")
    public ResponseEntity<Void> updateServerPassword(
            @PathVariable String uuid, @Valid @RequestBody PasswordDto dto, Principal principal) {
        try {
            boolean success = serverService.updatePasswordByIdAndPrincipal(
                    UUID.fromString(uuid), principal, dto.password());
            return success ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (EntityNotFoundException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Server with ID " + uuid + " not found.");
        } catch (AccessUnauthorizedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }
    }

    /**
     * Handles requests to delete {@code Server} entity.
     * @param uuid ID of the {@code Server}
     * @param principal JWT authenticated user
     * @return status of the performed operation
     */
    @DeleteMapping("{uuid}")
    public ResponseEntity<Void> deleteServer(@PathVariable String uuid, Principal principal) {
        try {
            boolean success = serverService
                    .deleteByIdAndPrincipal(UUID.fromString(uuid), principal);
            return success ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (EntityNotFoundException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Server with ID " + uuid + " not found.");
        }
    }
}
