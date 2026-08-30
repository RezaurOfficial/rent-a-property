# Property Rental Platform

A demo property rental application built as Spring Boot microservices behind a
hand-written API gateway, with Angular on the frontend and Keycloak as the single
source of truth for user identity.

Admins manage the property catalog (add/edit/delete); authenticated users browse,
rent, and cancel rentals. Every service only ever sees a Keycloak user id (the JWT
`sub` claim) — no service stores its own copy of user profile data or credentials.

## Architecture

```
Angular UI (4200) --(OIDC Auth Code + PKCE via keycloak-js)--> Keycloak (9090, realm "rental-app")
       |
       | REST calls, Bearer JWT
       v
API Gateway (8080) --Eureka lookup + manual proxy (RestClient)--> property-service (8081)
       |                                                    \--> rental-service (8082)
       |
       +-- /api/auth/register handled locally (Keycloak Admin REST API, client-credentials)

Eureka Server (8761) <-- registers: api-gateway, property-service, rental-service

rental-service --(Feign + client-credentials token)--> property-service
   (flips a property between AVAILABLE/RENTED when a rental is created/cancelled)
```

- **No Spring Cloud Gateway.** `api-gateway`'s `GatewayController` is a plain
  `@RestController` that resolves the target service via Eureka's `DiscoveryClient`
  and forwards the request with Spring's `RestClient` — routing is a small ordered
  Java list (`RouteDefinition`s), not a YAML predicate DSL.
- **Every service independently validates the JWT** issued by Keycloak (defense in
  depth) — the gateway is not a trust boundary the backends blindly rely on.
- **Keycloak realm roles** (`ADMIN`, `USER`, `SERVICE`) are mapped onto Spring
  Security `ROLE_*` authorities by a small `KeycloakRealmRoleConverter` duplicated in
  each of the three Spring services (they're independent Gradle projects, no shared
  parent/library).

| Component          | Tech                                  | Port |
|---------------------|----------------------------------------|------|
| `discovery-server`  | Spring Boot + Netflix Eureka Server    | 8761 |
| `api-gateway`       | Spring Boot, hand-written proxy        | 8080 |
| `property-service`  | Spring Boot + Spring Data JPA + H2     | 8081 |
| `rental-service`    | Spring Boot + Spring Data JPA + H2     | 8082 |
| `angular-ui`        | Angular 20 + keycloak-angular          | 4200 |
| Keycloak            | *(assumed already running)*            | 9090 |

## Prerequisites

- JDK 25, a JDK toolchain-capable Gradle (each service has its own `./gradlew`)
- Node.js compatible with Angular CLI 20 (already has `node_modules` installed)
- Keycloak already running on `http://localhost:9090`, with the realm set up per
  **[KEYCLOAK_SETUP.md](KEYCLOAK_SETUP.md)** — do that first, everything below depends
  on it. Every URL below assumes a pre-17 (Wildfly-based) Keycloak distribution, which
  needs the `/auth` prefix (e.g. `http://localhost:9090/auth/realms/rental-app`) — see
  the note at the top of `KEYCLOAK_SETUP.md` if your Keycloak doesn't use one.

## Configuring the two client secrets

After following `KEYCLOAK_SETUP.md` you'll have generated secrets for the
`rental-service` and `gateway-service` confidential clients. Set them as environment
variables before starting those two services (or edit the `application.yaml`
defaults directly — they're marked `changeme`):

```
# before starting rental-service
export RENTAL_SERVICE_CLIENT_SECRET=<secret from Keycloak>

# before starting api-gateway
export GATEWAY_SERVICE_CLIENT_SECRET=<secret from Keycloak>
```

(On Windows PowerShell: `$env:RENTAL_SERVICE_CLIENT_SECRET = "..."`.)

## Running it

Start in this order, each in its own terminal:

```
cd discovery-server && ./gradlew bootRun
cd property-service && ./gradlew bootRun
cd rental-service   && ./gradlew bootRun
cd api-gateway       && ./gradlew bootRun
cd angular-ui        && npm start
```

Check `http://localhost:8761` (Eureka dashboard) — `api-gateway`, `property-service`,
and `rental-service` should all show as `UP` before you rely on the gateway. Then open
`http://localhost:4200`.

`property-service` seeds a handful of sample properties on first startup
(`src/main/resources/data.sql`) so the property list isn't empty.

## Smoke-testing the API directly

```
# anonymous browse
curl http://localhost:8080/api/properties

# self-registration (creates a real Keycloak user with role USER)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","password":"Passw0rd!","firstName":"Alice","lastName":"Doe"}'
```

Renting/cancelling and the admin property CRUD screens require a Bearer token — the
easiest way to exercise those end-to-end is through the Angular UI after logging in.

## Known simplifications (by design, for a demo)

- Resource servers validate the JWT's issuer and signature only — no `aud` (audience)
  check. Any token issued by the `rental-app` realm is accepted by every service.
- H2 databases are in-memory (`jdbc:h2:mem:...`) — data resets on every restart.
- The gateway forwards to the *first* Eureka instance for a service id — fine for one
  instance per service, not a real load-balancing story.
