package ru.job4j.urlshortcut.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.job4j.urlshortcut.configuration.SecurityConfiguration;
import ru.job4j.urlshortcut.configuration.SecurityProperties;
import ru.job4j.urlshortcut.model.Server;
import ru.job4j.urlshortcut.model.Status;
import ru.job4j.urlshortcut.util.EntityNotFoundException;
import ru.job4j.urlshortcut.util.AccessUnauthorizedException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SecurityConfiguration.class)
class SecurityServiceImplTest {

    @MockBean
    private ServerService serverService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtEncoder jwtEncoder;
    @Autowired
    private JwtDecoder jwtDecoder;
    @Autowired
    private SecurityProperties properties;
    private SecurityService securityService;

    private final UUID uuid = UUID.randomUUID();
    private final String host = "example.com";
    private final String password = "password";
    private final Status status = Status.REGISTERED;
    private final LocalDateTime time = LocalDateTime.now();
    private final Server server = new Server(uuid, host, password, time, time, status, null);

    @BeforeEach
    void beforeEach() {
        securityService = new SecurityServiceImpl(
                properties, serverService, passwordEncoder, jwtEncoder);
        server.setPassword(passwordEncoder.encode(password));
    }

    @Test
    void generateTokenWhenCorrectLoginThenGetToken() {
        when(serverService.getById(uuid)).thenReturn(server);
        String jwt = securityService.generateToken(uuid, password).getTokenValue();
        assertThatCode(() -> jwtDecoder.decode(jwt)).doesNotThrowAnyException();
        assertThat(jwtDecoder.decode(jwt).getClaims().get("sub")).isEqualTo(host);
    }

    @Test
    void generateTokenWhenIncorrectUuidThenGetException() {
        when(serverService.getById(uuid)).thenThrow(EntityNotFoundException.class);
        assertThatThrownBy(() -> securityService.generateToken(uuid, password))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void generateTokenWhenIncorrectPasswordThenGetException() {
        when(serverService.getById(uuid)).thenReturn(server);
        assertThatThrownBy(() -> securityService.generateToken(uuid, password + "0"))
                .isInstanceOf(AccessUnauthorizedException.class);
    }
}
