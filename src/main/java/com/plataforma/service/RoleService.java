// src/main/java/com/plataforma/service/RoleService.java
package com.plataforma.service;
import com.plataforma.exception.DuplicateRoleException;
import com.plataforma.exception.RoleNotFoundException;
import com.plataforma.exception.UnauthorizedAccessException;

import com.plataforma.model.Role;
import com.plataforma.model.Permission;

import com.plataforma.repository.UserRepository;
import com.plataforma.repository.PermissionRepository;
import com.plataforma.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleService
{
	private final RoleRepository       roleRepository;
	private final UserRepository       userRepository;
	private final PermissionRepository permissionRepository;

	public List<Role> findAll() { return roleRepository.findAll(); }

	public Role findById(Long id)
	{
		return roleRepository.findById(id)
			.orElseThrow(() -> new RoleNotFoundException("Rol no encontrado"));
	}

	// ─── ALTA ─────────────────────────────────────────────────────────────────────
	// Antes de crear verificamos que no exista otro rol con el mismo nombre.
	// El nombre se normaliza a mayúsculas para mantener consistencia con RoleConstants.
	@Transactional
	public Role createRole(String name, String description)
	{
		String normalizedName = name.toUpperCase().trim();

		// Si ya existe un rol con ese nombre, lanzamos excepción antes de persistir
		if (roleRepository.findByName(normalizedName).isPresent())
			throw new DuplicateRoleException(normalizedName);

		Role role = Role.builder()
			.name(normalizedName)
			.description(description)
			.build(); // permissions y active ya tienen defaults por @Builder.Default

		return roleRepository.save(role);
	}

	// ─── EDICIÓN ──────────────────────────────────────────────────────────────────
	// Solo se permite cambiar nombre y descripción.
	// Los permisos se gestionan por separado a través de updatePermissions().
	@Transactional
	public Role updateRole(Long id, String newName, String newDescription)
	{
		Role role = this.findById(id); // lanza RoleNotFoundException si no existe

		String normalizedName = newName.toUpperCase().trim();

		// Solo verificamos colisión si el nombre efectivamente cambió.
		// Si editamos solo la descripción manteniendo el mismo nombre,
		// no tiene sentido buscar duplicados.
		boolean nameChanged = !role.getName().equals(normalizedName);
		if (nameChanged && roleRepository.findByName(normalizedName).isPresent())
			throw new DuplicateRoleException(normalizedName);

		role.setName(normalizedName);
		role.setDescription(newDescription);

		return roleRepository.save(role);
	}

	@Transactional
	public Role save(Role role) { return roleRepository.save(role); }

	@Transactional
	public void delete(Long id)
	{
		Role role = this.findById(id);

		// Validacion de Integridad
		if (userRepository.existsByRole(role))
			throw new UnauthorizedAccessException(
				"No se puede eliminar un rol que tiene usuarios asignados."
			);

		roleRepository.delete(role);
	}

	@Transactional
	public Role updatePermissions(Long roleId, Set<Long> permissionIds)
	{
		Role role = findById(roleId);
		List<Permission> permissions = permissionRepository.findAllById(permissionIds);
		role.setPermissions(new HashSet<>(permissions));
		return roleRepository.save(role);
	}
}
