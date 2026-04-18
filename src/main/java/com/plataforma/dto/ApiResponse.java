// src/main/java/com/plataforma/dto/ApiResponse.java
package com.plataforma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data // Ya incluye Getter y Setter
@Builder
@NoArgsConstructor // VITAL para que Jackson (JSON) pueda procesarlo
@AllArgsConstructor // VITAL para que el @Builder funcione correctamente
public class ApiResponse<T> {
	private String message;
	private T data;
	private int status;
	private LocalDateTime timestamp;

	/**
	 * Genera una respuesta HTTP 200 para el cliente.
	 * 
	 * @param <T>
	 * @param msg
	 * @param data
	 * @return
	 */
	public static <T> ApiResponse<T> success(String msg, T data) {
		return ApiResponse.<T>builder()
				.message(msg)
				.timestamp(LocalDateTime.now())
				.data(data)
				.status(200)
				.build();
	}

	public static <T> ApiResponse<T> success(T data) {
		return success("Operación exitosa", data);
	}

	public static <T> ApiResponse<T> error(String msg, int statusCode) {
		return ApiResponse.<T>builder()
				.message(msg)
				.timestamp(LocalDateTime.now())
				.data(null)
				.status(statusCode)
				.build();
	}
}
