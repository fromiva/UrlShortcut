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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.urlshortcut.configuration.SecurityConfiguration;
import ru.job4j.urlshortcut.dto.PasswordDto;
import ru.job4j.urlshortcut.dto.ServerRegistrationDto;
import ru.job4j.urlshortcut.model.Server;
import ru.job4j.urlshortcut.model.Status;
import ru.job4j.urlshortcut.service.ServerService;
import ru.job4j.urlshortcut.service.UrlService;
import ru.job4j.urlshortcut.util.EntityNotFoundException;
import ru.job4j.urlshortcut.util.AccessUnauthorizedException;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServerController.class)
@AutoConfigureMockMvc(webClientEnabled = false, webDriverEnabled = false)
@Import(SecurityConfiguration.class)
class ServerControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ServerService service;
    @MockBean
    private UrlService urlService;

    private final UUID uuid = UUID.randomUUID();
    private final String host = "example.com";
    private final String password = "password";
    private final Status status = Status.REGISTERED;
    private final LocalDateTime time = LocalDateTime.now();
    private final String desc = "Some description";
    private final Server server = new Server(null, host, password, time, time, status, desc);
    private final Server serverWithId = new Server(uuid, host, password, time, time, status, desc);
    private final String authority = "SCOPE_USER";

    private final URI uriReg = new URI("/api/servers/register");
    private final URI uriId = new URI("/api/servers/" + uuid);
    private final URI uriWrongUuid = new URI("/api/servers/" + uuid.toString().substring(0, 22));
    private final JsonMapper mapper = JsonMapper.builder().build();

    ServerControllerTest() throws URISyntaxException { }

    @Test
    void serverRegisterWhenCorrectRegistrationThenGetPersisted() throws Exception {
        ServerRegistrationDto dto = new ServerRegistrationDto(host, password, desc);
        String json = mapper.writeValueAsString(dto);
        when(service.create(server)).thenReturn(serverWithId);
        mockMvc.perform(request(POST, uriReg)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                .andExpect(jsonPath("$.host").value(host))
                .andExpect(jsonPath("$.status").value(status.name()))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.updated").exists())
                .andExpect(jsonPath("$.description").value(desc));
    }

    @Test
    void serverRegisterCheckThatNewPersistedDoesNotContainPassword() throws Exception {
        ServerRegistrationDto dto = new ServerRegistrationDto(host, password, desc);
        String json = mapper.writeValueAsString(dto);
        when(service.create(server)).thenReturn(serverWithId);
        mockMvc.perform(request(POST, uriReg)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void serverRegisterWhenBadPasswordRequestThenGetBadRequest() throws Exception {
        ServerRegistrationDto dto = new ServerRegistrationDto(host, "pass", desc);
        String json = mapper.writeValueAsString(dto);
        when(service.create(any())).thenThrow(new DataIntegrityViolationException(""));
        mockMvc.perform(request(POST, uriReg)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(MethodArgumentNotValidException.class))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value(uriReg.getPath()));
    }

    @Test
    void serverRegisterWhenBadHostRequestThenGetBadRequest() throws Exception {
        ServerRegistrationDto dto = new ServerRegistrationDto(null, password, desc);
        String json = mapper.writeValueAsString(dto);
        when(service.create(any())).thenThrow(new DataIntegrityViolationException(""));
        mockMvc.perform(request(POST, uriReg)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(MethodArgumentNotValidException.class))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value(uriReg.getPath()));
    }

    @Test
    void serverRegisterWhenEmptyDescriptionRequestThenGetPersisted() throws Exception {
        ServerRegistrationDto dto = new ServerRegistrationDto(host, password, null);
        String json = mapper.writeValueAsString(dto);
        when(service.create(server)).thenReturn(
                new Server(uuid, host, password, time, time, status, null));
        mockMvc.perform(request(POST, uriReg)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                .andExpect(jsonPath("$.description").doesNotExist());
    }

    @Test
    void serverRegisterWhenAlreadyRegisteredThenConflict() throws Exception {
        ServerRegistrationDto dto = new ServerRegistrationDto(host, password, desc);
        String json = mapper.writeValueAsString(dto);
        when(service.create(any())).thenThrow(new DataIntegrityViolationException(""));
        mockMvc.perform(request(POST, uriReg)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void getServerByIdWhenCorrectIdAndCorrectPrincipalThenGetServerStatistics() throws Exception {
        when(service.getByIdAndHost(eq(uuid), any())).thenReturn(serverWithId);
        when((urlService.getAllByServerId(uuid))).thenReturn(List.of());
        mockMvc.perform(request(GET, uriId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.server.uuid").value(uuid.toString()))
                .andExpect(jsonPath("$.server.host").value(host))
                .andExpect(jsonPath("$.server.status").value(status.name()))
                .andExpect(jsonPath("$.server.description").value(desc))
                .andExpect(jsonPath("$.urls").isEmpty());
    }

    @Test
    @WithMockUser(username = "subdomain." + host, authorities = authority)
    void getServerByIdWhenCorrectIdAndIncorrectPrincipalThenNotFound() throws Exception {
        when(service.getByIdAndHost(eq(uuid), any())).thenThrow(EntityNotFoundException.class);
        mockMvc.perform(request(GET, uriId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void getServerByIdCheckThatServerStatisticsDoesNotContainPassword() throws Exception {
        when(service.getByIdAndHost(eq(uuid), any())).thenReturn(serverWithId);
        when((urlService.getAllByServerId(uuid))).thenReturn(List.of());
        mockMvc.perform(request(GET, uriId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.server.uuid").value(uuid.toString()))
                .andExpect(jsonPath("$.server.password").doesNotExist());
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void getServerByIdWhenIncorrectIdThenNotFound() throws Exception {
        when(service.getByIdAndHost(eq(uuid), any())).thenThrow(EntityNotFoundException.class);
        mockMvc.perform(request(GET, uriId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void changeServerPasswordWhenCorrectIdThenSuccess() throws Exception {
        PasswordDto dto = new PasswordDto(password);
        String json = mapper.writeValueAsString(dto);
        when(service.updatePasswordByIdAndPrincipal(eq(uuid), any(), eq(password)))
                .thenReturn(true);
        mockMvc.perform(request(PATCH, uriId)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentLength()).isZero());
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void changeServerPasswordWhenIncorrectIdThenNotFound() throws Exception {
        PasswordDto dto = new PasswordDto(password);
        String json = mapper.writeValueAsString(dto);
        when(service.updatePasswordByIdAndPrincipal(eq(uuid), any(), eq(password)))
                .thenThrow(EntityNotFoundException.class);
        mockMvc.perform(request(PATCH, uriId)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    @WithMockUser(username = "subdomain." + host, authorities = authority)
    void changeServerPasswordByIncorrectUserThenForbidden() throws Exception {
        PasswordDto dto = new PasswordDto(password);
        String json = mapper.writeValueAsString(dto);
        when(service.updatePasswordByIdAndPrincipal(eq(uuid), any(), eq(password)))
                .thenThrow(AccessUnauthorizedException.class);
        mockMvc.perform(request(PATCH, uriId)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void changeServerPasswordWhenIncorrectUuidFormatThenNotFound() throws Exception {
        String newPassword = "password1";
        PasswordDto dto = new PasswordDto(newPassword);
        String json = mapper.writeValueAsString(dto);
        mockMvc.perform(request(PATCH, uriWrongUuid)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void deleteServerWhenCorrectIdThenSuccess() throws Exception {
        when(service.deleteByIdAndPrincipal(eq(uuid), any())).thenReturn(true);
        mockMvc.perform(request(DELETE, uriId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(result -> assertThat(result.getResponse().getContentLength()).isZero());
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void deleteServerWhenIncorrectIdThenNotFound() throws Exception {
        when(service.deleteByIdAndPrincipal(eq(uuid), any()))
                .thenThrow(EntityNotFoundException.class);
        mockMvc.perform(request(DELETE, uriId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    @WithMockUser(username = host, authorities = authority)
    void deleteServerWhenIncorrectUuidFormatThenNotFound() throws Exception {
        mockMvc.perform(request(DELETE, uriWrongUuid)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }
}
