package com.rezaurofficial.main.auth;

public record RegisterRequest(
		String username,
		String email,
		String password,
		String firstName,
		String lastName) {
}
