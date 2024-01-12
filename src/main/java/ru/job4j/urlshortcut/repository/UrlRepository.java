package ru.job4j.urlshortcut.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.urlshortcut.model.Url;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** {@code Url}-specific repository interface. */
public interface UrlRepository extends JpaRepository<Url, UUID> {

    /**
     * Retrieves an entity by its ID and put the log information to dedicated table..
     * @param uuid ID of the URL to retrieve and log
     * @return {@code Optional} with search result or empty {@code Optional} if nothing found
     */
    @NonNull
    @Transactional
    @Query(nativeQuery = true, value = "SELECT * FROM get_url(:uuid)")
    Optional<Url> findByIdAndLog(@NonNull UUID uuid);

    /**
     * Finds all the {@code Url} entities in the repository with the specified {@code Server} ID.
     * @param uuid ID of the server to search
     * @return list with search results or empty list if nothing found
     */
    @NonNull
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
