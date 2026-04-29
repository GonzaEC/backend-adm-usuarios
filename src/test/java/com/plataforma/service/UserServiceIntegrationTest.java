// src/test/java/com/plataforma/service/UserServiceIntegrationTest.java
package com.plataforma.service;

import com.plataforma.model.*;

import com.plataforma.repository.ProjectRepository;
import com.plataforma.repository.RoleRepository;
import com.plataforma.repository.UserRepository;
import com.plataforma.AbstractIntegrationTest;
import com.plataforma.constant.RoleConstants;

import com.plataforma.exception.OwnershipException;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Tag;

@Tag("integration")
class UserServiceIntegrationTest extends AbstractIntegrationTest
{
	@Autowired
	private AccessControlService accessControlService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Test
	void shouldValidateOwnershipWithRealDatabaseData()
	{
		Role devRole = roleRepository.findByName(RoleConstants.DEVELOPER)
			.orElseThrow();
		// Guardar usuario real en H2
		User dev = User.builder()
			.email("dev@plataforma.com")
			.role(devRole)
			.build();

		User savedUser = userRepository.save(dev);

		// Crear proyecto vinculado a ese ID real
		Project project = Project.builder()
				.name("Energía Renovable")
				.owner(savedUser)
				.maxAmountTokens(100L)
				.tokenPrice(BigDecimal.valueOf(100.00))
				.build();
		Project savedProject = projectRepository.save(project);

		// Validar que el servicio reconoce la propiedad correctamente
		assertDoesNotThrow(
			() -> accessControlService.validateModifyProject(
				savedUser, savedProject
			)
		);
	}

	@Test
	void shouldThrowExceptionWhenUserIsNotOwnerInDatabase()
	{
		Role devRole = roleRepository.findByName(RoleConstants.DEVELOPER)
			.orElseThrow();

		User dev = userRepository.save(
			User.builder().role(devRole).build()
		);

		User dev2 = userRepository.save(
			User.builder().role(devRole).build()
		);

		Project othersProject = Project.builder().owner(dev2).build();

		assertThrows(OwnershipException.class, () -> 
			accessControlService.validateModifyProject(dev, othersProject)
		);
	}

	@Test
	void shouldFailWhenUserIsNotOwner_evenWithPersistedData()
	{
		Role devRole = roleRepository.findByName(RoleConstants.DEVELOPER)
			.orElseThrow();

		User owner = userRepository.save(
			User.builder().role(devRole).build()
		);

		User other = userRepository.save(
			User.builder().role(devRole).build()
		);

		Project project = projectRepository.save(
			Project.builder()
				.maxAmountTokens(100L)
				.tokenPrice(BigDecimal.valueOf(100.00))
				.owner(owner)
				.build()
		);

		assertThrows(OwnershipException.class, () ->
			accessControlService.validateModifyProject(other, project)
		);
	}

	/**
	 * Esto valida:
	 *   mapping @ManyToOne
	 *   query derivada de Spring Data
	 *   integridad
	*/
	@Test
	void shouldFindProjectsByOwner()
	{
		Role devRole = roleRepository.findByName(RoleConstants.DEVELOPER)
			.orElseThrow();

		User dev = userRepository.save(
			User.builder().role(devRole).build()
		);

		projectRepository.save(
			Project.builder()
				.owner(dev)
				.maxAmountTokens(100L)
				.tokenPrice(BigDecimal.valueOf(100.00))
				.name("Solar")
				.build()
		);

		projectRepository.save(
			Project.builder()
				.owner(dev)
				.maxAmountTokens(100L)
				.tokenPrice(BigDecimal.valueOf(100.00))
				.name("Eólico")
				.build()
		);

		var projects = projectRepository.findByOwner(dev);

		assertEquals(2, projects.size());
	}

	@Test
	void shouldFailWhenSavingProjectWithoutOwner()
	{
		Project project = Project.builder()
			.name("Proyecto inválido")
			.build();

		assertThrows(Exception.class, () ->
			projectRepository.save(project)
		);
	}
}