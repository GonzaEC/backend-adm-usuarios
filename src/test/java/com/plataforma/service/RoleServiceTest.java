// src/test/java/com/plataforma/service/RoleServiceTest.java
package com.plataforma.service;

import com.plataforma.model.Role;

import com.plataforma.repository.RoleRepository;
import com.plataforma.repository.UserRepository;

import com.plataforma.exception.UnauthorizedAccessException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.plataforma.exception.DuplicateRoleException;
import com.plataforma.repository.PermissionRepository;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest
{
	@Mock
	private RoleRepository roleRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private RoleService roleService;

	@Test
	void shouldThrowExceptionWhenDeletingRoleWithAssignedUsers()
	{
		// Arrange
		Long roleId = 1L;
		Role adminRole = Role.builder().id(roleId).name("ADMIN").build();

		when(roleRepository.findById(roleId)).thenReturn(Optional.of(adminRole));
		// Simulamos que existen usuarios con este rol
		when(userRepository.existsByRole(adminRole)).thenReturn(true);

		// Act & Assert
		UnauthorizedAccessException exception = assertThrows(
			UnauthorizedAccessException.class, () -> {
				roleService.delete(roleId);
			}
		);

		assertEquals(
			"No se puede eliminar un rol que tiene usuarios asignados.",
			exception.getMessage()
		);
		verify(roleRepository, never()).delete(any());
	}

	@Test
	void shouldDeleteRoleWhenNoUsersAreAssigned()
	{
		// Arrange
		Long roleId = 2L;
		Role emptyRole = Role.builder().id(roleId).name("TEMPORAL").build();

		when(roleRepository.findById(roleId)).thenReturn(Optional.of(emptyRole));
		when(userRepository.existsByRole(emptyRole)).thenReturn(false);

		// Act
		assertDoesNotThrow(() -> roleService.delete(roleId));

		// Assert
		verify(roleRepository, times(1)).delete(emptyRole);
	}

	@Mock
	private PermissionRepository permissionRepository;

	// ─── TESTS DE CREAR ROL ───────────────────────────────────────────────────────

	@Test
	void shouldCreateRoleSuccessfully()
	{
		// El nombre no existe todavía en la DB
		when(roleRepository.findByName("ANALYST")).thenReturn(Optional.empty());
		when(roleRepository.save(any(Role.class))).thenAnswer(inv -> {
			Role r = inv.getArgument(0);
			r.setId(5L); // Simular que la DB asignó un ID al persistir
			return r;
		});

		Role created = roleService.createRole("analyst", "Analiza métricas");

		// Verificar que el nombre se normalizó a mayúsculas
		assertEquals("ANALYST", created.getName());
		assertNotNull(created.getId());
		// Verificar que se intentó persistir exactamente una vez
		verify(roleRepository, times(1)).save(any(Role.class));
	}

	@Test
	void shouldThrowWhenCreatingRoleWithDuplicateName()
	{
		// ADMIN ya existe en la DB
		when(roleRepository.findByName("ADMIN"))
			.thenReturn(Optional.of(Role.builder().id(1L).name("ADMIN").build()));

		assertThrows(DuplicateRoleException.class, () ->
			roleService.createRole("ADMIN", "duplicado")
		);

		// Si el nombre ya existe, no debe intentar guardar nada
		verify(roleRepository, never()).save(any());
	}

	// ─── TESTS DE EDITAR ROL ─────────────────────────────────────────────────────
	

	@Test
	void shouldUpdateRoleNameAndDescriptionSuccessfully()
	{
		Role existing = Role.builder()
			.id(1L).name("BASIC").description("Rol básico").build();

		when(roleRepository.findById(1L)).thenReturn(Optional.of(existing));
		// "INVESTOR" no existe todavía, no hay colisión
		when(roleRepository.findByName("INVESTOR")).thenReturn(Optional.empty());
		when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

		Role updated = roleService.updateRole(1L, "investor", "Puede invertir en proyectos");

		// Verificar normalización a mayúsculas y actualización de descripción
		assertEquals("INVESTOR", updated.getName());
		assertEquals("Puede invertir en proyectos", updated.getDescription());
		verify(roleRepository, times(1)).save(existing);
	}

	@Test
	void shouldNotCheckDuplicateWhenNameIsUnchanged()
	{
		// Editar solo la descripción sin cambiar el nombre.
		// El service no debería llamar a findByName porque el nombre no cambió.
		Role existing = Role.builder()
			.id(1L).name("ADMIN").description("descripción vieja").build();

		when(roleRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(roleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		assertDoesNotThrow(() ->
			roleService.updateRole(1L, "ADMIN", "descripción nueva")
		);

		// Como el nombre no cambió, findByName nunca debería haberse llamado
		verify(roleRepository, never()).findByName(any());
		verify(roleRepository, times(1)).save(existing);
	}

	@Test
	void shouldThrowWhenUpdatingToExistingRoleName()
	{
		// Intentar renombrar BASIC (id=1) a ADMIN cuando ADMIN (id=2) ya existe
		Role basic = Role.builder().id(1L).name("BASIC").build();
		Role admin = Role.builder().id(2L).name("ADMIN").build();

		when(roleRepository.findById(1L)).thenReturn(Optional.of(basic));
		when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(admin));

		assertThrows(DuplicateRoleException.class, () ->
			roleService.updateRole(1L, "ADMIN", "cualquier descripción")
		);

		// Si hay colisión, no debe intentar guardar
		verify(roleRepository, never()).save(any());
	}
}