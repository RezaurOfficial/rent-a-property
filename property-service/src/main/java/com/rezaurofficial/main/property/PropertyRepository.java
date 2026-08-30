package com.rezaurofficial.main.property;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {

	List<Property> findByStatus(PropertyStatus status);
}
