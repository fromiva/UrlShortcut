# Job4J URL Shortcut

**URL Shortcut** is a RESTful web application to store
and get access to URL with redirection anonymizer.
Provide abilities to create Server accounts, register
and delete URL within the Server domain zone,
get usage and redirection statistics of servers and URLs.

> [!NOTE]
> **Q:** What is redirection anonymizer?
> 
> **A:** When a user clicks a link on a site to go to another one,
> web browser adds a special field "Referer" to request with
> information with original web page URL.
> If it is necessary to hide the original URL,
> then redirection anonymizer is a one of the possible solutions.
> For more information, please see this Wikipedia page:
> [HTTP referer](https://en.wikipedia.org/wiki/HTTP_referer).

A typical usage scenario is to create Internet service
with anonym redirection to registered URLs,
with URL validity within a specified time limit.

Originally, it's a study project and a part of the [Job4J](https://job4j.ru/) training course.

## License

The application is distributed under the Creative Commons CC0 1.0 Universal License.
Bundled dependencies are distributed under its own open licenses, please explore
the corresponding original documentation.

## Documentation

In addition to this `README` file there is a number of special documentation
files in the  [docs](./docs) folder:

- [openapi.yaml](./docs/openapi.yaml): full OpenAPI specification for all the application endpoints
- [TSL.md](./docs/TSL.md): instructions to configure the SSL/TSL encryption
- [develop.md](./docs/develop.md): special notes for developers

## System requirements

To run the application, the following preinstalled components are required: 

- Java Runtime Environment 17 or higher to run (or Java Development Kit to build from source)
- PostgreSQL database management system 15 or higher
- Apache Maven build system 3.9 or higher (to build application from source only)

Database name and connection settings should match ones in `datasource` group in
[src/main/resources/application.yaml](./src/main/resources/application.yaml)
Also, configuration options and connection settings can be overriden externally,
see the [Spring documentation](https://docs.spring.io/spring-boot/docs/2.7.18/reference/html/features.html#features.external-config)

It is recommended to secure the application with SSL/TSL encryption.
For details, see the dedicated documentation file: [docs/TSL.md](./docs/TSL.md)

## Building, deployment and running

To build the application from source,
use the following Maven command in a terminal
(in the application root folder),
separate Maven installation is required:

```shell
# For Linux
mvn clean package
```

```shell
# For Windows
mvn.exe clean package
```

To deploy the application,
it is necessary to copy final JAR file
from `target` folder to required destination.

To run the application, use the following
command in a terminal (in the destination folder):

```shell
# For Linux
java -jar url-shortcut-<version>.jar
```

```shell
# For Windows
java.exe -jar url-shortcut-<version>.jar
```

### Security configurations

To configure the SSL/TSL encryption,
see the dedicated documentation file: [docs/TSL.md](./docs/TSL.md)

To configure the JWT security mechanism, use the following options and restart the app,
(example in YAML format):

```yaml
ru.job4j.urlshortcut.security:
  algorithm: hs256 # choose from: hs256 | hs384 | sh512
  secret: # a string represented secret key, key minimal size in bits should match algorithm requirements.
  expiration: 3600 # JWT expiration time in seconds
  issuer: 'https://www.example.com/' # JWT issuer title
```

> [!NOTE]
> - `secret` configuration is strongly recommended
> - after each `secret` change, all the previously issued JWTs will not be valid.

## Usage and standard workflow

The application is a RESTful application, so to use it properly
ones need to use a suitable HTTP client to send `GET`, `POST`, `PATH`
and `DELETE` HTTP requests.
Examples in this chapter used cURL as a client.

Typical usage workflow is the follows:

- register a `Server` (kind of user account)
- get a JWT to secure access to other application endpoints
- register one or many `URL` (each `URL` should be at the same domain as a `Server`)
- share / use new `URL` ID-based links to anonymize a URL redirection (do not require security token)
- delete `URL` or the entire `Server` account with all the related `URL`s and statistics

> [!NOTE]
> The full OpenAPI specification for all the application endpoints
> is available in the `docs` folder: [docs/openapi.yaml](docs/openapi.yaml),
> and can be opened in any OpenAPI editor, for example:
> [https://editor.swagger.io/](https://editor.swagger.io/).

### Usage examples

Before use the examples, ones need to replace the `https://www.example.com/`
location placeholder by the actual deployed service domain name.

#### To register a new `Server` by `/api/servers/register` endpoint

Request:

```shell
curl --request POST \
--location 'https://www.example.com/api/servers/register' \
--header 'Content-Type: application/json' \
--data \
'{
  "host": "www.example.com",
  "password": "password",
  "description": "Some optional description"
}'
```

Response, where the `uuid` is an autogenerated `Server` ID:

```json
{
    "uuid": "bb47ff9c-1fbb-4bcb-9f46-10725d15be2d",
    "host": "www.example.com",
    "created": "2024-01-15T01:17:49.350799486",
    "updated": "2024-01-15T01:17:49.350820146",
    "status": "REGISTERED",
    "description": "Some optional description"
}
```

#### To get a JWT access token by `/api/token` endpoint

Request, where `uuid` is the actual `Server` ID and `password`
is a password from the previous step:

```shell
curl --request POST \
--location 'https://www.example.com/api/token' \
--header 'Content-Type: application/json' \
--data \
'{
  "uuid": "bb47ff9c-1fbb-4bcb-9f46-10725d15be2d",
  "password": "password"
}'
```

Response, where the `token` is the actual JWT token:

```json
{
    "host": "www.example.com",
    "created": "2024-01-14T22:23:21Z",
    "expired": "2024-01-14T23:23:21Z",
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ3d3cuZXhhbXBsZS5jb20iLCJhdWQiOiJsb2NhbGhvc3QiLCJzY29wZSI6IlVTRVIiLCJpc3MiOiJsb2NhbGhvc3QiLCJleHAiOjE3MDUyNzQ2MDEsImlhdCI6MTcwNTI3MTAwMX0.gVVQOKOEKha33L7hd9BfSNVDwQR2iYc79S4lpVjuDs4"
}
```

#### To register a new `URL` by `/api/urls/register` endpoint

Request:

```shell
curl --request POST \
--location 'https://www.example.com/api/urls/register' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ3d3cuZXhhbXBsZS5jb20iLCJhdWQiOiJsb2NhbGhvc3QiLCJzY29wZSI6IlVTRVIiLCJpc3MiOiJsb2NhbGhvc3QiLCJleHAiOjE3MDUyNzQ2MDEsImlhdCI6MTcwNTI3MTAwMX0.gVVQOKOEKha33L7hd9BfSNVDwQR2iYc79S4lpVjuDs4' \
--data \
'{
  "url": "https://www.example.com/path",
  "expiration": 1728000,
  "description": "Some optional description"
}'
```

Response, where the `uuid` is an autogenerated `URL` ID:

```json
{
    "uuid": "a19393d6-8c60-4d27-84a4-04f8aff1ddf2",
    "serverUuid": "bb47ff9c-1fbb-4bcb-9f46-10725d15be2d",
    "url": "https://www.example.com/path",
    "created": "2024-01-15T01:36:13.082299939",
    "expired": "2024-02-04T01:36:13",
    "status": "REGISTERED",
    "description": "Some optional description"
}
```

#### To redirect to the `URL` use the following link `<service hostname>/redirect/<URL ID>`,

URL for request in browser:

```text
https://www.example.com/redirect/a19393d6-8c60-4d27-84a4-04f8aff1ddf2
```

it will redirect to `https://www.example.com/path`

#### To get the `URL` statistics by `/api/urls/<URL ID>` endpoint

Request, where `<URL ID>` is the actual `URL` ID from previous steps:

```shell
curl --request GET \
--location 'https://www.example.com/api/urls/a19393d6-8c60-4d27-84a4-04f8aff1ddf2' \
--header 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ3d3cuZXhhbXBsZS5jb20iLCJhdWQiOiJsb2NhbGhvc3QiLCJzY29wZSI6IlVTRVIiLCJpc3MiOiJsb2NhbGhvc3QiLCJleHAiOjE3MDUyNzQ2MDEsImlhdCI6MTcwNTI3MTAwMX0.gVVQOKOEKha33L7hd9BfSNVDwQR2iYc79S4lpVjuDs4'
```

Response, where the `visited` is the number of the URL redirections:

```json
{
    "url": {
        "uuid": "a19393d6-8c60-4d27-84a4-04f8aff1ddf2",
        "serverUuid": "bb47ff9c-1fbb-4bcb-9f46-10725d15be2d",
        "url": "https://www.example.com/path",
        "created": "2024-01-15T01:36:13.0823",
        "expired": "2024-02-04T01:36:13",
        "status": "REGISTERED",
        "description": "Some optional description"
    },
    "visited": 1
}
```

#### To get the `Server` statistics by `/api/servers/<server ID>` endpoint

Request, where `<server ID>` is the actual `Server` ID from previous steps:

```shell
curl --request GET \
--location 'https://www.example.com/api/servers/bb47ff9c-1fbb-4bcb-9f46-10725d15be2d' \
--header 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ3d3cuZXhhbXBsZS5jb20iLCJhdWQiOiJsb2NhbGhvc3QiLCJzY29wZSI6IlVTRVIiLCJpc3MiOiJsb2NhbGhvc3QiLCJleHAiOjE3MDUyNzQ2MDEsImlhdCI6MTcwNTI3MTAwMX0.gVVQOKOEKha33L7hd9BfSNVDwQR2iYc79S4lpVjuDs4'
```

Response, where the `urls` is the list of all the registered URLs:

```json
{
  "server": {
    "uuid": "bb47ff9c-1fbb-4bcb-9f46-10725d15be2d",
    "host": "www.example.com",
    "created": "2024-01-15T01:17:49.350799",
    "updated": "2024-01-15T01:17:49.35082",
    "status": "REGISTERED",
    "description": "Some optional description"
  },
  "urls": [
    {
      "uuid": "a19393d6-8c60-4d27-84a4-04f8aff1ddf2",
      "serverUuid": "bb47ff9c-1fbb-4bcb-9f46-10725d15be2d",
      "url": "https://www.example.com/path",
      "created": "2024-01-15T01:36:13.0823",
      "expired": "2024-02-04T01:36:13",
      "status": "REGISTERED",
      "description": "Some optional description"
    }
  ]
}
```

## Developer's notes

Developer notes are available in the dedicated documentation file:
[docs/develop.md](./docs/develop.md)
