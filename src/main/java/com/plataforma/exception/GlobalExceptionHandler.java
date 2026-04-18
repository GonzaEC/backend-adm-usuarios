// src/main/java/com/plataforma/exception/GlobalExceptionHandler.java
package com.plataforma.exception;

import com.plataforma.dto.ApiResponse;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler({
			InsufficientPermissionsException.class,
			OwnershipException.class,
			UnauthorizedAccessException.class
	})
	public ResponseEntity<ApiResponse<Void>> handleForbiddenActions(
			RuntimeException ex) {
		return createResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler({
			UserNotFoundException.class,
			RoleNotFoundException.class
	})
	public ResponseEntity<ApiResponse<Void>> handleNotFound(Exception ex) {
		return createResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
		return createResponse(
				"Ocurrio un error interno en el servidor",
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private ResponseEntity<ApiResponse<Void>> createResponse(
			String message, HttpStatus status) {
		ApiResponse<Void> res = ApiResponse.error(message, status.value());
		return new ResponseEntity<>(res, status);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
		return createResponse(
				"No se puede eliminar el recurso porque está siendo utilizado por otros registros.",
				HttpStatus.CONFLICT);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {

		// Usamos tu método estático y le pasamos el código 403
		ApiResponse<?> response = ApiResponse.error(
				"Acceso denegado: No tienes los permisos necesarios para realizar esta acción.",
				HttpStatus.FORBIDDEN.value());

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
	}

	// 409 Conflict: nombre de rol duplicado o rol en uso.
	// Es distinto al 403 porque no es un problema de permisos del actor,
	// sino un conflicto de integridad de datos en el sistema.
	@ExceptionHandler({DuplicateRoleException.class, RoleInUseException.class})
	public ResponseEntity<ApiResponse<Void>> handleRoleConflict(RuntimeException ex)
	{
		return createResponse(ex.getMessage(), HttpStatus.CONFLICT);
	}
}
