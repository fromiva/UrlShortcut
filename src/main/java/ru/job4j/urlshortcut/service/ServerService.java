package ru.job4j.urlshortcut.service;

import org.springframework.dao.DataIntegrityViolationException;
import ru.job4j.urlshortcut.model.Server;
import ru.job4j.urlshortcut.util.AccessForbiddenException;
import ru.job4j.urlshortcut.util.EntityNotFoundException;

import java.security.Principal;
import java.util.UUID;

/** {@code Server}-specific service interface. */
public interface ServerService {

    /**
     * Handles requests to save new {@code Server} entity.
     *
     * @param server entity to save
     * @return saved entity with the actual generated ID
     * @throws DataIntegrityViolationException when an attempt to insert data
     * results in violation of an integrity constraint
     */
    Server create(Server server);

    /**
     * Handles requests to get entity by ID.
     *
     * @param uuid ID of the target entity
     * @return target entity
     * @throws EntityNotFoundException when entity with specified ID cannot be found
     */
    Server getById(UUID uuid);

    /**
     * Handles requests to get entity by ID and hostname.
     *
     * @param uuid ID of the target entity
     * @param host server hostname
     * @return target entity
     * @throws EntityNotFoundException when entity with specified ID cannot be found
     */
    Server getByIdAndHost(UUID uuid, String host);

    /**
     * Handles requests to get entity by hostname.
     *
     * @param host server hostname
     * @return target entity
     * @throws EntityNotFoundException when entity with specified ID cannot be found
     */
    Server getByHost(String host);

    /**
     * Handles requests to change entity security password.
     *
     * @param uuid      ID of the target {@code Server} entity
     * @param principal user authentication
     * @param password  new password to set
     * @return target entity
     * @throws EntityNotFoundException when entity with specified ID cannot be found
     * @throws AccessForbiddenException when {@code principal}'s name doesn't match server host
     */
    boolean updatePasswordByIdAndPrincipal(UUID uuid, Principal principal, String password);

    /**
     * Handles requests to delete {@code Server} entity by ID.
     *
     * @param uuid      ID of the target entity
     * @param principal user authentication
     * @return operation result
     * @throws EntityNotFoundException     when entity with specified ID cannot be found
     * @throws AccessForbiddenException when {@code principal}'s name doesn't match server host
     */
    boolean deleteByIdAndPrincipal(UUID uuid, Principal principal);

}
