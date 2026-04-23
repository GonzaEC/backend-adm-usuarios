// src/test/java/com/plataforma/service/UserManagementIntegrationTest.java
package com.plataforma.service;

import com.plataforma.constant.RoleConstants;

import com.plataforma.model.*;

import com.plataforma.repository.RoleRepository;
import com.plataforma.repository.UserRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserManagementIntegrationTest
{
	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Test
	void shouldRegisterUserWithDefaultRole()
	{
		// Crear usuario sin rol especificado
		User newUser = User.builder()
			.email("test@plataforma.com")
			.password("123456")
			.build();

		User savedUser = userService.registerUser(newUser);

		assertNotNull(savedUser.getId());
		assertEquals(
			RoleConstants.BASIC,
			savedUser.getRole().getName(),
			"Debería asignar BASIC por defecto"
		);
		assertTrue(
			savedUser.isActive(),
			"El usuario debería estar activo al crearse"
		);
	}

	@Test
	void shouldUpdateUserFields()
	{
		Role devRole = roleRepository.findByName(RoleConstants.DEVELOPER)
			.orElseThrow();

		User user = userRepository.save(User.builder()
			.email("old@mail.com")
			.role(devRole)
			.active(true)
			.build()
		);

		user.setEmail("new@mail.com");
		User updated = userService.updateUser(user.getId(), user);

		assertEquals("new@mail.com", updated.getEmail());
	}

	@Test
	void shouldDeactivateUser_SoftDelete()
	{
		Role basic = roleRepository.findByName(RoleConstants.BASIC)
			.orElseThrow();

		// Baja: No se borra de la DB, desactivar (active = false)
		User user = userRepository.save(
			User.builder().role(basic).active(true).build()
		);

		userService.deactivateUser(user.getId());

		User deactivated = userRepository.findById(user.getId()).orElseThrow();
		assertFalse(
			deactivated.isActive(),
			"La baja debe ser un borrado lógico (desactivación)"
		);
	}

	@Test
	void shouldReturnPaginatedUsers()
	{
		Role basic = roleRepository.findByName(RoleConstants.BASIC)
			.orElseThrow();

		// Crear 3 usuarios
		userRepository.save(
			User.builder().role(basic).email("a@test.com").build()
		);
		userRepository.save(
			User.builder().role(basic).email("b@test.com").build()
		);
		userRepository.save(
			User.builder().role(basic).email("c@test.com").build()
		);

		// Pedir página 0, tamaño 2
		Page<User> userPage = userService.getAllUsers(
			PageRequest.of(0, 2)
		);

		assertEquals(
			2,
			userPage.getContent().size(),
			"La página debe tener 2 elementos"
		);
		assertEquals(3, userPage.getTotalElements(), "El total debería ser 3");
	}

	@Test
	void shouldFindUserDetail()
	{
		Role basic = roleRepository.findByName(RoleConstants.BASIC)
			.orElseThrow();
		// Detalle: Buscar por ID
		User user = userRepository.save(
			User.builder().role(basic).email("detail@test.com").build()
		);

		Optional<User> found = userService.getUserById(user.getId());

		assertTrue(found.isPresent());
		assertEquals("detail@test.com", found.get().getEmail());
	}
}