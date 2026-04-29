package com.plataforma.service;

import com.plataforma.model.*;
import com.plataforma.repository.UserRepository;
import com.plataforma.AbstractIntegrationTest;
import com.plataforma.exception.UnauthorizedAccessException;
import com.plataforma.exception.UserNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Tag;

@Tag("integration")
class AuthIntegrationTest extends AbstractIntegrationTest
{
	@Autowired
	private AuthService authService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void shouldRegisterWithEncodedPassword() {
		User user = User.builder()
				.email("seguro@test.com")
				.password("password123")
				.build();

		User saved = userService.registerUser(user);

		// Verificar que la password NO sea texto plano
		assertNotEquals("password123", saved.getPassword());
		assertTrue(
				passwordEncoder.matches("password123", saved.getPassword()));
	}

	// Este haria la validacion.
	@Test
	void shouldLoginSuccessfullyAndReturnToken() {
		// Registrar un usuario
		userService.registerUser(User.builder()
				.email("fede@plataforma.com")
				.password("secreto")
				.build());

		// Intentar login
		String token = authService.login("fede@plataforma.com", "secreto");

		assertNotNull(token);
		assertFalse(token.isEmpty());
	}

	@Test
	void shouldFailLoginWithWrongPassword() {
		userService.registerUser(User.builder()
				.email("error@test.com")
				.password("123456")
				.build());

		assertThrows(UnauthorizedAccessException.class, () -> authService.login("error@test.com", "incorrecta"));
	}

	@Test
	void shouldChangePasswordSuccessfully() {
		// Crear usuario
		User user = userService.registerUser(User.builder()
				.email("cambio@test.com")
				.password("password_vieja")
				.build());

		// Guardar hash viejo en un String (fuera del objeto User)
		String hashOriginal = user.getPassword();

		// Ejecutar cambio de contraseña
		authService.changePassword(
				user.getId(),
				"password_vieja",
				"password_nueva");

		// Correr verificaciones
		User updatedUser = userRepository.findById(user.getId()).orElseThrow();

		// El hash debe haber cambiado
		assertNotEquals(hashOriginal, updatedUser.getPassword());

		// La nueva contraseña debe ser valida
		assertTrue(passwordEncoder.matches(
				"password_nueva", updatedUser.getPassword()));

		// Intentar login con la nueva debería funcionar
		assertDoesNotThrow(() -> authService.login(
				"cambio@test.com", "password_nueva"));
	}

	@Test
	void shouldFailChangePasswordWhenOldPasswordIsWrong() {
		User user = userService.registerUser(User.builder()
				.email("ataque@test.com")
				.password("original")
				.build());

		// Intentar cambiar con la vieja contraseña erronea
		assertThrows(UnauthorizedAccessException.class, () -> authService.changePassword(
				user.getId(), "equivocada", "nueva123"));
	}

	@Test
	void shouldFailWhenUserDoesNotExist() {
		// Intentar cambiar contraseña de un ID que no existe (ej: 999)
		assertThrows(UserNotFoundException.class, () -> authService.changePassword(
				999L,
				"cualquiera",
				"nueva123"));
	}
	
	/**
     * Verifica la invalidación de credenciales antiguas.
     * Este test asegura que, tras un cambio de contraseña exitoso, el sistema
     * rechace intentos de inicio de sesión con la clave anterior, garantizando
     * que solo el nuevo hash sea válido para la autenticación.
     */
	@Test
	void shouldNotLoginWithOldPasswordAfterChange()
	{
		// Caso de seguridad importante: la contraseña vieja queda inválida.
		User user = userService.registerUser(User.builder()
			.email("seguridad@test.com")
			.password("vieja")
			.build());

		authService.changePassword(user.getId(), "vieja", "nueva");

		// El login con la contraseña vieja DEBE fallar
		assertThrows(UnauthorizedAccessException.class, () ->
			authService.login("seguridad@test.com", "vieja"),
			"La contraseña anterior no debe funcionar tras el cambio"
		);
	}
}