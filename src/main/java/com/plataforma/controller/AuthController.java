// src/main/java/com/plataforma/controller/AuthController.java
package com.plataforma.controller;

// DTO
import com.plataforma.dto.ApiResponse;
import com.plataforma.dto.ChangePasswordRequest;
import com.plataforma.dto.LoginRequest;

// Model
import com.plataforma.model.User;

// Service
import com.plataforma.service.AuthService;

// Lombok
import lombok.RequiredArgsConstructor;

// Spring
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController
{

	private final AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<String>> login(
		@RequestBody LoginRequest request
	)
	{
		String token = authService.login(
			request.getEmail(), request.getPassword()
		);
		return ResponseEntity.ok(ApiResponse.success("Login exitoso", token));
	}

	// POST /api/auth/change-password
	// El usuario autenticado cambia su propia contraseña.
	// JwtAuthenticationFilter pone el User como principal, lo tomamos de ahí
	// para evitar que un cliente mande un userId arbitrario en el body.
	@PostMapping("/change-password")
	public ResponseEntity<ApiResponse<Void>> changePassword(
		@AuthenticationPrincipal User currentUser,
		@RequestBody ChangePasswordRequest request
	)
	{
		authService.changePassword(
			currentUser.getId(),
			request.getOldPassword(),
			request.getNewPassword()
		);
		return ResponseEntity.ok(
			ApiResponse.success("Contraseña actualizada", null)
		);
	}
}
