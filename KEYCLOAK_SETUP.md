# Keycloak Setup Guide

Follow this against a Keycloak instance already running on `http://localhost:9090`
(admin console at `http://localhost:9090/auth/admin`). Every step below is required —
the Spring services and Angular app are already configured to expect exactly these
names.

> This guide assumes a pre-17 (Wildfly-based) Keycloak distribution, where every URL
> is prefixed with `/auth` (e.g. `http://localhost:9090/auth/realms/...`). If your
> Keycloak loads at the root path instead (17+, Quarkus-based — check whether
> `http://localhost:9090/` or `http://localhost:9090/auth` loads the welcome page),
> drop `/auth` from every URL below and from the `application.yaml`/environment files
> this guide references.

Realm name: **`rental-app`**
Issuer URL used everywhere: **`http://localhost:9090/auth/realms/rental-app`**

---

## 1. Create the realm

1. Log into the admin console with your bootstrap admin account.
2. Top-left realm dropdown → **Create realm**.
3. Realm name: `rental-app` → **Create**.
4. Make sure you're switched into the `rental-app` realm for every step below.

## 2. Create realm roles

**Realm roles** (left sidebar) → **Create role**, three times:

| Role name | Used for |
|---|---|
| `ADMIN` | Human admins — add/edit/delete properties, cancel any rental |
| `USER`  | Regular renters — browse, rent, cancel their own rentals |
| `SERVICE` | Assigned to rental-service's own service account — lets it call property-service's internal status-flip endpoint |

## 3. Create the frontend client (`rental-frontend`)

**Clients** → **Create client**:

- **General settings**: Client type `OpenID Connect`, Client ID `rental-frontend` → Next
- **Capability config**:
  - Client authentication: **Off** (public client)
  - Authentication flow: only **Standard flow** checked (uncheck Direct access grants)
  → Next
- **Login settings**:
  - Valid redirect URIs: `http://localhost:4200/*`
  - Valid post logout redirect URIs: `http://localhost:4200/*`
  - Web origins: `http://localhost:4200`
  → Save

- Open the client → **Advanced** tab → **Proof Key for Code Exchange Code Challenge
  Method**: set to `S256`. Save.

This is the client the Angular app uses for interactive login (Authorization Code +
PKCE) — no secret, because it's a public browser client.

## 4. Create the rental-service client (`rental-service`)

**Clients** → **Create client**:

- General settings: Client ID `rental-service` → Next
- Capability config:
  - Client authentication: **On** (confidential client)
  - Authentication flow: uncheck **Standard flow**, uncheck **Direct access grants**,
    check **Service accounts roles**
  → Next
- Login settings: leave blank → Save

Now assign it the `SERVICE` realm role:

1. Open the `rental-service` client → **Service account roles** tab.
2. **Assign role** → filter by **Filter by realm roles** → select `SERVICE` → Assign.

Grab the secret:

3. **Credentials** tab → copy **Client secret**. This is the
   `RENTAL_SERVICE_CLIENT_SECRET` value from the root `README.md`.

## 5. Create the gateway's admin client (`gateway-service`)

**Clients** → **Create client**:

- General settings: Client ID `gateway-service` → Next
- Capability config:
  - Client authentication: **On**
  - Authentication flow: uncheck **Standard flow**, uncheck **Direct access grants**,
    check **Service accounts roles**
  → Next
- Login settings: leave blank → Save

Grant it permission to create users and assign them the `USER` realm role via the
Admin REST API:

1. Open the `gateway-service` client → **Service account roles** tab.
2. **Assign role** → **Filter by clients** → find the `realm-management` client →
   select `realm-admin` → Assign.

   `manage-users` alone is **not** enough here — it covers creating/updating/deleting
   users, but reading a realm role's definition and mapping it onto a user needs
   broader realm-management rights. `realm-admin` is the composite role that covers
   all of it; simplest correct choice for this demo (it's more than this one endpoint
   strictly needs — narrowing it further means hand-picking individual
   realm-management roles like `view-realm` and testing which combination actually
   permits `POST .../role-mappings/realm`, which isn't worth it here).

Grab the secret:

3. **Credentials** tab → copy **Client secret**. This is the
   `GATEWAY_SERVICE_CLIENT_SECRET` value from the root `README.md`.

## 6. Create a seed admin user

Self-registration (via the app) only ever creates `USER`-role accounts, so create at
least one `ADMIN` user by hand:

1. **Users** → **Add user**. Username: `admin` (email optional but recommended).
   → Create.
2. **Credentials** tab → **Set password** → enter a password, toggle **Temporary**
   **off** → Save.
3. **Role mapping** tab → **Assign role** → filter by realm roles → select `ADMIN` →
   Assign.

Log in to the Angular app as this user to reach the "Manage Properties" admin screens.

## 7. (Optional) Confirm the token shape

Roles show up in the access token under the `realm_access.roles` claim by default —
no extra client scope/mapper work needed; the Spring services already read that claim.

To sanity-check a token manually (temporarily enable **Direct access grants** on
`rental-frontend` if you want to use the password grant for a quick curl test, then
turn it back off afterward):

```
curl -X POST http://localhost:9090/auth/realms/rental-app/protocol/openid-connect/token \
  -d grant_type=password \
  -d client_id=rental-frontend \
  -d username=admin \
  -d password=<the password you set> \
  | jq -r .access_token | cut -d. -f2 | base64 -d
```

You should see `"realm_access":{"roles":["ADMIN", ...]}` in the decoded payload.

---

## Summary of what you should have afterward

- Realm `rental-app` with roles `ADMIN`, `USER`, `SERVICE`
- Client `rental-frontend` — public, Standard Flow + PKCE (S256)
- Client `rental-service` — confidential, service account with role `SERVICE`
- Client `gateway-service` — confidential, service account with `realm-admin`
- One seed user (e.g. `admin`) with the `ADMIN` realm role and a permanent password
- Two client secrets ready to paste into `RENTAL_SERVICE_CLIENT_SECRET` and
  `GATEWAY_SERVICE_CLIENT_SECRET` (see root `README.md`)

## A note on the simplification here

Resource servers in this project only validate the JWT's issuer and signature, not
its audience (`aud`). That means any token issued by the `rental-app` realm —
regardless of which client it was issued to — is accepted by every backend service.
That's fine for this demo; hardening it later means adding an audience mapper to each
client and an `aud` check in each service's resource-server config.
