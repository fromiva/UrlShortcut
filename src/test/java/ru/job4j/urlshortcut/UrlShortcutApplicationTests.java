package ru.job4j.urlshortcut;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.job4j.urlshortcut.dto.JwtDto;
import ru.job4j.urlshortcut.dto.LoginDto;
import ru.job4j.urlshortcut.dto.ServerRegistrationDto;
import ru.job4j.urlshortcut.model.ErrorDetail;
import ru.job4j.urlshortcut.model.Server;
import ru.job4j.urlshortcut.repository.ServerRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/** Application integration tests class. */
@SpringBootTest(
        classes = UrlShortcutApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class UrlShortcutApplicationTests {

    @Autowired
    private ServerRepository serverRepository;

    private final String host = "example.com";
    private final String password = "password";
    private final String desc = "Some description";

    @LocalServerPort
    private int port;
    private String domainUrl;
    private final String regUrl = "/api/servers/register";
    private final String idUrl = "/api/servers/";
    private final String tokenUrl = "/api/token";
    private final TestRestTemplate restTemplate = new TestRestTemplate();
    private final JsonMapper mapper = JsonMapper.builder().build();

    @BeforeEach
    void beforeEach() {
        domainUrl = "http://localhost:" + port;
    }

    @AfterEach
    void afterEach() {
        serverRepository.deleteAll();
    }

    /** Default Spring Boot test that checks the Correct Spring Application Context loading. */
    @Test
    void contextLoads() { }

    /**
     * Test scenario of the default workflow with {@code Server} entity.
     * @throws JsonProcessingException checked exception passing from the JSON mapper
     */
    @Test
    void whenCreateGetAndDeleteServerEntityScenario() throws JsonProcessingException {

        /* Create a new entity */
        String json = mapper.writeValueAsString(new ServerRegistrationDto(host, password, desc));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        HttpEntity<String> requestServer = new HttpEntity<>(json, headers);

        ResponseEntity<Server> responseCreate = restTemplate
                .postForEntity(domainUrl + regUrl, requestServer, Server.class);
        assertThat(responseCreate.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseCreate.hasBody()).isTrue();
        assertThat(responseCreate.getBody()).isInstanceOf(Server.class);

        /* Get access token */
        String tokenJson = mapper.writeValueAsString(
                new LoginDto(responseCreate.getBody().getUuid().toString(), password));
        HttpEntity<String> requestToken = new HttpEntity<>(tokenJson, headers);
        ResponseEntity<JwtDto> responseJwt = restTemplate
                .postForEntity(domainUrl + tokenUrl, requestToken, JwtDto.class);
        assertThat(responseJwt.hasBody()).isTrue();

        String token = responseJwt.getBody().token();
        HttpHeaders headerBearer = new HttpHeaders();
        headerBearer.setBearerAuth(token);
        HttpEntity<String> requestBearer = new HttpEntity<>(headerBearer);

        /* Get the created entity */
        ResponseEntity<Server> responseGet = restTemplate.exchange(
                domainUrl + idUrl + responseCreate.getBody().getUuid(),
                HttpMethod.GET, requestBearer, Server.class);
        assertThat(responseGet.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseGet.hasBody()).isTrue();
        assertThat(responseGet.getBody()).isInstanceOf(Server.class);
        assertThat(responseCreate.getBody().getUuid()).isEqualTo(responseGet.getBody().getUuid());
        assertThat(responseCreate.getBody().getHost()).isEqualTo(responseGet.getBody().getHost());

        /* Try to get an entity by incorrect UUID */
        ResponseEntity<ErrorDetail> responseNotGet = restTemplate.exchange(
                domainUrl + idUrl + UUID.randomUUID(),
                HttpMethod.GET, requestBearer, ErrorDetail.class);
        assertThat(responseNotGet.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseGet.hasBody()).isTrue();

        /* Delete the created entity */
        ResponseEntity<Void> responseDelete = restTemplate.exchange(
                domainUrl + idUrl + responseCreate.getBody().getUuid(),
                HttpMethod.DELETE, requestBearer, Void.class);
        assertThat(responseDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        /* Check that the created entity is actually deleted */
        ResponseEntity<ErrorDetail> responseCheck = restTemplate.exchange(
                domainUrl + idUrl + responseCreate.getBody().getUuid(),
                HttpMethod.GET, requestBearer, ErrorDetail.class);
        assertThat(responseCheck.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseCheck.hasBody()).isTrue();
    }

    /**
     * Checks that the same {@code Server} cannot be registered twice.
     * @throws JsonProcessingException checked exception passing from the JSON mapper
     */
    @Test
    void whenCreateNewServerEntityTwiceThenGetError() throws JsonProcessingException {
        ServerRegistrationDto dto = new ServerRegistrationDto(host, password, desc);
        String json = mapper.writeValueAsString(dto);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        restTemplate.postForEntity(domainUrl + regUrl, request, Server.class);
        ResponseEntity<ErrorDetail> error = restTemplate
                .postForEntity(domainUrl + regUrl, request, ErrorDetail.class);
        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(error.hasBody()).isTrue();
        assertThat(error.getBody()).isInstanceOf(ErrorDetail.class);
    }
}
