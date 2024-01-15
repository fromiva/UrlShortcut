package ru.job4j.urlshortcut.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

@ConfigurationProperties(prefix = "ru.job4j.urlshortcut.security")
@Data
public class SecurityProperties {

    /** Issuer of the tokens. */
    private String issuer = "localhost";

    /** Signing HMAC algorithm. */
    private MacAlgorithm algorithm = MacAlgorithm.HS256;

    /** Secret key to sign tokens. Key minimal size in bits should match algorithm requirements. */
    private String secret = "01234567".repeat(8);

    /** Token expiration time (in seconds). */
    private int expiration = 3600;
}
