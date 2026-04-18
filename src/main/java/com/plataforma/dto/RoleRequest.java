// src/main/java/com/plataforma/dto/RoleRequest.java
package com.plataforma.dto;

import lombok.Data;

// DTO de entrada para crear y editar roles.
// Separamos lo que entra por HTTP de la entidad Role para no exponer
// campos internos como 'active' o 'permissions' en la creación.
@Data
public class RoleRequest
{
    private String name;
    private String description;
}