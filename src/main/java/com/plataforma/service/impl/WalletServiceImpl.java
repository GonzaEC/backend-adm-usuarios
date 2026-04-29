// src/main/java/com/plataforma/service/impl/WalletServiceImpl.java
package com.plataforma.service.impl;

import com.plataforma.exception.InsufficientFundsException;
import com.plataforma.exception.ResourceNotFoundException;
import com.plataforma.model.Wallet;
import com.plataforma.repository.WalletRepository;
import com.plataforma.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
		if (!wallet.hasSufficientFunds(amount))
			throw new InsufficientFundsException(
				"Saldo insuficiente. Disponible: " + wallet.getBalance()
				+ " — Requerido: " + amount
			);
	}

	@Override
	@Transactional
	public void debit(Wallet wallet, BigDecimal amount)
	{
		wallet.debit(amount);
		walletRepository.save(wallet);
	}
}