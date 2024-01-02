package ru.job4j.urlshortcut.dto;

import ru.job4j.urlshortcut.model.Server;

/** Mapper for {@link ServerRegistrationDto} Data Transfer Object */
public class ServerRegistrationDtoMapper {

    /**
     * Method to map an entity to a DTO.
     * @param entity to map
     * @return DTO
     */
    public static ServerRegistrationDto toDto(Server entity) {
        return new ServerRegistrationDto(
                entity.getHost(),
                entity.getPassword(),
                entity.getDescription());
    }

    /**
     * Method to map a DTO to an entity.
     * @param dto to map
     * @return entity
     */
    public static Server toEntity(ServerRegistrationDto dto) {
        Server server = new Server();
        server.setHost(dto.host());
        server.setPassword(dto.password());
        server.setDescription(dto.description());
        return server;
    }
}
