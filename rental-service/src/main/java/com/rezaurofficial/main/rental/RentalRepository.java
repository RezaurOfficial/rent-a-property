package com.rezaurofficial.main.rental;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {

	List<Rental> findByUserId(String userId);
}
