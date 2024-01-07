package ru.job4j.urlshortcut.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import ru.job4j.urlshortcut.configuration.SecurityProperties;
import ru.job4j.urlshortcut.model.Server;
import ru.job4j.urlshortcut.util.AccessUnauthorizedException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/** Security-specific tasks service implementation. */
@RequiredArgsConstructor
@Service
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityServiceImpl implements SecurityService {

    /** Security-specific configuration properties. */
    private final SecurityProperties properties;

    private final ServerService serverService;

    private final PasswordEncoder passwordEncoder;

    private final JwtEncoder jwtEncoder;

    /** {@inheritDoc} */
    @Override
    public Jwt generateToken(UUID uuid, String password) {
        Server server = serverService.getById(uuid);
        if (!passwordEncoder.matches(password, server.getPassword())) {
            throw new AccessUnauthorizedException("Password is incorrect.");
        }
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        JwsHeader header = JwsHeader.with(properties.getAlgorithm())
                .type("JWT")
                .build();
        JwtClaimsSet payload = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .audience(List.of(properties.getIssuer()))
                .subject(server.getHost())
                .claim("scope", "USER")
                .issuedAt(now)
                .expiresAt(now.plus(properties.getExpiration(), ChronoUnit.SECONDS))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, payload));
    }
}
