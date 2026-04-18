package com.plataforma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Este controlador es SOLO para tests. 
 * Sirve para probar que las anotaciones de seguridad funcionan
 * sin ensuciarnos con lógica de negocio o repositorios de proyectos.
 */
@RestController
@RequestMapping("/api/test-security")
public class TestSecurityController {

    @PreAuthorize("hasAuthority('PROJECT_DELETE')")
    @DeleteMapping("/borrar")
    public ResponseEntity<String> borrarSimulado() {
        // Si el usuario llega hasta aquí, significa que tiene el permiso.
        return ResponseEntity.ok("Borrado exitoso");
    }
}