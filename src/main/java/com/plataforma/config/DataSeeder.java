// src/main/java/com/plataforma/config/DataSeeder.java
package com.plataforma.config;

import com.plataforma.constant.RoleConstants;
import com.plataforma.constant.PermissionConstants;

import com.plataforma.model.Permission;
import com.plataforma.model.Role;
import com.plataforma.model.User;

import com.plataforma.repository.PermissionRepository;
import com.plataforma.repository.RoleRepository;
import com.plataforma.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Set;

/**
 * En lugar de crear los roles manualmente en cada registro,
 *  se usa un componente que se ejecuta una sola vez al iniciar la app.
 *  Este asegura que los permisos y roles existan en la DB.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner
{
	private final RoleRepository       roleRepository;
	private final PermissionRepository permissionRepository;
	private final UserRepository       userRepository;
	private final PasswordEncoder      passwordEncoder;

	// Si es true (por defecto), siembra un usuario admin para poder probar
	// la API desde Postman. Se desactiva en tests para no afectar conteos.
	@Value("${app.seed-admin:true}")
	private boolean seedAdmin;

	@Override
	public void run(String... args)
	{
		// Crear Permisos (si no existen)
		Permission readProject = getOrCreatePermission(
			PermissionConstants.PROJECT_READ
		);
		Permission createProject = getOrCreatePermission(
			PermissionConstants.PROJECT_CREATE
		);
		Permission modifyProject = getOrCreatePermission(
			PermissionConstants.PROJECT_UPDATE
		);
		Permission deleteProject = getOrCreatePermission(
			PermissionConstants.PROJECT_DELETE
		);
		Permission investProject = getOrCreatePermission(
			PermissionConstants.INVEST_CREATE
		);
		Permission readUser = getOrCreatePermission(
			PermissionConstants.USER_READ
		);
		Permission updateUser = getOrCreatePermission(
			PermissionConstants.USER_UPDATE
		);
		Permission deleteUser = getOrCreatePermission(
			PermissionConstants.USER_DELETE
		);
		// Crear Roles y asignar permisos
		createRoleIfNotFound(
			RoleConstants.BASIC, Set.of(readProject)
		);
		createRoleIfNotFound(
			RoleConstants.INVESTOR, Set.of(readProject, investProject)
		);
		createRoleIfNotFound(
			RoleConstants.DEVELOPER, Set.of(
				readProject, createProject, modifyProject
			)
		);
		createRoleIfNotFound(
			RoleConstants.ADMIN,
			Set.of(
				readProject,
				createProject,
				investProject,
				deleteProject,
				modifyProject,
				readUser,
				updateUser,
				deleteUser
			)
		);

		// Usuario admin por defecto para poder probar la API desde Postman.
		// Credenciales: admin@admin.com / admin123
		if (seedAdmin) createAdminIfNotFound();
	}

	private void createAdminIfNotFound()
	{
		String adminEmail = "admin@admin.com";
		if (userRepository.findByEmail(adminEmail).isPresent()) return;

		Role adminRole = roleRepository.findByName(RoleConstants.ADMIN)
			.orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));

		User admin = User.builder()
			.email(adminEmail)
			.password(passwordEncoder.encode("admin123"))
			.role(adminRole)
			.active(true)
			.build();

		userRepository.save(admin);
	}

	private Permission getOrCreatePermission(String name)
	{
		return permissionRepository.findByName(name)
			.orElseGet(() -> permissionRepository.save(
				Permission.builder()
					.name(name)
					.description("Permiso para " + name)
					.build())
			);
	}

	private void createRoleIfNotFound(String name, Set<Permission> permissions)
	{
		if (roleRepository.findByName(name).isEmpty())
			roleRepository.save(Role.builder()
				.name(name)
				.permissions(permissions)
				.build());
	}
}