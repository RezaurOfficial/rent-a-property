package com.rezaurofficial.main.gateway;

/**
 * A single entry in the gateway's hand-written routing table: any request path
 * starting with {@code pathPrefix} is forwarded to the Eureka-registered
 * {@code serviceId}. No YAML/predicate DSL — this list is the whole routing table.
 */
public record RouteDefinition(String pathPrefix, String serviceId) {
}
