package ru.job4j.urlshortcut.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.job4j.urlshortcut.model.Server;
import ru.job4j.urlshortcut.model.Status;
import ru.job4j.urlshortcut.model.Url;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UrlRepositoryTest {

    private final String host = "example.com";
    private final String password = "password";
    private final Status status = Status.REGISTERED;
    private final LocalDateTime time = LocalDateTime.now();
    private Server server;

    @Autowired
    private ServerRepository serverRepository;
    @Autowired
    private UrlRepository urlRepository;

    @BeforeEach
    void beforeEach() {
        server = serverRepository.save(new Server(null, host, password,
                time, time, status, null));
    }

    @Test
    void findAllByServerUuidWhenNotPersistedThenEmptyList() {
        assertThat(urlRepository.findAllByServerUuid(server.getUuid())).isEmpty();
    }

    @Test
    void findAllByServerUuidWhenPersistedThenFindAll() throws MalformedURLException {
        Url url1 = new Url(
                null, server.getUuid(),
                new URL("https://" + server.getHost() + "/path1"),
                time, time, status, null);
        Url url2 = new Url(
                null, server.getUuid(),
                new URL("https://" + server.getHost() + "/path2"),
                time, time, status, null);
        urlRepository.save(url1);
        urlRepository.save(url2);
        List<Url> actual = urlRepository.findAllByServerUuid(server.getUuid());
        assertThat(actual).isEqualTo(List.of(url1, url2));
    }

    @Test
    void findAllByServerUuidWhenPersistedManyThenFindAllRelated() throws MalformedURLException {
        Server server1 = serverRepository.save(new Server(null, "subdomain." + host,
                password, time, time, status, null));
        Url url1 = new Url(
                null, server.getUuid(),
                new URL("https://" + server.getHost() + "/path1"),
                time, time, status, null);
        Url url2 = new Url(
                null, server.getUuid(),
                new URL("https://" + server.getHost() + "/path2"),
                time, time, status, null);
        Url url3 = new Url(
                null, server1.getUuid(),
                new URL("https://" + server1.getHost() + "/path3"),
                time, time, status, null);
        urlRepository.save(url1);
        urlRepository.save(url2);
        urlRepository.save(url3);
        List<Url> actual = urlRepository.findAllByServerUuid(server.getUuid());
        List<Url> actual1 = urlRepository.findAllByServerUuid(server1.getUuid());
        assertThat(actual).isEqualTo(List.of(url1, url2));
        assertThat(actual1).isEqualTo(List.of(url3));
    }

    @Test
    void whenDeleteByUuidExistingEntityThenSuccess() throws MalformedURLException {
        Url url = new Url(
                null, server.getUuid(),
                new URL("https://" + server.getHost() + "/path1"),
                time, time, status, null);
        urlRepository.save(url);
        assertThat(urlRepository.deleteByUuid(url.getUuid())).isOne();
    }

    @Test
    void whenDeleteByUuidNotExistingEntityThenZeroDeleted() {
        assertThat(urlRepository.deleteByUuid(UUID.randomUUID())).isZero();
    }
}
