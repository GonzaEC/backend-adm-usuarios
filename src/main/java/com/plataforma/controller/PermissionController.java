// src/main/java/com/plataforma/controller/PermissionController.java
package com.plataforma.controller;

// DTO
import com.plataforma.dto.ApiResponse;
import com.plataforma.dto.LoginRequest;

import com.plataforma.model.*;

// Service
import com.plataforma.service.PermissionService;

// Lombok
import lombok.RequiredArgsConstructor;

// Spring
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController
{
    private final PermissionService permissionService;

    // GET /api/permissions
    // Listado disponible para cualquier usuario autenticado (ej: para poblar
    // el select de "asignar permisos a rol" en el front).
    @GetMapping
    public ResponseEntity<ApiResponse<List<Permission>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(permissionService.findAll()));
    }

    // POST /api/permissions
    // Crear permiso. Requiere user:update (mismo permiso que gestiona roles).
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Permission>> create(@RequestBody Permission p) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(permissionService.create(p)));
    }

    // PUT /api/permissions/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Permission>> update(@PathVariable Long id, @RequestBody Permission p) {
        return ResponseEntity.ok(ApiResponse.success(permissionService.update(id, p)));
    }

    // DELETE /api/permissions/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}