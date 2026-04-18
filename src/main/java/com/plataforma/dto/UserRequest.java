// src/main/java/com/plataforma/dto/UserRequest.java
package com.plataforma.dto;

import lombok.Data;

// DTO de entrada para crear usuarios.
// No incluimos 'active' ni 'role': el alta siempre se crea activa y
// con el rol por defecto (BASIC) asignado en UserService.registerUser.
@Data
public class UserRequest
{
    private String email;
    private String password;
}
