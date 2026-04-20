// src/main/java/com/plataforma/controller/UserController.java
package com.plataforma.controller;

import com.plataforma.dto.ApiResponse;
import com.plataforma.dto.UpdateUserRequest;
import com.plataforma.dto.UserRequest;

import com.plataforma.constant.PermissionConstants;

import com.plataforma.exception.RoleNotFoundException;
import com.plataforma.exception.UserNotFoundException;

import com.plataforma.model.Role;
import com.plataforma.model.User;

import com.plataforma.service.AccessControlService;
import com.plataforma.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController
{
	private final AccessControlService accessControlService;
    private final UserService userService;
/*
    UserController(AccessControlService accessControlService)
	{
        this.accessControlService = accessControlService;
    }
*/
	// POST /api/users
	// Alta manual. Se asigna el rol BASIC por defecto en UserService.
	// No requiere permiso porque cubre el caso de registro público;
	// si el proyecto quiere restringirlo, agregar @PreAuthorize("hasAuthority('user:update')").
	@PostMapping
	public ResponseEntity<ApiResponse<User>> create(@RequestBody UserRequest request)
	{
		User toCreate = User.builder()
			.email(request.getEmail())
			.password(request.getPassword())
			.build();

		User created = userService.registerUser(toCreate);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.success("Usuario creado", created));
	}

	// GET /api/users?page=0&size=10
	// Listado paginado. Spring inyecta Pageable desde los query params.
	@GetMapping
	@PreAuthorize("hasAuthority('user:read')")
	public ResponseEntity<ApiResponse<Page<User>>> getAll(Pageable pageable)
	{
		return ResponseEntity.ok(
			ApiResponse.success("Usuarios obtenidos", userService.getAllUsers(pageable))
		);
	}

	// GET /api/users/{id}
	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('user:read')")
	public ResponseEntity<ApiResponse<User>> getById(@PathVariable Long id)
	{
		User user = userService.getUserById(id)
			.orElseThrow(() -> new UserNotFoundException(id));
		return ResponseEntity.ok(ApiResponse.success("Usuario encontrado", user));
	}

    // PUT /api/users/{id}
    // Edición de datos del usuario (por ahora solo email).
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<ApiResponse<User>> update(
        @PathVariable Long id,
        @RequestBody UpdateUserRequest request
    )
    {
        User details = User.builder().email(request.getEmail()).build();
        User updated = userService.updateUser(id, details);
        return ResponseEntity.ok(ApiResponse.success("Usuario actualizado", updated));
    }

    // DELETE /api/users/{id}
    // Baja lógica (soft delete: active = false).
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id)
    {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("Usuario desactivado", null));
    }

    // PUT /api/users/{id}/activate
    // Reactiva un usuario previamente desactivado.
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id)
    {
        userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("Usuario activado", null));
    }

	// PUT /api/users/{id}/role
	// Asignación / revocación de rol. Revocar = asignar otro rol (ej: BASIC).
	// El body es { "roleId": <id> }.
	@PutMapping("/{id}/role")
	@PreAuthorize("hasAuthority('user:update')")
	public ResponseEntity<ApiResponse<User>> assignRole(
		@PathVariable Long id,
		@RequestBody Map<String, Long> body,
		Authentication authentication
	)
	{
		Long roleId  = body.get("roleId");
		User actor   = (User) authentication.getPrincipal();

		User target = userService.getUserById(id)
			.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		Role role = userService.getRoleById(roleId)
			.orElseThrow(() -> new RoleNotFoundException(
				"Rol no encontrado: " + roleId
			));
		
		accessControlService.validateChangeRole(actor, target, role);
		User updated = userService.assignRole(target, role);
		return ResponseEntity.ok(ApiResponse.success("Rol asignado", updated));
	}
}
