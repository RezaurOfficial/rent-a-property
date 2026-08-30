package com.rezaurofficial.main.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Maps Keycloak's realm_access.roles claim onto Spring Security ROLE_* authorities,
 * on top of the default scope-based authorities.
 */
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

	private final JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();

	@Override
	public Collection<GrantedAuthority> convert(Jwt jwt) {
		Collection<GrantedAuthority> scopeAuthorities = scopeConverter.convert(jwt);

		Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
		Collection<GrantedAuthority> realmRoleAuthorities = List.of();
		if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
			realmRoleAuthorities = roles.stream()
					.map(String.class::cast)
					.map(role -> "ROLE_" + role)
					.map(SimpleGrantedAuthority::new)
					.collect(Collectors.toList());
		}

		return Stream.concat(scopeAuthorities.stream(), realmRoleAuthorities.stream())
				.toList();
	}

	public static JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
		return converter;
	}
}
