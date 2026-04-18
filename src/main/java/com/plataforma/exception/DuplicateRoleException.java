// src/main/java/com/plataforma/exception/DuplicateRoleException.java
package com.plataforma.exception;

// Se lanza cuando se intenta crear o renombrar un rol con un nombre que ya existe.
public class DuplicateRoleException extends RuntimeException
{
    public DuplicateRoleException(String name)
    {
        super("Ya existe un rol con el nombre '" + name + "'.");
    }
}