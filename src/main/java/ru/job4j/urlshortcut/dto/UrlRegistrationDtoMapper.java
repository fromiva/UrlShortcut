package ru.job4j.urlshortcut.dto;

import ru.job4j.urlshortcut.model.Url;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Mapper for {@link UrlRegistrationDto} Data Transfer Object */
public class UrlRegistrationDtoMapper {

    /**
     * Method to map an url to a DTO.
     * @param url to map
     * @return DTO
     */
    public static UrlRegistrationDto toDto(Url url) {
        return new UrlRegistrationDto(
                url.getUrl().toString(),
                Duration.between(url.getCreated(), url.getExpired()).toSeconds(),
                url.getDescription());
    }

    /**
     * Method to map a DTO to an entity.
     * @param dto to map
     * @return entity
     */
    public static Url toEntity(UrlRegistrationDto dto) throws MalformedURLException {
        Url url = new Url();
        LocalDateTime created = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime expired = dto.expiration() > 0 ? created.plusSeconds(dto.expiration()) : null;
        url.setUrl(new URL(dto.url()));
        url.setCreated(created);
        url.setExpired(expired);
        url.setDescription(dto.description());
        return url;
    }
}
