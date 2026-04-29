// src/test/java/com/plataforma/controller/SecurityIntegrationTest.java
package com.plataforma.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma.AbstractIntegrationTest;
import com.plataforma.constant.RoleConstants;
import com.plataforma.model.Role;
import com.plataforma.model.User;
import com.plataforma.repository.RoleRepository;
import com.plataforma.repository.UserRepository;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de seguridad contra Postgres real (Testcontainers).
 *
 * Estrategia INTEGRATION:
 *   - Extiende AbstractIntegrationTest → Postgres real en Docker.
 *   - Sin MockBean: todos los repositorios son reales.
 *   - El flujo completo es: registrar usuario → login → obtener token → llamar endpoint.
 *   - Flyway aplica las migraciones reales antes de cada suite.
 *
 * Lo que se verifica aquí:
 *   1. 401 sin token (igual que unit, pero con Postgres real en el stack completo).
 *   2. 401 con token inválido (ídem).
 *   3. 403 cuando un usuario DEVELOPER (sin PROJECT_DELETE) intenta borrar.
 *   4. 200 cuando un usuario ADMIN (con PROJECT_DELETE) intenta borrar.
 *
 * La diferencia respecto a SecurityUnitTest es que aquí el token es emitido
 * tras un login real con credenciales persistidas en Postgres, garantizando
 * que el flujo completo de autenticación/autorización funciona end-to-end.
 */
@Tag("integration")
class SecurityIntegrationTest extends AbstractIntegrationTest
{
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	// ── Helpers ────────────────────────────────────────────────────────────────

	/** Persiste un usuario con el rol dado y devuelve su token JWT. */
	private String crearUsuarioYLogin(String roleName) throws Exception
	{
		Role role = roleRepository.findByName(roleName).orElseThrow();
		String email = roleName.toLowerCase() + "+" + System.nanoTime() + "@mail.com";

		userRepository.save(User.builder()
			.email(email)
			.password(passwordEncoder.encode("pass123"))
			.role(role)
			.active(true)
			.build());

		return loginYObtenerToken(email, "pass123");
	}

	/** Llama al endpoint de login real y extrae el token de la respuesta. */
	private String loginYObtenerToken(String email, String password) throws Exception
	{
		String body = String.format(
			"{\"email\": \"%s\", \"password\": \"%s\"}", email, password
		);

		MvcResult result = mockMvc.perform(post("/api/auth/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content(body))
			.andExpect(status().isOk())
			.andReturn();

		Map<?, ?> json = objectMapper.readValue(
			result.getResponse().getContentAsString(), Map.class
		);
		return (String) json.get("data");
	}

	// ── Tests: 401 ─────────────────────────────────────────────────────────────

	@Test
	void debeDevolver401_sinToken() throws Exception
	{
		mockMvc.perform(get("/api/proyectos"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void debeDevolver401_tokenInvalido() throws Exception
	{
		mockMvc.perform(get("/api/proyectos")
			.header(HttpHeaders.AUTHORIZATION, "Bearer token-basura-123"))
			.andExpect(status().isUnauthorized());
	}

	// ── Tests: autorización por rol/permiso (flujo end-to-end) ─────────────────

	@Test
	void debeDevolver403_developerSinPermisoProjectDelete() throws Exception
	{
		String tokenDev = crearUsuarioYLogin(RoleConstants.DEVELOPER);

		mockMvc.perform(delete("/api/test-security/borrar")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDev))
			.andExpect(status().isForbidden());
	}

	@Test
	void debeDevolver200_adminConPermisoProjectDelete() throws Exception
	{
		String tokenAdmin = crearUsuarioYLogin(RoleConstants.ADMIN);

		mockMvc.perform(delete("/api/test-security/borrar")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
			.andExpect(status().isOk());
	}
}