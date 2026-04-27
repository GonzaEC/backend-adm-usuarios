// src/main/java/com/plataforma/service/impl/InvestmentServiceImpl.java
package com.plataforma.service.impl;

import com.plataforma.exception.ProjectNotAvailableException;
import com.plataforma.model.*;
import com.plataforma.repository.InvestmentRepository;
import com.plataforma.repository.UserProjectRepository;
import com.plataforma.service.AccessControlService;
import com.plataforma.service.InvestmentService;
import com.plataforma.service.WalletService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvestmentServiceImpl implements InvestmentService
{
	private final AccessControlService accessControlService;
	private final WalletService        walletService;
	private final InvestmentRepository investmentRepository;
	private final UserProjectRepository userProjectRepository;

	/**
	 * Ejecuta la inversión de forma atómica.
	 *
	 * Orden de validaciones (fail-fast: la más barata primero):
	 *   1. Permiso del usuario     — solo consulta el objeto en memoria
	 *   2. Estado del proyecto     — solo consulta el objeto en memoria
	 *   3. Fondos del wallet       — consulta DB pero no escribe
	 *   4. Débito + persistencia   — escribe en DB
	 */
	@Override
	@Transactional
	public Investment invest(User investor, Project project, Long tokensToBuy)
	{
		// ¿Tiene permiso INVEST_CREATE?
		accessControlService.validateInvest(investor);

		// ¿El proyecto acepta inversiones?
		if (!project.canReceiveInvestments())
			throw new ProjectNotAvailableException(
				"El proyecto '%s' no acepta inversiones en su estado actual: %s"
				.formatted(project.getName(), project.getState())
			);

		// ¿Hay fondos suficientes en Wallet?
		Wallet wallet = walletService.findByUserId(investor.getId());

		BigDecimal tokenPrice  = project.getTokenPrice();
		BigDecimal totalAmount = tokenPrice.multiply(BigDecimal.valueOf(tokensToBuy));

		walletService.validateSufficientFunds(wallet, totalAmount);

		// Debitar Wallet
		walletService.debit(wallet, totalAmount);

		// Crear o Actualizar UserProject (membresia)
		UserProject membership = findOrCreateMembership(investor, project);
		membership.setTokensAmount(membership.getTokensAmount() + tokensToBuy);
		userProjectRepository.save(membership);

		// Registrar Investment (comprobante historico)
		Investment investment = new Investment();
		investment.setUser(investor);
		investment.setProject(project);
		investment.setTokensPurchased(tokensToBuy);
		investment.setAmountPaid(totalAmount);
		investment.setTokenPrice(tokenPrice);

		return investmentRepository.save(investment);
	}

	private UserProject findOrCreateMembership(User investor, Project project)
	{
		Optional<UserProject> existing =
			userProjectRepository.findByUserAndProject(investor, project);

		return existing.orElseGet(() ->
			UserProject.builder()
				.user(investor)
				.project(project)
				.tokensAmount(0L)
				.build()
		);
	}
}