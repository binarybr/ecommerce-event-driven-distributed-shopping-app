# config-server

**Port:** 8888 · Stack: Spring Cloud Config Server

Centralised configuration provider. Services can fetch their config from here at
startup instead of (or in addition to) their bundled `application.yaml`, so settings
can be managed in one place across environments.

## Role in the system

- Serves configuration to client services that enable the Spring Cloud Config client.
- In this project most services also ship a local `application.yaml` with env-var
  overrides (e.g. `${DB_HOST:localhost}`), so they boot even if config-server is not
  the source of truth — config-server is the central option, env vars are the runtime
  override.
- Several services `depends_on` config-server so it is available early in startup.

## Notes

- Backed by a config source (e.g. a file system or Git repo of property files,
  depending on configuration).
- Not on the request path — purely a startup/refresh concern; it does not handle
  `/api/**` traffic.

## Key files

`application.yaml` (config source + server port), main application class with
`@EnableConfigServer`.
