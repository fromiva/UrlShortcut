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
    void whenDeleteByIdExistingEntityThenSuccess() {
        repository.save(server);
        assertThat(repository.deleteByIdAndReturnCount(server.getUuid())).isOne();
    }

    @Test
    void whenDeleteByIdNotExistingEntityThenZeroDeleted() {
        assertThat(repository.deleteByIdAndReturnCount(uuid)).isZero();
    }

    @Test
    void whenUpdatePasswordByIdForExistingEntityThenSuccess() {
        String newPassword = encoder.encode("password1");
        repository.save(server);
        assertThat(repository.updatePasswordById(server.getUuid(), newPassword)).isOne();
    }

    @Test
    void whenUpdatePasswordByIdForNotExistingEntityThenZeroUpdated() {
        String newPassword = encoder.encode("password1");
        assertThat(repository.updatePasswordById(uuid, newPassword)).isZero();
    }
}
