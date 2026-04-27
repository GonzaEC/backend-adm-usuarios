// src/main/java/com/plataforma/repository/WalletRepository.java
package com.plataforma.repository;

import com.plataforma.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long>
{
	/**
	 * Busca el Wallet por el ID del usuario propietario.
	 * Dado que @MapsId hace que wallet.id == user.id,
	 * esto es equivalente a findById(userId), pero mas expresivo
	 * en el contexto del servicio.
	 */
	Optional<Wallet> findByUserId(Long userId);
}