package com.rezaurofficial.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "property-service")
public interface PropertyServiceClient {

	@GetMapping("/api/properties/{id}")
	PropertyDto getProperty(@PathVariable("id") Long id);

	@PutMapping("/api/properties/{id}/status")
	void updateStatus(@PathVariable("id") Long id, @RequestBody PropertyStatusUpdateRequest request);
}
