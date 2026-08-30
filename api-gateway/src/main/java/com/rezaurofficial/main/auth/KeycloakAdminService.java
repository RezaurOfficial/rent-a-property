package com.rezaurofficial.main.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Registers new end users directly in Keycloak (the source of truth for user data)
 * using a service-account client with the realm-management "realm-admin" role —
 * this service never stores credentials or profile data itself. "manage-users" alone
 * is not enough here: it covers user CRUD but not reading/mapping realm roles.
 */
@Service
public class KeycloakAdminService {

	private static final String REGISTRATION_ID = "gateway-service";

	private final RestClient restClient;
	private final OAuth2AuthorizedClientManager authorizedClientManager;

	@Value("${keycloak.base-url}")
	private String baseUrl;

	@Value("${keycloak.realm}")
	private String realm;

	public KeycloakAdminService(RestClient.Builder restClientBuilder, OAuth2AuthorizedClientManager authorizedClientManager) {
		this.restClient = restClientBuilder.build();
		this.authorizedClientManager = authorizedClientManager;
	}

	public void registerUser(RegisterRequest request) {
		String adminToken = fetchAdminToken();
		String userId = createUser(adminToken, request);
		assignRealmRole(adminToken, userId, "USER");
	}

	public UserSummary getUser(String userId) {
		String adminToken = fetchAdminToken();

		try {
			return restClient.get()
					.uri(baseUrl + "/admin/realms/" + realm + "/users/" + userId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
					.retrieve()
					.body(UserSummary.class);
		} catch (RestClientResponseException e) {
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
			}
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not fetch user from Keycloak");
		}
	}

	private String fetchAdminToken() {
		OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
				.withClientRegistrationId(REGISTRATION_ID)
				.principal(REGISTRATION_ID)
				.build();

		OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
		if (authorizedClient == null) {
			throw new IllegalStateException("Unable to authorize " + REGISTRATION_ID);
		}
		return authorizedClient.getAccessToken().getTokenValue();
	}

	private String createUser(String adminToken, RegisterRequest request) {
		Map<String, Object> userRepresentation = Map.of(
				"username", request.username(),
				"email", request.email(),
				"firstName", request.firstName(),
				"lastName", request.lastName(),
				"enabled", true,
				"emailVerified", true,
				"credentials", List.of(Map.of(
						"type", "password",
						"value", request.password(),
						"temporary", false)));

		ResponseEntity<Void> response;
		try {
			response = restClient.post()
					.uri(baseUrl + "/admin/realms/" + realm + "/users")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
					.contentType(MediaType.APPLICATION_JSON)
					.body(userRepresentation)
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientResponseException e) {
			if (e.getStatusCode() == HttpStatus.CONFLICT) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Username or email already taken");
			}
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not create user in Keycloak");
		}

		String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
		if (location == null) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Keycloak did not return the new user's location");
		}
		return location.substring(location.lastIndexOf('/') + 1);
	}

	private void assignRealmRole(String adminToken, String userId, String roleName) {
		Object role = restClient.get()
				.uri(baseUrl + "/admin/realms/" + realm + "/roles/" + roleName)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
				.retrieve()
				.body(Object.class);

		restClient.post()
				.uri(baseUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.body(List.of(role))
				.retrieve()
				.toBodilessEntity();
	}
}
