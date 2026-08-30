package com.rezaurofficial.main.property;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

	private final PropertyRepository propertyRepository;

	@GetMapping
	public List<Property> list(@RequestParam(required = false) PropertyStatus status) {
		return status == null ? propertyRepository.findAll() : propertyRepository.findByStatus(status);
	}

	@GetMapping("/{id}")
	public Property get(@PathVariable Long id) {
		return findOrThrow(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public Property create(@RequestBody PropertyRequest request) {
		Property property = new Property();
		applyRequest(property, request);
		return propertyRepository.save(property);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public Property update(@PathVariable Long id, @RequestBody PropertyRequest request) {
		Property property = findOrThrow(id);
		applyRequest(property, request);
		return propertyRepository.save(property);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMIN')")
	public void delete(@PathVariable Long id) {
		propertyRepository.delete(findOrThrow(id));
	}

	@PutMapping("/{id}/status")
	@PreAuthorize("hasRole('SERVICE')")
	public Property updateStatus(@PathVariable Long id, @RequestBody PropertyStatusUpdateRequest request) {
		Property property = findOrThrow(id);
		property.setStatus(request.status());
		return propertyRepository.save(property);
	}

	private Property findOrThrow(Long id) {
		return propertyRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
	}

	private void applyRequest(Property property, PropertyRequest request) {
		property.setTitle(request.title());
		property.setDescription(request.description());
		property.setAddress(request.address());
		property.setCity(request.city());
		property.setPricePerMonth(request.pricePerMonth());
		property.setBedrooms(request.bedrooms());
	}
}
