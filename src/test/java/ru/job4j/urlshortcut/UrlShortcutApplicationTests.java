package ru.job4j.urlshortcut;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import ru.job4j.urlshortcut.dto.ServerStatisticsDto;
import ru.job4j.urlshortcut.dto.UrlRegistrationDto;
import ru.job4j.urlshortcut.dto.UrlStatisticsDto;
import ru.job4j.urlshortcut.model.ErrorDetail;
import ru.job4j.urlshortcut.model.Server;
import ru.job4j.urlshortcut.model.Url;
import ru.job4j.urlshortcut.repository.ServerRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/** Application integration tests class with full application context loading. */
@SpringBootTest(
        classes = UrlShortcutApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UrlShortcutApplicationTests {

    @Autowired
    private ServerRepository serverRepository;

    private final String host = "example.com";
    private final String password = "password";
    private final String desc = "Some description";

    @LocalServerPort
    private int port;
    private String domainUrl;
    private final String serverRegUrl = "/api/servers/register";
    private final String serverIdUrl = "/api/servers/";
    private final String urlRegUrl = "/api/urls/register";
    private final String urlIdUrl = "/api/urls/";
    private final String tokenUrl = "/api/token";
    private final TestRestTemplate restTemplate = new TestRestTemplate();
    private final JsonMapper mapper = JsonMapper.builder().build();

    @BeforeEach
    void beforeEach() {
        domainUrl = "http://localhost:" + port;
    }

    /**
     * Deletes all the remaining {@code Server} entities,
     * {@code Url} entities cascade deleted by database schema configuration.
     */
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

        /* Create a new server entity */
        String json = mapper.writeValueAsString(new ServerRegistrationDto(host, password, desc));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        HttpEntity<String> requestServer = new HttpEntity<>(json, headers);

        ResponseEntity<Server> responseCreate = restTemplate
                .postForEntity(domainUrl + serverRegUrl, requestServer, Server.class);
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

        /* Get the created server entity */
        ResponseEntity<ServerStatisticsDto> responseGet = restTemplate.exchange(
                domainUrl + serverIdUrl + responseCreate.getBody().getUuid(),
                HttpMethod.GET, requestBearer, ServerStatisticsDto.class);
        assertThat(responseGet.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseGet.hasBody()).isTrue();
        assertThat(responseGet.getBody()).isInstanceOf(ServerStatisticsDto.class);
        assertThat(responseCreate.getBody().getUuid())
                .isEqualTo(responseGet.getBody().server().getUuid());
        assertThat(responseCreate.getBody().getHost())
                .isEqualTo(responseGet.getBody().server().getHost());

        /* Try to get a server entity by incorrect UUID */
        ResponseEntity<ErrorDetail> responseNotGet = restTemplate.exchange(
                domainUrl + serverIdUrl + UUID.randomUUID(),
                HttpMethod.GET, requestBearer, ErrorDetail.class);
        assertThat(responseNotGet.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseNotGet.hasBody()).isTrue();

        /* Delete the created server entity */
        ResponseEntity<Void> responseDelete = restTemplate.exchange(
                domainUrl + serverIdUrl + responseCreate.getBody().getUuid(),
                HttpMethod.DELETE, requestBearer, Void.class);
        assertThat(responseDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        /* Check that the created server entity is actually deleted */
        ResponseEntity<ErrorDetail> responseCheck = restTemplate.exchange(
                domainUrl + serverIdUrl + responseCreate.getBody().getUuid(),
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
        restTemplate.postForEntity(domainUrl + serverRegUrl, request, Server.class);
        ResponseEntity<ErrorDetail> error = restTemplate
                .postForEntity(domainUrl + serverRegUrl, request, ErrorDetail.class);
        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(error.hasBody()).isTrue();
        assertThat(error.getBody()).isInstanceOf(ErrorDetail.class);
    }

    /**
     * Test scenario of the default workflow with {@code Url} entity.
     * @throws JsonProcessingException checked exception passing from the JSON mapper
     */
    @Test
    void whenCreateGetAndDeleteUrlEntityScenario() throws JsonProcessingException {

        /* Create a new server entity */
        String json = mapper.writeValueAsString(new ServerRegistrationDto(host, password, desc));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        HttpEntity<String> requestServer = new HttpEntity<>(json, headers);

        ResponseEntity<Server> responseServer = restTemplate
                .postForEntity(domainUrl + serverRegUrl, requestServer, Server.class);
        assertThat(responseServer.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseServer.hasBody()).isTrue();
        assertThat(responseServer.getBody()).isInstanceOf(Server.class);

        /* Get access token */
        String tokenJson = mapper.writeValueAsString(
                new LoginDto(responseServer.getBody().getUuid().toString(), password));
        HttpEntity<String> requestToken = new HttpEntity<>(tokenJson, headers);
        ResponseEntity<JwtDto> responseJwt = restTemplate
                .postForEntity(domainUrl + tokenUrl, requestToken, JwtDto.class);
        assertThat(responseJwt.hasBody()).isTrue();

        String token = responseJwt.getBody().token();
        HttpHeaders headerBearer = new HttpHeaders();
        headerBearer.setBearerAuth(token);
        HttpEntity<String> requestBearer = new HttpEntity<>(headerBearer);

        /* Create a new URL entity */
        UrlRegistrationDto dto = new UrlRegistrationDto("https://" + host + "/path", 3600, null);
        String jsonUrl = mapper.writeValueAsString(dto);
        HttpHeaders headerUrl = new HttpHeaders();
        headerUrl.setContentType(APPLICATION_JSON);
        headerUrl.setBearerAuth(token);
        HttpEntity<String> requestUrl = new HttpEntity<>(jsonUrl, headerUrl);

        ResponseEntity<Url> responseUrlCreate = restTemplate
                .postForEntity(domainUrl + urlRegUrl, requestUrl, Url.class);
        assertThat(responseUrlCreate.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseUrlCreate.hasBody()).isTrue();
        assertThat(responseUrlCreate.getBody()).isInstanceOf(Url.class);

        /* Get the created URL entity */
        ResponseEntity<UrlStatisticsDto> responseUrlGet = restTemplate.exchange(
                domainUrl + urlIdUrl + responseUrlCreate.getBody().getUuid(),
                HttpMethod.GET, requestBearer, UrlStatisticsDto.class);
        assertThat(responseUrlGet.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseUrlGet.hasBody()).isTrue();
        assertThat(responseUrlGet.getBody()).isInstanceOf(UrlStatisticsDto.class);
        assertThat(responseUrlCreate.getBody().getUuid())
                .isEqualTo(responseUrlGet.getBody().url().getUuid());
        assertThat(responseUrlCreate.getBody().getUrl())
                .isEqualTo(responseUrlGet.getBody().url().getUrl());
        assertThat(responseUrlGet.getBody().visited()).isZero();

        /* Create the same new URL entity twice and get error */
        ResponseEntity<ErrorDetail> error = restTemplate
                .postForEntity(domainUrl + urlRegUrl, requestUrl, ErrorDetail.class);
        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(error.hasBody()).isTrue();
        assertThat(error.getBody()).isInstanceOf(ErrorDetail.class);

        /* Try to get a URL entity by incorrect UUID */
        ResponseEntity<ErrorDetail> responseUrlNotGet = restTemplate.exchange(
                domainUrl + urlIdUrl + UUID.randomUUID(),
                HttpMethod.GET, requestBearer, ErrorDetail.class);
        assertThat(responseUrlNotGet.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseUrlNotGet.hasBody()).isTrue();

        /* Delete the created URL entity */
        ResponseEntity<Void> responseUrlDelete = restTemplate.exchange(
                domainUrl + urlIdUrl + responseUrlCreate.getBody().getUuid(),
                HttpMethod.DELETE, requestBearer, Void.class);
        assertThat(responseUrlDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        /* Check that the created URL entity is actually deleted */
        ResponseEntity<ErrorDetail> responseUrlCheck = restTemplate.exchange(
                domainUrl + urlIdUrl + responseUrlCreate.getBody().getUuid(),
                HttpMethod.GET, requestBearer, ErrorDetail.class);
        assertThat(responseUrlCheck.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseUrlCheck.hasBody()).isTrue();

        /* Delete the created server entity */
        ResponseEntity<Void> responseDelete = restTemplate.exchange(
                domainUrl + serverIdUrl + responseServer.getBody().getUuid(),
                HttpMethod.DELETE, requestBearer, Void.class);
        assertThat(responseDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
