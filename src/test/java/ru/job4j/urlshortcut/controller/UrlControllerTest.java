package ru.job4j.urlshortcut.controller;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.urlshortcut.configuration.SecurityConfiguration;
import ru.job4j.urlshortcut.dto.UrlRegistrationDto;
import ru.job4j.urlshortcut.model.Status;
import ru.job4j.urlshortcut.model.Url;
import ru.job4j.urlshortcut.service.UrlService;
import ru.job4j.urlshortcut.util.AccessForbiddenException;
import ru.job4j.urlshortcut.util.EntityNotFoundException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UrlController.class)
@AutoConfigureMockMvc(webClientEnabled = false, webDriverEnabled = false)
@Import(SecurityConfiguration.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UrlService service;

    private final UUID uuid = UUID.randomUUID();
    private final UUID serverUuid = UUID.randomUUID();
    private final String host = "example.com";
    private final URL path = new URL("https://" + host + "/path");
    private final LocalDateTime time = LocalDateTime.now();
    private final Status status = Status.REGISTERED;
    private final String desc = "Some description";
    private final String authority = "SCOPE_USER";

    private final URI uriReg = new URI("/api/urls/register");
    private final URI uriId = new URI("/api/urls/" + uuid);
    private final URI uriWrongUuid = new URI("/api/urls/" + uuid.toString().substring(0, 22));
    private final Url url = new Url(null, null, path, time, time, status, desc);
    private final Url urlWithId = new Url(uuid, serverUuid, path, time, time, status, desc);

    private final JsonMapper mapper = JsonMapper.builder().build();

    UrlControllerTest() throws MalformedURLException, URISyntaxException { }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void urlRegisterWhenCorrectRegistrationThenGetPersisted() throws Exception {
        UrlRegistrationDto dto = new UrlRegistrationDto(path.toString(), 3600, desc);
        String json = mapper.writeValueAsString(dto);
        when(service.create(eq(url), any())).thenReturn(urlWithId);
        mockMvc.perform(request(POST, uriReg)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                .andExpect(jsonPath("$.serverUuid").value(serverUuid.toString()))
                .andExpect(jsonPath("$.url").value(path.toString()))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.expired").exists())
                .andExpect(jsonPath("$.status").value(status.toString()))
                .andExpect(jsonPath("$.description").value(desc));
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void urlRegisterWhenNullUrlRequestThenGetBadRequest() throws Exception {
        UrlRegistrationDto dto = new UrlRegistrationDto(null, 3600, desc);
        String json = mapper.writeValueAsString(dto);
        when(service.create(eq(url), any())).thenReturn(urlWithId);
        mockMvc.perform(request(POST, uriReg)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void urlRegisterWhenBadUrlRequestThenGetBadRequest() throws Exception {
        UrlRegistrationDto dto = new UrlRegistrationDto("[]", 3600, desc);
        String json = mapper.writeValueAsString(dto);
        when(service.create(eq(url), any())).thenReturn(urlWithId);
        mockMvc.perform(request(POST, uriReg)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void urlRegisterWhenZeroExpirationRequestThenGetPersisted() throws Exception {
        UrlRegistrationDto dto = new UrlRegistrationDto(path.toString(), 0, desc);
        String json = mapper.writeValueAsString(dto);
        Url persisted = new Url(uuid, serverUuid, path, time, null, status, desc);
        when(service.create(eq(url), any())).thenReturn(persisted);
        mockMvc.perform(request(POST, uriReg)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                .andExpect(jsonPath("$.expired").doesNotExist());
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void urlRegisterWhenEmptyDescriptionRequestThenGetPersisted() throws Exception {
        UrlRegistrationDto dto = new UrlRegistrationDto(path.toString(), 3600, null);
        String json = mapper.writeValueAsString(dto);
        Url persisted = new Url(uuid, serverUuid, path, time, time, status, null);
        when(service.create(eq(url), any())).thenReturn(persisted);
        mockMvc.perform(request(POST, uriReg)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                .andExpect(jsonPath("$.description").doesNotExist());
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void urlRegisterWhenAlreadyRegisteredThenGetConflict() throws Exception {
        UrlRegistrationDto dto = new UrlRegistrationDto(path.toString(), 3600, null);
        String json = mapper.writeValueAsString(dto);
        when(service.create(eq(url), any())).thenThrow(DataIntegrityViolationException.class);
        mockMvc.perform(request(POST, uriReg)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    @WithMockUser(username = "subdomain." + host, authorities = authority)
    void urlRegisterWhenIncorrectPrincipalThenGetException() throws Exception {
        UrlRegistrationDto dto = new UrlRegistrationDto(path.toString(), 3600, desc);
        String json = mapper.writeValueAsString(dto);
        when(service.create(eq(url), any())).thenThrow(AccessForbiddenException.class);
        mockMvc.perform(request(POST, uriReg)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void getUrlByUuidWhenCorrectIdAndCorrectPrincipalThenGetUrlStatistics() throws Exception {
        long count = 5L;
        when(service.getById(uuid)).thenReturn(urlWithId);
        when(service.getUrlVisitsCount(uuid)).thenReturn(count);
        mockMvc.perform(request(GET, uriId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url.uuid").value(uuid.toString()))
                .andExpect(jsonPath("$.url.serverUuid").value(serverUuid.toString()))
                .andExpect(jsonPath("$.url.url").value(path.toString()))
                .andExpect(jsonPath("$.url.created").exists())
                .andExpect(jsonPath("$.url.expired").exists())
                .andExpect(jsonPath("$.url.status").value(status.toString()))
                .andExpect(jsonPath("$.url.description").value(desc))
                .andExpect(jsonPath("$.visited").value(count));
    }

    @Test
    @WithMockUser(username = "subdomain." + host, authorities = authority)
    void getUrlByIdWhenCorrectIdAndIncorrectPrincipalThenNotFound() throws Exception {
        when(service.getById(uuid)).thenReturn(urlWithId);
        mockMvc.perform(request(GET, uriId))
                .andExpect(status().isForbidden())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void getUrlByIdWhenIncorrectIdThenNotFound() throws Exception {
        when(service.getById(uuid)).thenThrow(EntityNotFoundException.class);
        mockMvc.perform(request(GET, uriId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void deleteUrlWhenCorrectIdThenSuccess() throws Exception {
        when(service.deleteByIdAndPrincipal(eq(uuid), any())).thenReturn(true);
        mockMvc.perform(request(DELETE, uriId))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void deleteUrlWhenIncorrectIdThenNotFound() throws Exception {
        when(service.deleteByIdAndPrincipal(eq(uuid), any()))
                .thenThrow(EntityNotFoundException.class);
        mockMvc.perform(request(DELETE, uriId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void deleteUrlWhenIncorrectUuidFormatThenNotFound() throws Exception {
        mockMvc.perform(request(DELETE, uriWrongUuid))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }
}
