package ru.job4j.urlshortcut.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.job4j.urlshortcut.configuration.SecurityConfiguration;
import ru.job4j.urlshortcut.model.Server;
import ru.job4j.urlshortcut.model.Status;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SecurityConfiguration.class)
class ServerRepositoryTest {

    @Autowired
    private PasswordEncoder encoder;

    private final UUID uuid = UUID.randomUUID();
    private final String host = "example.com";
    private final String password = "password";
    private final Status status = Status.REGISTERED;
    private final LocalDateTime time = LocalDateTime.now();
    private final String desc = "Some description";
    private final Server server = new Server(null, host, password, time, time, status, desc);

    @Autowired
    private ServerRepository repository;

    @BeforeEach
    void beforeEach() {
        server.setUuid(null);
        server.setPassword(encoder.encode(password));
    }

    @Test
    void whenFindByUuidAndHostExistingEntityThenSuccess() {
        repository.save(server);
        assertThat(repository.findByUuidAndHost(server.getUuid(), host)).isNotEmpty();
    }

    @Test
    void whenFindByUuidAndIncorrectHostExistingEntityThenEmptyOptional() {
        repository.save(server);
        assertThat(repository.findByUuidAndHost(server.getUuid(), "subdomain." + host)).isEmpty();
    }

    @Test
    void whenFindByHostExistingEntityThenSuccess() {
        repository.save(server);
        assertThat(repository.findByHost(host)).isNotEmpty();
    }

    @Test
    void whenFindByIncorrectHostExistingEntityThenEmptyOptional() {
        repository.save(server);
        assertThat(repository.findByHost("subdomain." + host)).isEmpty();
    }

    @Test
    void whenDeleteByUuidExistingEntityThenSuccess() {
        repository.save(server);
        assertThat(repository.deleteByUuid(server.getUuid())).isOne();
    }

    @Test
    void whenDeleteByUuidNotExistingEntityThenZeroDeleted() {
        assertThat(repository.deleteByUuid(uuid)).isZero();
    }

    @Test
    void whenUpdatePasswordByUuidForExistingEntityThenSuccess() {
        String newPassword = encoder.encode(password);
        repository.save(server);
        assertThat(repository.updatePasswordByUuid(server.getUuid(), newPassword)).isOne();
    }

    @Test
    void whenUpdatePasswordByUuidForNotExistingEntityThenZeroUpdated() {
        String newPassword = encoder.encode(password);
        assertThat(repository.updatePasswordByUuid(uuid, newPassword)).isZero();
    }
}
