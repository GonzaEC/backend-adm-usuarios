package com.plataforma.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.plataforma.constant.RoleConstants;

import com.plataforma.repository.RoleRepository;
import com.plataforma.repository.UserRepository;

import com.plataforma.model.User;
import com.plataforma.model.Role;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class RbacIntegrationTest
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

	private Long createUserAndGetId(String email, String password) throws Exception {
		String body = String.format(
			"{\"email\": \"%s\", \"password\": \"%s\"}",
			email, password
		);
	
		MvcResult result = mockMvc.perform(post("/api/users")
			.contentType(MediaType.APPLICATION_JSON)
			.content(body))
			.andExpect(status().isCreated())
			.andReturn();
	
		String response = result.getResponse().getContentAsString();
		Map<String, Object> json = objectMapper.readValue(response, Map.class);
	
		Map<String, Object> data = (Map<String, Object>) json.get("data");
	
		return Long.valueOf(data.get("id").toString());
	}

	private Long createRoleAndGetId(String adminToken) throws Exception
	{
		String body = "{\"name\":\"ROLE_TEST\",\"description\":\"test\"}";
	
		MvcResult result = mockMvc.perform(post("/api/roles")
			.header("Authorization", bearer(adminToken))
			.contentType(MediaType.APPLICATION_JSON)
			.content(body))
			.andExpect(status().isCreated())
			.andReturn();
	
		String response = result.getResponse().getContentAsString();
		Map<String, Object> json = objectMapper.readValue(response, Map.class);
	
		Map<String, Object> data = (Map<String, Object>) json.get("data");
	
		return Long.valueOf(data.get("id").toString());
	}

	private String createAdminAndLogin() throws Exception
	{

		Role adminRole = roleRepository.findByName(RoleConstants.ADMIN)
			.orElseThrow();
	
		String email = "admin+" + System.nanoTime() + "@mail.com";
	
		User admin = User.builder()
			.email(email)
			.password(passwordEncoder.encode("admin123"))
			.role(adminRole)
			.active(true)
			.build();
	
		userRepository.save(admin);
	
		return loginAndGetToken(email, "admin123");
	}

	private String createBasicAndLogin() throws Exception
	{

		String email = "user+" + System.nanoTime() + "@mail.com";
	
		// usar endpoint real → mejor test de integración
		mockMvc.perform(post("/api/users")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"email\":\"" + email + "\",\"password\":\"1234\"}"))
			.andExpect(status().isCreated());
	
		return loginAndGetToken(email, "1234");
	}

	// Helper para no repetir el header de Authorization
	private String bearer(String token)
	{
		return "Bearer " + token;
	}

	private String loginAndGetToken(String email, String password) throws Exception
	{
		String body = String.format(
			"{\"email\": \"%s\", \"password\": \"%s\"}",
			email, password
		);
	
		MvcResult result = mockMvc.perform(post("/api/auth/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content(body))
			.andExpect(status().isOk())
			.andReturn();
	
		String response = result.getResponse().getContentAsString();
		return (String) objectMapper.readValue(response, Map.class).get("data");
	}

	private User createAdmin()
	{
		Role adminRole = roleRepository.findByName(RoleConstants.ADMIN)
			.orElseThrow();
	
		User admin = User.builder()
			.email("admin+" + System.nanoTime() + "@mail.com")
			.password(passwordEncoder.encode("admin123"))
			.role(adminRole)
			.active(true)
			.build();
	
		return userRepository.save(admin);
	}

	private String login(User user) throws Exception
	{
		return loginAndGetToken(user.getEmail(), "admin123");
	}

	// --- GRUPO 1: AUTH ---

	@Test
	void loginAdmin() throws Exception
	{
		String token = createAdminAndLogin();
		Assertions.assertNotNull(token);
	}

	@Test
	@DisplayName("Auth: Cambiar contraseña")
	void changePassword() throws Exception
	{
		String adminToken = createAdminAndLogin();
		String body = "{\"oldPassword\": \"admin123\", \"newPassword\": \"admin456\"}";
		mockMvc.perform(post("/api/auth/change-password")
			.header("Authorization", bearer(adminToken))
			.contentType(MediaType.APPLICATION_JSON)
			.content(body))
			.andExpect(status().isOk());
	}

	// --- GRUPO 2: USUARIOS ---

	@Test
	@DisplayName("Usuarios: Crear usuario BASIC")
	void createUser() throws Exception
	{
		String body = "{\"email\": \"juan@mail.com\", \"password\": \"1234\"}";
		mockMvc.perform(post("/api/users")
			.contentType(MediaType.APPLICATION_JSON)
			.content(body))
			.andExpect(status().isCreated())
			.andExpect(
				jsonPath("$.data.email").value("juan@mail.com")
			);
	}

	@Test
	@DisplayName("Usuarios: Listar paginado")
	void listUsers() throws Exception
	{
		String adminToken = createAdminAndLogin();
		mockMvc.perform(get("/api/users")
			.header("Authorization", bearer(adminToken))
			.param("page", "0")
			.param("size", "10"))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("Usuarios: Detalle usuario 1")
	void userDetail() throws Exception
	{
		String adminToken = createAdminAndLogin();
		Long userId = createUserAndGetId(
			"user+" + System.nanoTime() + "@mail.com",
			"1234"
		);
		mockMvc.perform(get("/api/users/"+userId)
			.header("Authorization", bearer(adminToken)))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("Usuarios: Editar usuario 2")
	void editUser() throws Exception
	{
		String adminToken = createAdminAndLogin();
		Long userId = createUserAndGetId(
			"user+" + System.nanoTime() + "@mail.com",
			"1234"
		);
		String body = "{\"email\": \"nuevo@mail.com\"}";
		mockMvc.perform(put("/api/users/"+userId)
			.header("Authorization", bearer(adminToken))
			.contentType(MediaType.APPLICATION_JSON)
			.content(body))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("Usuarios: Asignar rol DEVELOPER")
	void assignRole() throws Exception
	{
		String adminToken = createAdminAndLogin();
		Long userId = createUserAndGetId(
			"user+" + System.nanoTime() + "@mail.com",
			"1234"
		);
		String body = "{\"roleId\": 3}"; // PUEDE FALLAR SI NO EXISTE!!
		mockMvc.perform(put("/api/users/"+userId+"/role")
			.header("Authorization", bearer(adminToken))
			.contentType(MediaType.APPLICATION_JSON)
			.content(body))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("Usuarios: Soft Delete")
	void deleteUser() throws Exception
	{
		String adminToken = createAdminAndLogin();
		Long userId = createUserAndGetId(
			"user+" + System.nanoTime() + "@mail.com",
			"1234"
		);
		mockMvc.perform(delete("/api/users/"+userId)
			.header("Authorization", bearer(adminToken)))
			.andExpect(status().isOk());
	}

	// --- GRUPO 3: ROLES ---

	@Test
	@DisplayName("Roles: Listar roles con permisos")
	void listRoles() throws Exception
	{
		String adminToken = createAdminAndLogin();
		mockMvc.perform(get("/api/roles")
			.header("Authorization", bearer(adminToken)))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("Roles: Crear nuevo rol")
	void createRole() throws Exception
	{
		String adminToken = createAdminAndLogin();
		String body = "{\"name\": \"MODERADOR\", \"description\": \"Modera contenido\"}";
		mockMvc.perform(post("/api/roles")
			.header("Authorization", bearer(adminToken))
			.contentType(MediaType.APPLICATION_JSON)
			.content(body))
			.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("Roles: Asignar permisos a rol")
	void assignPermissionsToRole() throws Exception
	{
		String adminToken = createAdminAndLogin();
		Long roleId = createRoleAndGetId(adminToken);
		String body = "[1, 2]"; // PUEDE FALLAR SI NO EXISTEN
		mockMvc.perform(put("/api/roles/"+roleId+"/permissions")
			.header("Authorization", bearer(adminToken))
			.contentType(MediaType.APPLICATION_JSON)
			.content(body))
			.andExpect(status().isOk());
	}

	// --- GRUPO 4: PERMISOS ---

	@Test
	@DisplayName("Permisos: Crear permiso")
	void createPermission() throws Exception
	{
		String adminToken = createAdminAndLogin();
		String body = "{\"name\": \"report:read\", \"description\": \"Ver reportes\"}";
		mockMvc.perform(post("/api/permissions")
			.header("Authorization", bearer(adminToken))
			.contentType(MediaType.APPLICATION_JSON)
			.content(body))
			.andExpect(status().isCreated());
	}

	// --- GRUPO 5: AUTORIZACIÓN (CASOS DE ERROR) ---

	@Test
	@DisplayName("Error: 401 Sin Token")
	void error401NoToken() throws Exception
	{
		mockMvc.perform(get("/api/roles"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Error: 401 Token Inválido")
	void error401InvalidToken() throws Exception
	{
		mockMvc.perform(get("/api/roles")
			.header("Authorization", bearer("token-falso")))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Error: 403 Usuario BASIC intenta borrar rol")
	void basicCannotDeleteRole() throws Exception
	{
		String adminToken = createAdminAndLogin();
		String userToken  = createBasicAndLogin();
	
		mockMvc.perform(delete("/api/roles/1")
			.header("Authorization", bearer(userToken)))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Error: Admin no puede demotear a otro Admin")
	void errorAdminDemote() throws Exception
	{
		User admin1 = createAdmin();
		User admin2 = createAdmin();
	
		String tokenAdmin1 = login(admin1);
		String tokenAdmin2 = login(admin2);
	
		String body = "{\"roleId\": 2}"; // BASIC o cualquier no-admin
	
		mockMvc.perform(put("/api/users/" + admin1.getId() + "/role")
			.header("Authorization", bearer(tokenAdmin2))
			.contentType(MediaType.APPLICATION_JSON)
			.content(body))
			.andExpect(status().isForbidden());
	}

	@Test
	void adminCanDeleteRole() throws Exception
	{
		String adminToken = createAdminAndLogin();

		mockMvc.perform(delete("/api/roles/1")
			.header("Authorization", bearer(adminToken)))
			.andExpect(status().isOk());
		}
}