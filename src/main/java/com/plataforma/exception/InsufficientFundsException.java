// src/main/java/com/plataforma/exception/InsufficientFundsException.java
package com.plataforma.exception;

public class InsufficientFundsException extends RuntimeException
{
	public InsufficientFundsException(String message) { super(message); }
}