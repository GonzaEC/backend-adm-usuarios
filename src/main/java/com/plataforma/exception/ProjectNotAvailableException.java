// src/main/java/com/plataforma/exception/ProjectNotAvailableException.java
package com.plataforma.exception;

/**
 * Se lanza cuando se intenta invertir en un proyecto que no esta
 * en estado PRE_OPEN ni OPEN (ej: DRAFT o CLOSED).
 */
public class ProjectNotAvailableException extends RuntimeException
{
	public ProjectNotAvailableException(String message) { super(message); }
}