// src/main/java/com/plataforma/service/WalletService.java
package com.plataforma.service;

import java.math.BigDecimal;

import com.plataforma.model.Wallet;

/**
 * Puerto de dominio para operaciones sobre el Wallet de un inversor.
 *
 * Esta interfaz es agnostica a la infraestructura:
 * - En tests: se usa un mock o una implementacion en memoria.
 * - En produccion: la implementación concreta (WalletServiceImpl)
 *   puede delegar a una API de pagos externa (MercadoPago, blockchain, etc.)
 *   sin que el dominio sepa nada de eso.
 *
 * Regla: ningun metodo de esta interfaz importa clases de infraestructura
 * (no hay HttpClient, no hay repositorios JPA, no hay anotaciones de Spring
 * en esta interfaz — esas pertenecen a la implementacion).
 */
public interface WalletService
{
	/**
	 * Valida que el wallet tenga fondos suficientes para cubrir el monto.
	 * Lanza excepción si no los tiene.
	 *
	 * @param wallet el monedero del inversor
	 * @param amount el monto requerido (debe ser > 0)
	 * @throws com.plataforma.exception.InsufficientFundsException si el saldo es insuficiente
	 */
	void validateSufficientFunds(Wallet wallet, BigDecimal amount);

	/**
	 * Debita el monto del wallet.
	 * Precondición: validateSufficientFunds fue llamado antes en la misma transacción.
	 *
	 * @param wallet el monedero a debitar
	 * @param amount el monto a descontar
	 */
	void debit(Wallet wallet, BigDecimal amount);

	/**
	 * Busca el Wallet de un usuario por su ID.
	 * Lanza excepción si el usuario no tiene wallet (caso de datos inconsistentes).
	 *
	 * @param userId ID del usuario
	 * @return el Wallet correspondiente
	 * @throws com.plataforma.exception.ResourceNotFoundException si no existe
	 */
	Wallet findByUserId(Long userId);
}