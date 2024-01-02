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
import ru.job4j.urlshortcut.util.EntityNotFoundException;

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
    void whenGetByCorrectIdThenGetEntity() {
        when(repository.findById(uuid)).thenReturn(Optional.of(serverWithId));
        Server actual = serverService.getById(uuid);
        assertThat(actual).usingRecursiveComparison().isEqualTo(serverWithId);
    }

    @Test
    void whenGetByIncorrectIdThenGetException() {
        when(repository.findById(uuid)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> serverService.getById(uuid))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void whenUpdatePasswordByCorrectIdThenSuccess() {
        String newPassword = "password1";
        when(repository.findById(uuid)).thenReturn(Optional.of(serverWithId));
        when(repository.updatePasswordById(eq(uuid), any())).thenReturn(1);
        assertThat(serverService.updatePassword(uuid, password, newPassword)).isTrue();
    }

    @Test
    void whenUpdatePasswordByIncorrectIdThenGetException() {
        String newPassword = "password1";
        when(repository.findById(uuid)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> serverService.updatePassword(uuid, password, newPassword))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void whenDeleteByCorrectIdThenSuccess() {
        when(repository.deleteByIdAndReturnCount(uuid)).thenReturn(1);
        assertThat(serverService.deleteById(uuid)).isTrue();
    }

    @Test
    void whenDeleteByIncorrectIdThenGetFalse() {
        when(repository.deleteByIdAndReturnCount(uuid)).thenReturn(0);
        assertThat(serverService.deleteById(uuid)).isFalse();
    }
}
