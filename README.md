# Flight Booking System – Gateway Security & Container Topology

This document summarizes how the API Gateway secures traffic with JWT and how the Docker composition wires all services together. It is descriptive only—no run commands are included.

## API Gateway JWT Security Model
**What the token contains**
- HMAC-SHA256 JWT signed with a shared secret (`jwt.secret`) and an expiry (`jwt.expiration`, milliseconds).
- Claims: `sub` (username) and `role` (backed by the `Role` enum).

**How tokens are issued**
- `AuthController` calls `JwtUtil.generateToken(username, role)` to mint tokens with issued-at and expiration timestamps.

**How requests are authenticated**
- `JwtAuthenticationFilter` inspects `Authorization: Bearer <token>` on every request except whitelisted paths.
- Signature/expiry are verified via `JwtUtil.isValid` and `JwtUtil.extractClaims`.
- The `role` claim is normalized (adds `ROLE_` prefix if missing), mapped to `Role`, and inserted into the reactive security context as a `SimpleGrantedAuthority`.

**Route protection**
- Public: `/auth/**`, `/api/auth/**`, read/search flight catalogue endpoints, and POST search.
- Admin-only: add airline and add inventory.
- User-only: booking POST and cancel.
- User/Admin: ticket lookup and booking history.
- Everything else requires authentication.

**Failure behavior**
- Missing/invalid/expired tokens or unknown roles return `401 Unauthorized` immediately—no downstream calls are made.

**Where secrets live**
- `ApiGateway/src/main/resources/application.properties` defines `jwt.secret` and `jwt.expiration`; Config Server can also supply them via `SPRING_CONFIG_IMPORT`.
- Gateway persistence: MongoDB database `apiGatewayDB` for user data.

## Docker Composition Overview
**Services and ports**
- Config Server `config-server` (8888), Eureka `eureka-server` (8761), API Gateway `api-gateway` (9000), Flight Service `flight-service` (8090), Booking Service `booking-service` (8080), MongoDB `mongodb` (container 27017, exposed on host 27018), Kafka with Zookeeper (9092).
- `depends_on` establishes startup order so config and discovery are available first.

**Discovery & configuration**
- Each Spring Boot service imports config from `config-server:8888` and registers with `eureka-server:8761`.
- `docker-compose.yml` wires `SPRING_CONFIG_IMPORT` and `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` so containers resolve peers by service name instead of localhost.

**Data layer**
- MongoDB uses the `mongo_data` named volume to persist data across container recreations.
- Services reach Mongo via the internal host `mongodb:27017` (host-side mapped to `27018` to avoid colliding with a local mongod).

**Networking**
- All containers share the default compose network; they address each other by service name (`config-server`, `eureka-server`, `mongodb`, `kafka`, etc.).
- Host access uses the published ports above; inside the network, containers communicate on their internal ports.

**Resilience**
- `restart: on-failure` on gateway, flight-service, and booking-service helps them retry after transient config/Eureka startup delays.

## Control Plane Notes
- **Config repository**: The Config Server pulls configuration from the Git repo `ConfigServerFlightBooking.git` (branch `main`) as specified in its `application.properties`.
- **JWT secret stewardship**: The symmetric key in `application.properties` is suitable for development only. For real deployments, supply it via environment variable or secret manager and avoid committing production keys.
- **Trust boundaries**: The gateway is the single ingress for downstream services; JWT validation happens there. Downstream services still register with Eureka and can apply their own auth if needed, but the gateway is responsible for enforcing user/admin role checks at the edge.
