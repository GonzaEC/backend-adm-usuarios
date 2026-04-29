// src/test/java/com/plataforma/service/UserServiceIntegrationTest.java
package com.plataforma.service;

import com.plataforma.model.*;

import com.plataforma.repository.ProjectRepository;
import com.plataforma.repository.RoleRepository;
import com.plataforma.repository.UserRepository;
import com.plataforma.constant.RoleConstants;

import com.plataforma.exception.OwnershipException;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Esto es un test de integracion. En caso que todo este ok, significa que:
 *   + Se instancio en memoria de trabajo una base de datos.
 *   + Se probo que la carga y recuperacion de datos funcionan.
 *   + Se aplicaron correctamente los test de control de acceso.
 *   + Se intancio usuarios en base de datos.
 *   + Se instancio project en base de datos.
 *   + Se instancio Roles en base de datos.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional // Revierte los cambios en la DB despues de cada test
class UserServiceIntegrationTest
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
			Project.builder().owner(owner).build()
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
			Project.builder().owner(dev).name("Solar").build()
		);

		projectRepository.save(
			Project.builder().owner(dev).name("Eólico").build()
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