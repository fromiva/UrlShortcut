# Notes for developers

## Technology stack

The application is a Spring Boot 2.7 Java web application with the following main dependencies:

- Java Development Kit version 17 or higher;
- Spring Security OAuth 2.0 Resource Server module;
- Spring Data JPA module;
- Java EE Bean Validation module;
- Apache Maven build system version 3.9 or higher;
- embedded Apache Tomcat Servlet container and web server;
- Spring-powered Liquibase database migration tool;
- PostgreSQL JDBC driver;
- embedded H2 SQL database engine
  (for unit testing only, not available in the final uber JAR file).

## Architecture

Model–view–controller pattern is used as core application architecture one,
Spring Web MVC as a pattern implementation framework.

Code organization and structure rules:

- Codebase divided by layers: persistence, service and presentation
- Liquibase's migration scripts have the YAML and SQL format
- Documentation files have the Markdown format
- Code style is checked by CheckStyle

## Security mechanism

Bearer JSON Web Token endpoint security method is used as a security mechanism.
Spring Security OAuth2 Resource Server is chosen as it implementation.

Authentication is performed at the moment when a JWT is issued.
Authorization is performed at the moment when an endpoint checks token validity.
Token scope `<NAME>` should match required endpoint authority with title `SCOPE_<NAME>`.

Embedded JWT encoder is used to issue the tokens
by signing generated JWT with a single symmetric key.
The application issues tokens to access to its own endpoints only.
Dedicated or third party authentication/authorization server is not required or assumed.

## Redirection log mechanism

The redirection log is implemented as a user-defined function (for PostgreSQL DBMS only),
that creates a log record in the special database table and retrieves specified URL entity.
This function is called from the dedicated Spring Data JPA URL repository each time
when a redirection request occurs.
There is a similar stub function for H2 DBMS, that only retrieves an entity without logging.

Numbers of redirections are calculated by special count function
that sums a number of log records with a specified URL ID.

## Application profiles

The application has the following Spring Boot, Maven and Liquibase profiles/contexts:

- `dev`: profile to use while the development process
- `qa`: profile to use while the quality assurance process
- `test`: profile to run unit tests with in-memory database configuration
- `prod`: default profile to run in a production environment

Any Spring Boot profile has a corresponding both Maven profile and Liquibase context.
When the application starts from Maven, it uses Maven profile to initiate
the bound Spring Boot one. Default Maven profile is `test` one.

When JAR file has run directly, the `prod` Spring Boot profile is used as a default one.

### Database migration

Embedded Liquibase module is used as a database migration tool,
and it performs migration automatically if needed when each time the application starts.

### Testing and quality assurance

There are two dedicated application profiles to unit testing and quality assurance:
`test` and `qa` accordingly.

Besides the JUnit and Mokito testing libraries, the specialized Spring Framework,
Spring Boot, Spring Data and Spring Security utilities and annotations are used.
JaCoCo is used as a code coverage toolkit.
