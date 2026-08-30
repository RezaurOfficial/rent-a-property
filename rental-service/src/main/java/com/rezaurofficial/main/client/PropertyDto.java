package com.rezaurofficial.main.client;

/** Only the fields rental-service actually reads from property-service's Property. */
public record PropertyDto(Long id, PropertyStatus status) {
}
