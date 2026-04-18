// src/main/java/com/plataforma/service/PermissionService.java

package com.plataforma.service;

import com.plataforma.model.Permission;

import com.plataforma.repository.PermissionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// src/main/java/com/plataforma/service/PermissionService.java
@Service
@RequiredArgsConstructor
public class PermissionService
{
	private final PermissionRepository permissionRepository;

	public List<Permission> findAll()
	{
		return permissionRepository.findAll();
	}

	public Permission create(Permission permission)
	{
		return permissionRepository.save(permission);
	}

	public Permission update(Long id, Permission details)
	{
		Permission permission = permissionRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("Permiso no encontrado"));

		permission.setName(details.getName());
		permission.setDescription(details.getDescription());
		return permissionRepository.save(permission);
	}

	@Transactional
	public void delete(Long id)
	{
		Permission permission = permissionRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("Permiso no encontrado"));

		// Regla de negocio: ¿Se puede borrar si está en uso? 
		// Por ahora, JPA lanzará DataIntegrityViolationException si hay Roles asociados.
		permissionRepository.delete(permission);
	}
}