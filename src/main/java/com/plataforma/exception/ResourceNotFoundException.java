// src/main/java/com/plataforma/exception/ResourceNotFoundException.java
package com.plataforma.exception;

public class ResourceNotFoundException extends RuntimeException
{
	public ResourceNotFoundException(String message) { super(message); }
}