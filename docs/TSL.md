# SSL / TSL encryption configuration

> [!CAUTION]
> Be careful with the private key of your public/private key certificate.
> Do not disclose it to the other persons and do not provide access to it.

The application is a Spring Boot Java web application. To process HTTP and HTTPS
requests, it uses an embedded Apache Tomcat web server. So, web traffic encryption
configuration is a Spring Boot specific Tomcat configuration.

Firstly, you need SSL (TSL) public/private key certificates issued by
a certification authority for your domain name.

With Spring Boot, you can use keystore in PKCS12 (Public Key Cryptography Standards)
or in JKS (Java KeyStore) formats.

Let's look at a configuration example. Suppose you have a certificates represented
in the PEM plain text format.

- `public_key.crt` file with a public key

```text
-----BEGIN CERTIFICATE-----
<some certificate text data>
-----END CERTIFICATE-----
```

- `private_key.crt` file with a private key

```text
-----BEGIN RSA PRIVATE KEY-----
<some certificate text data>
-----END RSA PRIVATE KEY-----
```

You can generate a keystore using OpenSSL by the following command:

```shell
openssl pkcs12 -export -in public_key.crt -inkey private_key.crt -out certificate.pfx
```

Now, you can configure the application. Put the keystore and create the
`application.yaml` file in the application folder. Put the following content
to the `application.yaml` file (it enables the TLS v 1.3 with 256-bit encryption
and starts the application on port 8443) and restart the application:

```yaml
server:
  ssl:
    enabled: true
    key-store-type: PKCS12
    key-store: certificate.pfx
    key-store-password: <provide the correct password for keystore here>
    protocol: TLS
    enabled-protocols: TLSv1.3
    ciphers: TLS_AES_256_GCM_SHA384
  port: 8443
```

Or you can skip any of `yaml` option and provide it as command line one
when the application starts, for example:

```text
java -jar url-shortcut-0.0.1-SNAPSHOT.jar \
--server.ssl.key-store-password=<provide the correct password for keystore here>
```

## Bibliography

- [Securing Spring Boot Applications With SSL](https://spring.io/blog/2023/06/07/securing-spring-boot-applications-with-ssl)
