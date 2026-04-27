// src/main/java/com/plataforma/service/InvestmentService.java
package com.plataforma.service;

import com.plataforma.model.Investment;
import com.plataforma.model.Project;
import com.plataforma.model.User;

/**
 * Puerto de dominio para la operación de inversión.
 *
 * Orquesta en 1 transaccion atomica:
 *   1. Validar que Proyecto acepta inversiones (PRE_OPEN o OPEN)
 *   2. Validar que Usuario tiene permiso INVEST_CREATE
 *   3. Validar que Wallet tiene fondos suficientes
 *   4. Debitar el Wallet
 *   5. Crear o actualizar el UserProject (membresia)
 *   6. Registrar el Investment (comprobante historico)
 *
 * Si cualquier paso falla, el @Transactional en la implementacion
 * hace rollback de todo — el inversor nunca pierde plata sin recibir tokens.
 */
public interface InvestmentService
{
	/**
	 * Ejecuta la inversion de un usuario en un proyecto.
	 *
	 * @param investor      el usuario que invierte (debe tener rol INVESTOR)
	 * @param project       el proyecto destino (debe estar en PRE_OPEN u OPEN)
	 * @param tokensToBuy   cantidad de tokens a adquirir (debe ser > 0)
	 * @return              el Investment registrado como comprobante
	 *
	 * @throws com.plataforma.exception.InsufficientPermissionsException si usuario no puede invertir
	 * @throws com.plataforma.exception.ProjectNotAvailableException     si proyecto no acepta inversiones
	 * @throws com.plataforma.exception.InsufficientFundsException       si wallet no tiene saldo
	 */
	Investment invest(User investor, Project project, Long tokensToBuy);
}