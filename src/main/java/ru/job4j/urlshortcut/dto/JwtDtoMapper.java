package ru.job4j.urlshortcut.dto;

import org.springframework.security.oauth2.jwt.Jwt;

/** Mapper for {@link JwtDto} Data Transfer Object */
public class JwtDtoMapper {

    /**
     * Method to map a JWT to a DTO.
     * @param jwt to map
     * @return DTO
     */
    public static JwtDto toDto(Jwt jwt) {
        return new JwtDto(
                jwt.getSubject(),
                jwt.getIssuedAt(),
                jwt.getExpiresAt(),
                jwt.getTokenValue());
    }
}
