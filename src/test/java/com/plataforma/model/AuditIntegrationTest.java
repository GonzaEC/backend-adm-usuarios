// src/test/java/com/plataforma/model/AuditIntegrationTest.java
package com.plataforma.model;

import com.plataforma.constant.RoleConstants;
import com.plataforma.repository.RoleRepository;
import com.plataforma.repository.UserRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuditIntegrationTest
{

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Test
	void shouldSetCreatedAtOnUserPersist()
	{
		Role role = roleRepository.findByName(RoleConstants.BASIC)
			.orElseThrow();

		User user = User.builder()
			.email("audit@test.com")
			.password("123")
			.role(role)
			.build();

		User saved = userRepository.save(user);

		assertNotNull(
			saved.getCreatedAt(), "createdAt debería setearse automáticamente"
		);
		assertNull(
			saved.getUpdatedAt(), "updatedAt debería ser null en creación"
		);
	}

	@Test
	void shouldSetUpdatedAtOnUserUpdate() throws InterruptedException
	{
		Role role = roleRepository.findByName(RoleConstants.BASIC)
			.orElseThrow();

		User user = userRepository.save(
			User.builder()
				.email("audit-update@test.com")
				.password("123")
				.role(role)
				.build()
		);

		LocalDateTime createdAt = user.getCreatedAt();

		// Simular update real
		user.setPassword("456");

		Thread.sleep(5); // evitar mismo timestamp

		User updated = userRepository.saveAndFlush(user); // Guardo y fuerzo evento
		// El save comun no siempre fuerza el @PreUpdate inmediatamente
		// En este caso particular, queda cacheado y luego se aplica el assert.
		// Cuando lo testea detecta que no paso, cuando en realidad si.

		assertNotNull(
			updated.getUpdatedAt(), "updatedAt debería setearse en update"
		);
		assertTrue(
			updated.getUpdatedAt().isAfter(createdAt),
			"updatedAt debería ser posterior a createdAt"
		);
	}

	// Test para revisar si hibernate esta usando lifecycle hooks
	// De lo contrario hay que usar flush
	@Test
	void shouldTriggerPreUpdateOnlyOnDirtyEntity()
	{
		Role role = roleRepository.findByName(RoleConstants.BASIC).orElseThrow();

		User user = userRepository.save(
			User.builder().email("dirty@test.com").role(role).build()
		);

		LocalDateTime updatedBefore = user.getUpdatedAt();
	
		userRepository.save(user); // NO se cambia nada
	
		assertEquals(
			updatedBefore,
			user.getUpdatedAt(),
			"No debería actualizar updatedAt si no hubo cambios"
		);
	}

	@Test
	void shouldPersistAuditFieldsInDatabase()
	{
		Role role = roleRepository.findByName(RoleConstants.BASIC)
			.orElseThrow();

		User user = userRepository.save(
			User.builder()
				.email("db@test.com")
				.password("123")
				.role(role)
				.build()
		);

		// Forzar reload desde DB (Hibernate real)
		User reloaded = userRepository.findById(user.getId()).orElseThrow();

		assertNotNull(reloaded.getCreatedAt(), "Debe persistirse en DB");
	}

	@Test
	void shouldApplyAuditToMultipleEntities()
	{
		Role role = roleRepository.save(
			Role.builder()
				.name("TEST_ROLE")
				.build()
		);

		assertNotNull(role.getCreatedAt(), "Role debería tener auditoría");

		User user = userRepository.save(
			User.builder()
				.email("multi@test.com")
				.password("123")
				.role(role)
				.build()
		);

		assertNotNull(user.getCreatedAt(), "User debería heredar auditoría");
	}
}