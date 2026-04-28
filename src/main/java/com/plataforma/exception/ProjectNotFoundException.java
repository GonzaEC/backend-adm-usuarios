// src/main/java/com/plataforma/exception/ProjectNotFoundException.java
package com.plataforma.exception;

/**
 * Se lanza cuando se busca un proyecto por ID y no existe en la base de datos.
 */
public class ProjectNotFoundException extends RuntimeException
{
    public ProjectNotFoundException(String message)
    {
        super(message);
    }
}