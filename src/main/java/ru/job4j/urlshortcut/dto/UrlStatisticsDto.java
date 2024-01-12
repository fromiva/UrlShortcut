package ru.job4j.urlshortcut.dto;

import org.springframework.lang.NonNull;
import ru.job4j.urlshortcut.model.Url;

/**
 * Data Transfer Object to transfer information with {@code Url} statistics.
 * @param url {@code Url} entity
 * @param visited number of visits
 */
public record UrlStatisticsDto(@NonNull Url url, @NonNull Long visited) { }
