package com.rezaurofficial.main.client;

import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.security.OAuth2AccessTokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * Lets rental-service call property-service as itself (client_credentials), independent
 * of any end-user's token, for the internal SERVICE-role-gated status update endpoint.
 */
@Configuration
public class FeignClientCredentialsConfig {

	private static final String REGISTRATION_ID = "rental-service";

	@Bean
	public OAuth2AuthorizedClientManager authorizedClientManager(
			ClientRegistrationRepository clientRegistrationRepository,
			OAuth2AuthorizedClientService authorizedClientService) {

		AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
				new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
		manager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build());
		return manager;
	}

	@Bean
	public RequestInterceptor propertyServiceAuthInterceptor(OAuth2AuthorizedClientManager authorizedClientManager) {
		return new OAuth2AccessTokenInterceptor(REGISTRATION_ID, authorizedClientManager);
	}
}
