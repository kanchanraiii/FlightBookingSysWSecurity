package com.apigateway.dto;

public record UpdatePasswordRequest(
		String username,
        String email,
        String oldPassword,
        String newPassword
) {

}
