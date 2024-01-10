package ru.job4j.urlshortcut.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.urlshortcut.model.Url;

import java.util.List;
import java.util.UUID;

/** {@code Url}-specific repository interface. */
public interface UrlRepository extends JpaRepository<Url, UUID> {

    /**
     * Finds all the {@code Url} entities in the repository with the specified {@code Server} ID.
     * @param uuid ID of the server to search
     * @return list with search results or empty list if nothing found
     */
    List<Url> findAllByServerUuid(@NonNull UUID uuid);

    /**
     * Deletes {@code Url} entity from the repository and returns the result of the operation.
     * @param uuid ID of the URL to delete
     * @return numer of the deleted rows
     */
    @Transactional
    @Modifying
    int deleteByUuid(@NonNull UUID uuid);
}
