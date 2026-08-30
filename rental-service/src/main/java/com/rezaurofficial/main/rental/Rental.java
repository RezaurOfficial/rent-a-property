package com.rezaurofficial.main.rental;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "rentals")
@Getter
@Setter
@NoArgsConstructor
public class Rental {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long propertyId;

	@Column(nullable = false)
	private String userId;

	@Column(nullable = false)
	private LocalDate startDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RentalStatus status = RentalStatus.ACTIVE;
}
