// src/main/java/com/plataforma/controller/RoleController.java
package com.plataforma.controller;

import com.plataforma.dto.ApiResponse;
import com.plataforma.dto.RoleRequest;
import com.plataforma.model.Role;
import com.plataforma.service.RoleService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController
{
    private final RoleService roleService;

    // GET /api/roles
    // Cualquier usuario autenticado puede ver los roles disponibles.
    // Los permisos de cada rol vienen incluidos porque Role usa FetchType.EAGER.
    @GetMapping
    public ResponseEntity<ApiResponse<List<Role>>> getAll()
    {
        return ResponseEntity.ok(
            ApiResponse.success("Roles obtenidos", roleService.findAll())
        );
    }

    // GET /api/roles/{id}
    // Detalle de un rol con sus permisos.
    // Como permissions es EAGER, al traer el Role ya vienen los permisos — sin query extra.
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> getById(@PathVariable Long id)
    {
        return ResponseEntity.ok(
            ApiResponse.success("Rol encontrado", roleService.findById(id))
        );
    }

    // POST /api/roles
    // Solo ADMIN puede crear roles.
    // @PreAuthorize funciona porque SecurityConfig tiene @EnableMethodSecurity.
    // Chequea el permiso "user:update" que el ADMIN tiene asignado en DataSeeder.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Role>> create(@RequestBody RoleRequest request)
    {
        Role created = roleService.createRole(
            request.getName(), request.getDescription()
        );
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Rol creado", created));
    }

    // PUT /api/roles/{id}
    // Editar nombre y/o descripción. Solo ADMIN.
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Role>> update(
        @PathVariable Long id,
        @RequestBody RoleRequest request
    )
    {
        Role updated = roleService.updateRole(
            id, request.getName(), request.getDescription()
        );
        return ResponseEntity.ok(ApiResponse.success("Rol actualizado", updated));
    }

    // DELETE /api/roles/{id}
    // Baja del rol. Falla con 409 si tiene usuarios asignados.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id)
    {
        roleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Rol eliminado", null));
    }

    // PUT /api/roles/{id}/permissions
    // Reemplaza el set completo de permisos del rol.
    // Recibe un Set<Long> con los IDs de los permisos que debe tener el rol.
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Role>> updatePermissions(
        @PathVariable Long id,
        @RequestBody Set<Long> permissionIds
    )
    {
        Role updated = roleService.updatePermissions(id, permissionIds);
        return ResponseEntity.ok(ApiResponse.success("Permisos actualizados", updated));
    }
}