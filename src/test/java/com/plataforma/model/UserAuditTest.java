// src/test/java/com/plataforma/model/UserAuditTest.java
package com.plataforma.model;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
 
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de auditoría JPA (@PrePersist / @PreUpdate).
 *
 * Usa @DataJpaTest: contexto JPA mínimo con H2, sin capa web, sin servicios.
 * NO depende de data.sql ni de ningún seed externo — cada test crea
 * sus propios datos con em.persist() para ser completamente autosuficiente.
 *
 * @DataJpaTest no ejecuta data.sql por defecto (a diferencia de @SpringBootTest),
 * por eso los tests que antes hacían roleRepository.findByName("ADMIN").orElseThrow()
 * fallaban con NoSuchElementException: la tabla estaba vacía.
 */
@DataJpaTest
@ActiveProfiles("test")
@Tag("unit")
class UserAuditTest
{

	@Autowired
	private TestEntityManager em;

	// ── Helpers estaticos reutilizables desde otros tests ──────────────────────

	public static void assertAuditOnCreate(Auditable entity)
	{
		assertNotNull(
			entity.getCreatedAt(), "createdAt debe setearse en @PrePersist"
		);
		assertNull(
			entity.getUpdatedAt(),
			"updatedAt debe ser null en creación"
		);
	}

	public static void assertAuditOnUpdate(
		Auditable entity, LocalDateTime createdAt
	)
	{
		assertNotNull(
			entity.getUpdatedAt(),
			"updatedAt debe setearse en @PreUpdate"
		);
		assertTrue(
			entity.getUpdatedAt().isAfter(createdAt),
			"updatedAt debe ser posterior a createdAt"
		);
	}

	/**
	 * Crea y persiste un Role mínimo sin depender de ningún seed.
	 * Cada test pasa un sufijo único para evitar colisiones de nombre.
	 */
	private Role persistRole(String nameSuffix)
	{
		Role role = Role.builder()
			.name("TEST_" + nameSuffix)
			.active(true)
			.build();
		return em.persist(role);
	}

	// ── Tests ──────────────────────────────────────────────────────────────────

	@Test
	void shouldSetCreatedAtOnPersist()
	{
		Role role = persistRole("CREATED");

		User user = User.builder()
			.email("create@test.com")
			.password("123")
			.role(role)
			.build();

		em.persist(user);
		em.flush(); // dispara @PrePersist

		assertAuditOnCreate(user);
	}

	@Test
	void shouldSetUpdatedAtOnUpdate() throws InterruptedException
	{
		Role role = persistRole("UPDATED");

		User user = User.builder()
			.email("test@test.com")
			.password("123")
			.role(role)
			.build();

		em.persist(user);
		em.flush();

		LocalDateTime createdAt = user.getCreatedAt();

		// Simular cambio
		user.setPassword("456");

		Thread.sleep(5); // evitar misma timestamp

		em.merge(user);
		em.flush();

		assertAuditOnUpdate(user, createdAt);
	}

	@Test
	void projectShouldHaveAuditFields()
	{
		Role role = persistRole("PROJECT");

		User user = em.persist(User.builder()
			.email("owner@test.com")
			.password("123")
			.role(role)
			.build());

		Project project = Project.builder()
			.name("Test Project")
			.owner(user)
			.tokenPrice(new java.math.BigDecimal("100.00"))
			.build();

			em.persist(project);
			em.flush();

		assertAuditOnCreate(project);
	}

	@Test
	void allEntitiesShouldExtendAuditable()
	{
		assertTrue(Auditable.class.isAssignableFrom(User.class));
		assertTrue(Auditable.class.isAssignableFrom(Role.class));
		assertTrue(Auditable.class.isAssignableFrom(Project.class));
		assertTrue(Auditable.class.isAssignableFrom(Investment.class));
	}
}
