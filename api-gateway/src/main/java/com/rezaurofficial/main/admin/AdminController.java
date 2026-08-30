package com.rezaurofficial.main.admin;

import com.rezaurofficial.main.auth.KeycloakAdminService;
import com.rezaurofficial.main.auth.UserSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only identity lookups, resolved live against Keycloak (never cached/stored
 * here) so the UI can show who rented what without any service persisting user data.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

	private final KeycloakAdminService keycloakAdminService;

	@GetMapping("/users/{id}")
	public UserSummary getUser(@PathVariable String id) {
		return keycloakAdminService.getUser(id);
	}
}
