package ru.job4j.urlshortcut.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.job4j.urlshortcut.configuration.SecurityConfiguration;
import ru.job4j.urlshortcut.model.Server;
import ru.job4j.urlshortcut.model.Status;
import ru.job4j.urlshortcut.repository.ServerRepository;
import ru.job4j.urlshortcut.util.AccessForbiddenException;
import ru.job4j.urlshortcut.util.EntityNotFoundException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SecurityConfiguration.class)
class ServerServiceImplTest {

    @Autowired
    private PasswordEncoder encoder;
    @MockBean
    private ServerRepository repository;
    @MockBean
    private Principal principal;
    private ServerService serverService;

    private final UUID uuid = UUID.randomUUID();
    private final String host = "example.com";
    private final String password = "password";
    private final Status status = Status.REGISTERED;
    private final LocalDateTime time = LocalDateTime.now();
    private final String desc = "Some description";
    private final Server server = new Server(null, host, password, time, time, status, desc);
    private final Server serverWithId = new Server(uuid, host, password, time, time, status, desc);

    @BeforeEach
    void beforeEach() {
        serverService = new ServerServiceImpl(encoder, repository);
        server.setPassword(password);
        serverWithId.setPassword(encoder.encode(password));
    }

    @Test
    void whenCreateNewCorrectInstanceThenGetPersistedWithActualId() {
        when(repository.save(server)).thenReturn(serverWithId);
        Server actual = serverService.create(server);
        assertThat(actual.getUuid()).isNotNull();
        assertThat(actual.getUuid()).isEqualTo(uuid);
    }

    @Test
    void whenCreateNewCorrectInstanceThenGetPersistedWithEncodedPassword() {
        ArgumentCaptor<Server> captor = ArgumentCaptor.forClass(Server.class);
        when(repository.save(captor.capture())).thenReturn(serverWithId);
        serverService.create(server);
        Server actual = captor.getValue();
        assertThat(encoder.matches(password, actual.getPassword())).isTrue();
    }

    @Test
    void whenCreateAlreadyCreatedInstanceThenGetException() {
        when(repository.save(server)).thenThrow(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> serverService.create(server))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void whenGetByCorrectIdAndHostThenGetEntity() {
        when(repository.findByUuidAndHost(uuid, host)).thenReturn(Optional.of(serverWithId));
        Server actual = serverService.getByIdAndHost(uuid, host);
        assertThat(actual).usingRecursiveComparison().isEqualTo(serverWithId);
    }

    @Test
    void whenGetByIncorrectIdThenGetException() {
        when(repository.findByUuidAndHost(uuid, host)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> serverService.getByIdAndHost(uuid, host))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void whenGetByIncorrectHostThenGetException() {
        String wrongHost = "subdomain." + host;
        when(repository.findByUuidAndHost(uuid, wrongHost)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> serverService.getByIdAndHost(uuid, wrongHost))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void whenUpdatePasswordByCorrectIdThenSuccess() {
        String newPassword = "password1";
        when(repository.findById(uuid)).thenReturn(Optional.of(serverWithId));
        when(repository.updatePasswordByUuid(eq(uuid), any())).thenReturn(1);
        when(principal.getName()).thenReturn(host);
        assertThat(serverService.updatePasswordByIdAndPrincipal(uuid, principal, newPassword))
                .isTrue();
    }

    @Test
    void whenUpdatePasswordByIncorrectIdThenGetException() {
        String newPassword = "password1";
        assertThatThrownBy(() -> serverService
                .updatePasswordByIdAndPrincipal(uuid, principal, newPassword))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void whenUpdatePasswordByIncorrectHostThenGetException() {
        String newPassword = "password1";
        when(repository.findById(uuid)).thenReturn(Optional.of(serverWithId));
        when(principal.getName()).thenReturn("subdomain." + host);
        assertThatThrownBy(() -> serverService
                .updatePasswordByIdAndPrincipal(uuid, principal, newPassword))
                .isInstanceOf(AccessForbiddenException.class);
    }

    @Test
    void whenDeleteByCorrectIdThenSuccess() {
        when(repository.findById(uuid)).thenReturn(Optional.of(serverWithId));
        when(repository.deleteByUuid(uuid)).thenReturn(1);
        when(principal.getName()).thenReturn(host);
        assertThat(serverService.deleteByIdAndPrincipal(uuid, principal)).isTrue();
    }

    @Test
    void whenDeleteByIncorrectIdThenGetException() {
        when(repository.findById(uuid)).thenThrow(EntityNotFoundException.class);
        assertThatThrownBy(() -> serverService.deleteByIdAndPrincipal(uuid, principal))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void whenDeleteByIncorrectPrincipalThenGetException() {
        when(repository.findById(uuid)).thenReturn(Optional.of(serverWithId));
        when(principal.getName()).thenReturn("subdomain." + host);
        assertThatThrownBy(() -> serverService.deleteByIdAndPrincipal(uuid, principal))
                .isInstanceOf(AccessForbiddenException.class);
    }
}
