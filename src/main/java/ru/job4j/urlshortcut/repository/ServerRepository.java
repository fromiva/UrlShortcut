package ru.job4j.urlshortcut.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.urlshortcut.model.Server;

import java.util.UUID;

/** {@code Server}-specific repository interface. */
public interface ServerRepository extends JpaRepository<Server, UUID> {

    /**
     * Deletes {@code Server} entity from the repository and returns the result of the operation.
     * @param uuid ID of the server to delete
     * @return numer of the deleted rows
     */
    @Transactional
    @Modifying
    @Query("DELETE Server s WHERE s.uuid = :uuid")
    int deleteByIdAndReturnCount(@NonNull @Param("uuid") UUID uuid);

    /**
     * Updates password of a {@code Server}.
     * @param uuid ID of the server to update
     * @param password encoded password to set
     * @return numer of the updated rows
     */
    @Transactional
    @Modifying
    @Query("UPDATE Server s SET s.password = :password WHERE s.uuid = :uuid")
    int updatePasswordById(@NonNull @Param("uuid") UUID uuid,
                           @NonNull @Param("password") String password);
}