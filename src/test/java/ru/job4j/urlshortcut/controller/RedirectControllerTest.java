package ru.job4j.urlshortcut.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.urlshortcut.configuration.SecurityConfiguration;
import ru.job4j.urlshortcut.model.Status;
import ru.job4j.urlshortcut.model.Url;
import ru.job4j.urlshortcut.service.UrlService;
import ru.job4j.urlshortcut.util.EntityNotFoundException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RedirectController.class)
@AutoConfigureMockMvc(webClientEnabled = false, webDriverEnabled = false)
@Import(SecurityConfiguration.class)
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UrlService service;

    private final UUID uuid = UUID.randomUUID();
    private final UUID servUuid = UUID.randomUUID();
    private final String host = "example.com";
    private final URL path = new URL("https://" + host + "/path");
    private final LocalDateTime time = LocalDateTime.now();
    private final Status status = Status.REGISTERED;

    private final URI uriId = new URI("/redirect/" + uuid);

    RedirectControllerTest() throws URISyntaxException, MalformedURLException { }

    @Test
    void redirectByUuidWhenCorrectUuidThenGetRedirection() throws Exception {
        Url url = new Url(uuid, servUuid, path, time, time.plusDays(1), status, null);
        when(service.getById(uuid)).thenReturn(url);
        mockMvc.perform(request(GET, uriId))
                .andExpect(status().is(302))
                .andExpect(result -> assertThat(result.getResponse()
                        .getRedirectedUrl()).isEqualTo(path.toString()));
    }

    @Test
    void redirectByUuidWhenCorrectUuidAndExpiredThenGetGone() throws Exception {
        Url url = new Url(uuid, servUuid, path, time.minusDays(2), time.minusDays(1), status, null);
        when(service.getById(uuid)).thenReturn(url);
        mockMvc.perform(request(GET, uriId)).andExpect(status().isGone());
    }

    @Test
    void redirectByUuidWhenIncorrectUuidThenNotFound() throws Exception {
        when(service.getById(uuid)).thenThrow(EntityNotFoundException.class);
        mockMvc.perform(request(GET, uriId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    void redirectByUuidWhenIncorrectUuidFormatThenNotFound() throws Exception {
        when(service.getById(uuid)).thenThrow(EntityNotFoundException.class);
        mockMvc.perform(request(GET, new URI("/redirect/" + uuid.toString().substring(0, 22))))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }
}
