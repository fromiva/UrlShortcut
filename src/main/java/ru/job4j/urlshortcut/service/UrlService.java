package ru.job4j.urlshortcut.service;

import org.springframework.dao.DataIntegrityViolationException;
import ru.job4j.urlshortcut.model.Url;
import ru.job4j.urlshortcut.util.AccessForbiddenException;
import ru.job4j.urlshortcut.util.EntityNotFoundException;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/** {@code Url}-specific service interface. */
public interface UrlService {

    /**
     * Handles requests to save new {@code Url} entity.
     *
     * @param url entity to save
     * @param principal user authentication
     * @return saved entity with the actual generated ID
     * @throws AccessForbiddenException when {@code principal}'s name doesn't match URL hostname
     * @throws DataIntegrityViolationException when an attempt to insert data
     * results in violation of an integrity constraint
     */
    Url create(Url url, Principal principal);

    /**
     * Handles requests to get entity by ID.
     *
     * @param uuid ID of the target entity
     * @return target entity
     * @throws EntityNotFoundException when entity with specified ID cannot be found
     */
    Url getById(UUID uuid);

    /**
     * Handles requests to get all the entities with the specified server ID.
     *
     * @param uuid server ID
     * @return list with search results or empty list if nothing found
     */
    List<Url> getAllByServerId(UUID uuid);

    /**
     * Handles requests to delete {@code Url} entity by ID.
     *
     * @param uuid ID of the target entity
     * @param principal user authentication
     * @return operation result
     * @throws EntityNotFoundException when entity with specified ID cannot be found
     * @throws AccessForbiddenException when {@code principal}'s name doesn't match URL hostname
     */
    boolean deleteByIdAndPrincipal(UUID uuid, Principal principal);
}
