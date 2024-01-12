package ru.job4j.urlshortcut.dto;

import org.springframework.lang.NonNull;
import ru.job4j.urlshortcut.model.Server;
import ru.job4j.urlshortcut.model.Url;

import java.util.List;

/**
 * Data Transfer Object to transfer information with {@code Server} statistics.
 * @param server {@code Server} entity
 * @param urls list of server-related {@code Url} entities
 */
public record ServerStatisticsDto(@NonNull Server server, @NonNull List<Url> urls) { }
