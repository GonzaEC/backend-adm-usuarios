// src/main/java/com/plataforma/service/impl/WalletServiceImpl.java
package com.plataforma.service.impl;

import com.plataforma.exception.InsufficientFundsException;
import com.plataforma.exception.ResourceNotFoundException;
import com.plataforma.model.Wallet;
import com.plataforma.repository.WalletRepository;
import com.plataforma.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService
{
	private final WalletRepository walletRepository;

	@Override
	public Wallet findByUserId(Long userId)
	{
		return walletRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException(
				"Wallet no encontrado para el usuario: " + userId
			));
	}

	@Override
	public void validateSufficientFunds(Wallet wallet, BigDecimal amount)
	{
		if (wallet.getBalance().compareTo(amount) < 0)
			throw new InsufficientFundsException(
				"Saldo insuficiente: disponible %s, requerido %s"
				.formatted(wallet.getBalance(), amount)
			);
		}

	@Override
	public void debit(Wallet wallet, BigDecimal amount)
	{
		wallet.setBalance(wallet.getBalance().subtract(amount));
		walletRepository.save(wallet);
	}
}