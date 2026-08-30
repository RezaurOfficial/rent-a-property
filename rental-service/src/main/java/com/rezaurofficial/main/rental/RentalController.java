package com.rezaurofficial.main.rental;

import com.rezaurofficial.main.client.PropertyDto;
import com.rezaurofficial.main.client.PropertyServiceClient;
import com.rezaurofficial.main.client.PropertyStatus;
import com.rezaurofficial.main.client.PropertyStatusUpdateRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

	private final RentalRepository rentalRepository;
	private final PropertyServiceClient propertyServiceClient;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('USER')")
	public Rental rent(@RequestBody RentalRequest request, Authentication authentication) {
		PropertyDto property;
		try {
			property = propertyServiceClient.getProperty(request.propertyId());
		} catch (FeignException.NotFound e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found");
		}
		if (property.status() != PropertyStatus.AVAILABLE) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Property is not available");
		}

		Rental rental = new Rental();
		rental.setPropertyId(request.propertyId());
		rental.setUserId(subject(authentication));
		rental.setStartDate(LocalDate.now());
		rental = rentalRepository.save(rental);

		propertyServiceClient.updateStatus(request.propertyId(), new PropertyStatusUpdateRequest(PropertyStatus.RENTED));
		return rental;
	}

	@PostMapping("/{id}/cancel")
	public Rental cancel(@PathVariable Long id, Authentication authentication) {
		Rental rental = findOrThrow(id);
		if (!rental.getUserId().equals(subject(authentication)) && !isAdmin(authentication)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot cancel someone else's rental");
		}
		rental.setStatus(RentalStatus.CANCELLED);
		rental = rentalRepository.save(rental);

		propertyServiceClient.updateStatus(rental.getPropertyId(), new PropertyStatusUpdateRequest(PropertyStatus.AVAILABLE));
		return rental;
	}

	@GetMapping("/my")
	public List<Rental> myRentals(Authentication authentication) {
		return rentalRepository.findByUserId(subject(authentication));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public List<Rental> allRentals() {
		return rentalRepository.findAll();
	}

	private Rental findOrThrow(Long id) {
		return rentalRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental not found"));
	}

	private String subject(Authentication authentication) {
		return ((Jwt) authentication.getPrincipal()).getSubject();
	}

	private boolean isAdmin(Authentication authentication) {
		return authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
	}
}
