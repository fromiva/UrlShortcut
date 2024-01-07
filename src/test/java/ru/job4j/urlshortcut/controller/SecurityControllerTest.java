package ru.job4j.urlshortcut.controller;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.urlshortcut.configuration.SecurityConfiguration;
import ru.job4j.urlshortcut.dto.LoginDto;
import ru.job4j.urlshortcut.service.SecurityService;
import ru.job4j.urlshortcut.service.SecurityServiceImpl;
import ru.job4j.urlshortcut.util.AccessUnauthorizedException;
import ru.job4j.urlshortcut.util.EntityNotFoundException;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SecurityController.class)
@AutoConfigureMockMvc(webClientEnabled = false, webDriverEnabled = false)
@Import({SecurityConfiguration.class, SecurityServiceImpl.class})
class SecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SecurityService service;

    private final UUID uuid = UUID.randomUUID();
    private final String host = "example.com";
    private final String issuer = "localhost";
    private final String password = "password";
    private final URI uri = new URI("/api/token");
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final Jwt jwt = Jwt.withTokenValue("tokenPlaceholder")
            .header("alg", "HS256")
            .header("typ", "JWT")
            .issuer(issuer)
            .subject(host)
            .audience(List.of(issuer))
            .claim("scope", "USER")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

    SecurityControllerTest() throws URISyntaxException { }

    @Test
    void tokenWhenCorrectLoginThenGetTokenDetails() throws Exception {
        LoginDto dto = new LoginDto(uuid.toString(), password);
        String json = mapper.writeValueAsString(dto);
        when(service.generateToken(uuid, password)).thenReturn(jwt);
        mockMvc.perform(request(POST, uri)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.host").value(host))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.expired").exists())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void tokenWhenIncorrectUuidThenGetNotFound() throws Exception {
        LoginDto dto = new LoginDto(uuid.toString(), password);
        String json = mapper.writeValueAsString(dto);
        when(service.generateToken(uuid, password)).thenThrow(EntityNotFoundException.class);
        mockMvc.perform(request(POST, uri)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(result -> AssertionsForClassTypes
                        .assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    void tokenWhenIncorrectUuidFormatThenGetNotFound() throws Exception {
        LoginDto dto = new LoginDto(uuid.toString().substring(0, 22), password);
        String json = mapper.writeValueAsString(dto);
        mockMvc.perform(request(POST, uri)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(result -> AssertionsForClassTypes
                        .assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }

    @Test
    void tokenWhenIncorrectPasswordThenGetUnauthorized() throws Exception {
        LoginDto dto = new LoginDto(uuid.toString(), password);
        String json = mapper.writeValueAsString(dto);
        when(service.generateToken(uuid, password)).thenThrow(AccessUnauthorizedException.class);
        mockMvc.perform(request(POST, uri)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> AssertionsForClassTypes
                        .assertThat(result.getResolvedException())
                        .isInstanceOf(ResponseStatusException.class));
    }
}
