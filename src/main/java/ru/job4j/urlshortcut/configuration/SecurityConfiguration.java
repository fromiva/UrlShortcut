package ru.job4j.urlshortcut.configuration;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;

/** Application web-security configuration class. */
@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
@EnableWebSecurity
public class SecurityConfiguration {

    /** Security-specific configuration properties. */
    private final SecurityProperties properties;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(properties.getSecret().getBytes()));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey secretKey = new SecretKeySpec(properties.getSecret().getBytes(), "RSA");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(properties.getAlgorithm())
                .build();
        /* Custom JWT validator to check a token audience. Issuer and audience are the same. */
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                new JwtIssuerValidator(properties.getIssuer()),
                new JwtTimestampValidator(),
                new JwtClaimValidator<List<String>>(
                        JwtClaimNames.AUD, aud -> aud.contains(properties.getIssuer())));
        decoder.setJwtValidator(validator);
        return decoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .authorizeHttpRequests(req -> req.antMatchers("/api/servers/register").permitAll())
                .authorizeHttpRequests(req -> req.antMatchers("/api/token").permitAll())
                .authorizeHttpRequests(req -> req.antMatchers("/redirect/*").permitAll())
                /* Authority 'SCOPE_USER' matches the JWT scope 'USER'.*/
                .authorizeHttpRequests(req -> req.anyRequest().hasAuthority("SCOPE_USER"))
                .build();
    }
}
