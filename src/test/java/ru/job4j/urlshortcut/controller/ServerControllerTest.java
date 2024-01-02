package ru.job4j.urlshortcut.controller;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.urlshortcut.configuration.SecurityConfiguration;
import ru.job4j.urlshortcut.dto.ServerPasswordChangeDto;
import ru.job4j.urlshortcut.dto.ServerRegistrationDto;
import ru.job4j.urlshortcut.model.Server;
import ru.job4j.urlshortcut.model.Status;
import ru.job4j.urlshortcut.service.ServerService;
import ru.job4j.urlshortcut.util.EntityNotFoundException;
import ru.job4j.urlshortcut.util.UnauthorizedException;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

    private final UUID uuid = UUID.randomUUID();
    private final String host = "example.com";
    private final String password = "password";
    private final Status status = Status.REGISTERED;
    private final LocalDateTime time = LocalDateTime.now();
    private final String desc = "Some description";
    private final Server server = new Server(null, host, password, time, time, status, desc);
    private final Server serverWithId = new Server(uuid, host, password, time, time, status, desc);

    private final URI uriReg = new URI("/api/servers/register");
    private final URI uriId = new URI("/api/servers/" + uuid);
    private final URI uriWrongUuid = new URI("/api/servers/9b307a38-0253-476b-9977");
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
    void getServerByIdWhenCorrectIdThenGetServer() throws Exception {
        when(service.getById(uuid)).thenReturn(serverWithId);
        mockMvc.perform(request(GET, uriId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                .andExpect(jsonPath("$.host").value(host))
                .andExpect(jsonPath("$.status").value(status.name()))
                .andExpect(jsonPath("$.description").value(desc));
    }

    @Test
    void getServerByIdCheckThatServerDoesNotContainPassword() throws Exception {
        when(service.getById(uuid)).thenReturn(serverWithId);
        mockMvc.perform(request(GET, uriId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getServerByIdWhenIncorrectIdThenNotFound() throws Exception {
        when(service.getById(uuid)).thenThrow(EntityNotFoundException.class);
        mockMvc.perform(request(GET, uriId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    void changeServerPasswordWhenCorrectIdThenSuccess() throws Exception {
        String newPassword = "password1";
        ServerPasswordChangeDto dto = new ServerPasswordChangeDto(password, newPassword);
        String json = mapper.writeValueAsString(dto);
        when(service.updatePassword(uuid, password, newPassword)).thenReturn(true);
        mockMvc.perform(request(PATCH, uriId)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentLength()).isZero());
    }

    @Test
    void changeServerPasswordWhenIncorrectIdThenNotFound() throws Exception {
        String newPassword = "password1";
        ServerPasswordChangeDto dto = new ServerPasswordChangeDto(password, newPassword);
        String json = mapper.writeValueAsString(dto);
        when(service.updatePassword(uuid, password, newPassword))
                .thenThrow(EntityNotFoundException.class);
        mockMvc.perform(request(PATCH, uriId)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    void changeServerPasswordWhenIncorrectPasswordThenUnauthorized() throws Exception {
        String newPassword = "password1";
        ServerPasswordChangeDto dto = new ServerPasswordChangeDto(password, newPassword);
        String json = mapper.writeValueAsString(dto);
        when(service.updatePassword(uuid, password, newPassword))
                .thenThrow(UnauthorizedException.class);
        mockMvc.perform(request(PATCH, uriId)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    void changeServerPasswordWhenIncorrectUuidFormatThenNotFound() throws Exception {
        String newPassword = "password1";
        ServerPasswordChangeDto dto = new ServerPasswordChangeDto(password, newPassword);
        String json = mapper.writeValueAsString(dto);
        mockMvc.perform(request(PATCH, uriWrongUuid)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    void deleteServerWhenCorrectIdThenSuccess() throws Exception {
        when(service.deleteById(uuid)).thenReturn(true);
        mockMvc.perform(request(DELETE, uriId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(result -> assertThat(result.getResponse().getContentLength()).isZero());
    }

    @Test
    void deleteServerWhenIncorrectIdThenNotFound() throws Exception {
        when(service.deleteById(uuid)).thenThrow(EntityNotFoundException.class);
        mockMvc.perform(request(DELETE, uriId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    void deleteServerWhenIncorrectUuidFormatThenNotFound() throws Exception {
        mockMvc.perform(request(DELETE, uriWrongUuid)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }
}
