package ru.job4j.urlshortcut.service;

import ru.job4j.urlshortcut.model.Server;
import ru.job4j.urlshortcut.util.EntityNotFoundException;
import ru.job4j.urlshortcut.util.UnauthorizedException;

import java.util.UUID;

/** {@code Server}-specific service interface. */
public interface ServerService {

    /**
     * Handles requests to save new {@code Server} entity.
     *
     * @param server entity to save
     * @return saved entity with the actual generated ID
     */
    Server create(Server server);

    /**
     * Handles requests to get entity by ID.
     *
     * @param uuid ID of the target entity
     * @return target entity
     * @exception EntityNotFoundException when entity with specified ID cannot be found
     */
    Server getById(UUID uuid);

    /**
     * Handles requests to change entity security password.
     *
     * @param uuid    ID of the target entity
     * @param old     existing password
     * @param current new password to set
     * @return target entity
     * @throws EntityNotFoundException when entity with specified ID cannot be found
     */
    boolean updatePassword(UUID uuid, String old, String current);

    /**
     * Handles requests to delete {@code Server} entity by ID.
     *
     * @param uuid ID of the target entity
     * @return deleted entity
     * @throws EntityNotFoundException when entity with specified ID cannot be found
     * @throws UnauthorizedException when access credentials is incorrect
     */
    boolean deleteById(UUID uuid);

}
