package com.rezaurofficial.main.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Hand-written API gateway routing: no Spring Cloud Gateway, no predicate DSL. A
 * request under one of the prefixes below is resolved to a live instance via Eureka
 * and forwarded as-is (method, headers, body); the downstream response is streamed
 * straight back to the caller.
 */
@RestController
public class GatewayController {

	private static final List<RouteDefinition> ROUTES = List.of(
			new RouteDefinition("/api/properties", "property-service"),
			new RouteDefinition("/api/rentals", "rental-service"));

	private static final List<String> SKIPPED_HEADERS = List.of(HttpHeaders.HOST, HttpHeaders.CONTENT_LENGTH);

	private final DiscoveryClient discoveryClient;
	private final RestClient restClient;

	public GatewayController(DiscoveryClient discoveryClient, RestClient.Builder restClientBuilder) {
		this.discoveryClient = discoveryClient;
		this.restClient = restClientBuilder.build();
	}

	@RequestMapping("/api/**")
	public ResponseEntity<byte[]> handle(HttpServletRequest request) throws IOException {
		String path = request.getRequestURI();

		RouteDefinition route = ROUTES.stream()
				.filter(candidate -> path.startsWith(candidate.pathPrefix()))
				.findFirst()
				.orElse(null);

		if (route == null) {
			return ResponseEntity.notFound().build();
		}

		List<ServiceInstance> instances = discoveryClient.getInstances(route.serviceId());
		if (instances.isEmpty()) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
		}
		ServiceInstance instance = instances.get(0);

		String query = request.getQueryString();
		URI targetUri = URI.create(instance.getUri() + path + (query != null ? "?" + query : ""));

		byte[] body = request.getInputStream().readAllBytes();

		RestClient.RequestBodySpec requestSpec = restClient
				.method(HttpMethod.valueOf(request.getMethod()))
				.uri(targetUri);

		for (String headerName : Collections.list(request.getHeaderNames())) {
			if (SKIPPED_HEADERS.stream().anyMatch(headerName::equalsIgnoreCase)) {
				continue;
			}
			for (String value : Collections.list(request.getHeaders(headerName))) {
				requestSpec = requestSpec.header(headerName, value);
			}
		}

		try {
			return (body.length > 0 ? requestSpec.body(body) : requestSpec)
					.retrieve()
					.toEntity(byte[].class);
		} catch (RestClientResponseException e) {
			return ResponseEntity.status(e.getStatusCode())
					.headers(e.getResponseHeaders())
					.body(e.getResponseBodyAsByteArray());
		}
	}
}
