// src/test/java/com/plataforma/service/RolePermissionIntegrationTest.java
package com.plataforma.service;

import com.plataforma.model.Permission;
import com.plataforma.model.Role;

import com.plataforma.repository.PermissionRepository;
import com.plataforma.repository.RoleRepository;
import com.plataforma.AbstractIntegrationTest;
import com.plataforma.exception.RoleNotFoundException;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

//@Transactional // Limpia la DB después de cada test
@Tag("integration")
class RolePermissionIntegrationTest extends AbstractIntegrationTest
{

	@Autowired
	private RoleService roleService;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PermissionRepository permissionRepository;

	@Test
	void shouldUpdateRolePermissionsSuccessfully()
	{
		// Crear un rol y un par de permisos en la DB
		Role role = roleRepository.save(Role.builder().name("TEST_ROLE").build());

		Permission p1 = permissionRepository.save(
			Permission.builder()
				.name("test:run")
				.description("desc")
				.build()
		);

		Permission p2 = permissionRepository.save(
			Permission.builder()
				.name("test:stop")
				.description("desc")
				.build()
		);

		Set<Long> permissions = Set.of(p1.getId(), p2.getId());

		// Asignar los permisos al rol
		Role updatedRole = roleService.updatePermissions(
			role.getId(), permissions
		);

		// Verificar que se guardaron correctamente
		assertEquals(2, updatedRole.getPermissions().size());
		
		assertTrue(updatedRole.getPermissions().stream()
			.anyMatch(p -> p.getName().equals("test:run")));
	}

	@Test
	void shouldThrowExceptionWhenRoleDoesNotExist()
	{
		// Usar un ID que no existe
		Long nonExistentId = 999L;
		Set<Long> pIds = Set.of(1L);

		// Debe lanzar RoleNotFoundException
		assertThrows(RoleNotFoundException.class, () -> {
			roleService.updatePermissions(nonExistentId, pIds);
		});
	}
}