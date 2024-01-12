package ru.job4j.urlshortcut.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.job4j.urlshortcut.model.UrlAccessRecord;

import java.util.UUID;

/** {@code Url} access log repository interface. */
public interface UrlAccessRecordRepository extends JpaRepository<UrlAccessRecord, Long> {

    /**
     * Counts a number of {@code Url} visits.
     * @param uuid ID of the URL to count
     * @return count result
     */
    long countByUrlUuid(UUID uuid);
}
