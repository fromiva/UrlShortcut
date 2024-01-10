package ru.job4j.urlshortcut.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.job4j.urlshortcut.model.Server;
import ru.job4j.urlshortcut.model.Status;
import ru.job4j.urlshortcut.model.Url;
import ru.job4j.urlshortcut.repository.UrlRepository;
import ru.job4j.urlshortcut.util.AccessForbiddenException;
import ru.job4j.urlshortcut.util.EntityNotFoundException;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class UrlServiceImplTest {

    @MockBean
    private UrlRepository repository;
    @MockBean
    private ServerService serverService;
    @MockBean
    private Principal principal;
    private UrlService urlService;

    private final UUID uuid = UUID.randomUUID();
    private final UUID serverUuid = UUID.randomUUID();
    private final String host = "example.com";
    private final String password = "password";
    private final URL path = new URL("https://" + host + "/path");
    private final LocalDateTime time = LocalDateTime.now();
    private final Status status = Status.REGISTERED;
    private final Server server = new Server(serverUuid, host, password, time, time, status, null);
    private final Url url = new Url(null, null, path, time, time, status, null);
    private final Url urlWithId = new Url(uuid, serverUuid, path, time, time, status, null);

    UrlServiceImplTest() throws MalformedURLException { }

    @BeforeEach
    void beforeEach() {
        urlService = new UrlServiceImpl(serverService, repository);

    }

    @Test
    void whenCreateNewCorrectInstanceThenGetPersistedWithActualId() {
        when(serverService.getByHost(host)).thenReturn(server);
        when(principal.getName()).thenReturn(host);
        when(repository.save(url)).thenReturn(urlWithId);
        Url actual = urlService.create(url, principal);
        assertThat(actual.getUuid()).isNotNull();
        assertThat(actual).isEqualTo(urlWithId);
    }

    @Test
    void whenCreateAlreadyCreatedInstanceThenGetException() {
        when(serverService.getByHost(host)).thenReturn(server);
        when(principal.getName()).thenReturn(host);
        when(repository.save(url)).thenThrow(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> urlService.create(url, principal))
                .isInstanceOf(DataIntegrityViolationException.class);

    }

    @Test
    void whenCreateNewInstanceWithIncorrectPrincipalThenGetException() {
        when(serverService.getByHost(host)).thenReturn(server);
        when(principal.getName()).thenReturn("subdomain." + host);
        assertThatThrownBy(() -> urlService.create(url, principal))
                .isInstanceOf(AccessForbiddenException.class);

    }

    @Test
    void whenGetByIncorrectIdThenGetException() {
        when(repository.findById(uuid)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> urlService.getById(uuid))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void whenGetAllByServerIdExistingThenGetList() {
        List<Url> expected = List.of(urlWithId);
        when(repository.findAllByServerUuid(serverUuid)).thenReturn(expected);
        assertThat(urlService.getAllByServerId(serverUuid)).isEqualTo(expected);
    }

    @Test
    void whenGetAllByServerIdNotExistingThenGetEmptyList() {
        when(repository.findAllByServerUuid(serverUuid)).thenReturn(List.of());
        assertThat(urlService.getAllByServerId(serverUuid)).isEmpty();
    }

    @Test
    void whenDeleteByCorrectIdThenSuccess() {
        when(principal.getName()).thenReturn(host);
        when(repository.findById(uuid)).thenReturn(Optional.of(urlWithId));
        when(repository.deleteByUuid(uuid)).thenReturn(1);
        assertThat(urlService.deleteByIdAndPrincipal(uuid, principal)).isTrue();
    }

    @Test
    void whenDeleteByIncorrectIdThenGetException() {
        when(principal.getName()).thenReturn(host);
        when(repository.findById(uuid)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> urlService.deleteByIdAndPrincipal(uuid, principal))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void whenDeleteByIncorrectPrincipalThenGetException() {
        when(principal.getName()).thenReturn("subdomain." + host);
        when(repository.findById(uuid)).thenReturn(Optional.of(urlWithId));
        assertThatThrownBy(() -> urlService.deleteByIdAndPrincipal(uuid, principal))
                .isInstanceOf(AccessForbiddenException.class);
    }
}
