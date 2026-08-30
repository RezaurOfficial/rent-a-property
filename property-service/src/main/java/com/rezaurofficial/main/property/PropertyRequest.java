package com.rezaurofficial.main.property;

import java.math.BigDecimal;

public record PropertyRequest(
		String title,
		String description,
		String address,
		String city,
		BigDecimal pricePerMonth,
		int bedrooms) {
}
