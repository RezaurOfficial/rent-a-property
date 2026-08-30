package com.rezaurofficial.main.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Minimal, display-only view of a Keycloak user — never persisted, always fetched
 * live. Deserialized directly from Keycloak's much larger UserRepresentation JSON.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserSummary(String id, String username, String email, String firstName, String lastName) {
}
