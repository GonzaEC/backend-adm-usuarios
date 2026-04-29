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
public class GlobalExceptionHandler
{
	// ── Helper ────────────────────────────────────────────────────────────────
	private ResponseEntity<ApiResponse<Void>> response(String message, HttpStatus status)
	{
		return new ResponseEntity<>(ApiResponse.error(message, status.value()), status);
	}

	// ── 403 Forbidden ─────────────────────────────────────────────────────────
	@ExceptionHandler({
			InsufficientPermissionsException.class,
			OwnershipException.class,
			UnauthorizedAccessException.class
	})
	public ResponseEntity<ApiResponse<Void>> handleForbiddenActions(RuntimeException ex)
	{
		return response(ex.getMessage(), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex)
	{
		return ResponseEntity
			.status(HttpStatus.FORBIDDEN)
			.body(ApiResponse.error(
				"Acceso denegado: No tienes los permisos necesarios para realizar esta acción.",
				HttpStatus.FORBIDDEN.value()
			));
	}

	// ── 404 Not Found ─────────────────────────────────────────────────────────
	@ExceptionHandler({
			UserNotFoundException.class,
			RoleNotFoundException.class,
			ProjectNotFoundException.class
	})
	public ResponseEntity<ApiResponse<Void>> handleNotFound(Exception ex) {
		return response(ex.getMessage(), HttpStatus.NOT_FOUND);
	}

	// ── 409 Conflict ──────────────────────────────────────────────────────────
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex)
	{
		return response(
			"No se puede eliminar el recurso porque está siendo utilizado por otros registros.",
			HttpStatus.CONFLICT
		);
	}

	@ExceptionHandler({DuplicateRoleException.class, RoleInUseException.class})
	public ResponseEntity<ApiResponse<Void>> handleRoleConflict(RuntimeException ex)
	{
		return response(ex.getMessage(), HttpStatus.CONFLICT);
	}

	// Estado del proyecto impide la operacion (DRAFT o CLOSED al intentar invertir)
	@ExceptionHandler(ProjectNotAvailableException.class)
	public ResponseEntity<ApiResponse<Void>> handleProjectNotAvailable(ProjectNotAvailableException ex)
	{
		return response(ex.getMessage(), HttpStatus.CONFLICT);
	}

	// ── 422 Unprocessable Entity ──────────────────────────────────────────────
	// Request válido, pero no procesable por falta de fondos
	@ExceptionHandler(InsufficientFundsException.class)
	public ResponseEntity<ApiResponse<Void>> handleInsufficientFunds(InsufficientFundsException ex)
	{
		return response(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
	}

	// ── 500 Fallback ──────────────────────────────────────────────────────────
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex)
	{
		return response("Ocurrio un error interno en el servidor", HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
