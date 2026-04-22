// src/test/java/com/plataforma/model/UserAuditTest.java
package com.plataforma.model;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
class UserAuditTest
{

	@Autowired
	private TestEntityManager em;

	public static void assertAuditOnCreate(Auditable entity)
	{
		assertNotNull(entity.getCreatedAt());
		assertNull(entity.getUpdatedAt());
	}

	public static void assertAuditOnUpdate(
		Auditable entity, LocalDateTime createdAt
	)
	{
		assertNotNull(entity.getUpdatedAt());
		assertTrue(entity.getUpdatedAt().isAfter(createdAt));
	}

	@Test
	void shouldSetCreatedAtOnPersist()
	{
		Role role = Role.builder()
			.name("ADMIN")
			.build();

		em.persist(role);

		User user = User.builder()
			.email("test@test.com")
			.password("123")
			.role(role)
			.build();

		em.persist(user);
		em.flush(); // fuerza el @PrePersist

		assertAuditOnCreate(user);
	}

	@Test
	void shouldSetUpdatedAtOnUpdate() throws InterruptedException
	{
		Role role = Role.builder()
			.name("ADMIN")
			.build();

		em.persist(role);

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
		em.persist(user);
		em.flush();

		assertAuditOnUpdate(user, createdAt);
	}

	@Test
	void projectShouldHaveAuditFields()
	{
		Role role = em.persist(Role.builder().name("ADMIN").build());

		User user = em.persist(User.builder()
			.email("owner@test.com")
			.password("123")
			.role(role)
			.build());

		Project project = Project.builder()
			.name("Test Project")
			.owner(user)
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
