// src/test/java/com/plataforma/controller/SecurityUnitTest.java
package com.plataforma.controller;

import com.plataforma.model.Permission;
import com.plataforma.model.Role;
import com.plataforma.model.User;
import com.plataforma.repository.UserRepository;
import com.plataforma.security.JwtUtils;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de seguridad: filtro JWT + @PreAuthorize.
 * Estrategia: Spring completo con H2, UserRepository mockeado.
 *
 * CONVENCIÓN: los permisos usan formato "recurso:accion" en minúsculas,
 * igual que en V2__seed_roles_permissions.sql y data.sql.
 * El filtro los carga como SimpleGrantedAuthority(permission.getName())
 * sin ninguna transformación → @PreAuthorize debe usar el mismo formato.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("unit")
class SecurityUnitTest
{
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtils jwtUtils;

	@MockBean
	private UserRepository userRepository;

	// ── Helpers ────────────────────────────────────────────────────────────────

	/**
	 * Genera un token JWT firmado y configura el mock del repositorio para que
	 * el filtro encuentre al usuario cuando valide el token.
	 *
	 * @param email     email del usuario ficticio
	 * @param roleName  nombre del rol (ej. "DEVELOPER", "ADMIN")
	 * @param permisos  lista de authorities que tendrá el usuario
	 * @return token JWT válido
	 */
	private String buildTokenWithMockedUser(
		String email, String roleName, String... permisos
	)
	{
		Set<Permission> perms = Arrays.stream(permisos)
			.map(p -> Permission.builder().name(p).build())
			.collect(Collectors.toSet());

		Role role = Role.builder().name(roleName).permissions(perms).build();
		User mockUser = User.builder().id(1L).email(email).role(role).build();

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

		return jwtUtils.generateToken(mockUser);
	}

	// ── Ítem 1: middleware 401 ──────────────────────────────────────────────

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

	// ── Ítem 2: autorización por permiso ───────────────────────────────────

	@Test
	void debeDevolver403_sinPermisoProjectDelete() throws Exception
	{
		String token = buildTokenWithMockedUser(
			"dev@mail.com", "DEVELOPER", "project:read"
			// nota: PROJECT_DELETE no está → debe fallar con 403
		);

		mockMvc.perform(delete("/api/test-security/borrar")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
			.andExpect(status().isForbidden());
	}

	@Test
	void debeDevolver200_conPermisoProjectDelete() throws Exception
	{
		String token = buildTokenWithMockedUser(
			"admin@mail.com", "ADMIN", "project:delete"
		);

		mockMvc.perform(delete("/api/test-security/borrar")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
			.andExpect(status().isOk());
	}
}