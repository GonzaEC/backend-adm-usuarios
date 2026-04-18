// src/main/java/com/plataforma/exception/RoleInUseException.java
package com.plataforma.exception;

// Se lanza cuando se intenta eliminar un rol que tiene usuarios asignados.
// Separada de UnauthorizedAccessException porque es un problema de integridad,
// no de permisos — el HTTP correcto es 409 Conflict, no 403 Forbidden.
public class RoleInUseException extends RuntimeException
{
    public RoleInUseException(String roleName)
    {
        super("No se puede eliminar el rol '" + roleName + "' porque tiene usuarios asignados.");
    }
}