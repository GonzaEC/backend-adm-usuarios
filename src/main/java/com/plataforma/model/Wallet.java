// src/main/java/com/plataforma/model/Wallet.java
package com.plataforma.model;

import java.math.BigDecimal;

import com.plataforma.exception.InsufficientFundsException;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Representa el monedero de un inversor dentro de la plataforma.
 *
 * Decisiones de diseño:
 * - Relación OneToOne con User: cada usuario tiene exactamente un Wallet.
 *   Se usa @MapsId para compartir el mismo PK que User, evitando una columna
 *   de FK separada. Esto garantiza que nunca exista un Wallet huérfano.
 *
 * - balance en BigDecimal: nunca usar double/float para dinero.
 *   BigDecimal evita errores de punto flotante en operaciones financieras.
 *
 * - Moneda única por ahora: si en el futuro se necesita multi-moneda,
 *   se agrega un campo `currency` (String o enum) y se ajusta la validación
 *   en WalletService sin cambiar la estructura central.
 */
@Entity
@Table(name = "wallets")
@SQLDelete(sql = "UPDATE wallets SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Wallet extends Auditable
{
	@Id
	private Long id;

	/**
	 * @MapsId: el PK de Wallet ES el PK de User.
	 * No hay una columna "user_id" separada — el "id" de wallets
	 * referencia directamente a users(id).
	 */
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId
	@JoinColumn(name = "id")
	private User user;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal balance;

	/**
	 * Constructor de fábrica: crea un Wallet nuevo con saldo cero
	 * para un usuario dado. Es el único punto de creación válido.
	 */
	public static Wallet createFor(User user)
	{
		Wallet wallet = new Wallet();
		wallet.setUser(user);
		wallet.setBalance(BigDecimal.ZERO);
		return wallet;
	}

	/**
	 * Regla de dominio: ¿tiene fondos suficientes para el monto dado?
	 * La logica vive acá y no en el service, porque es una propiedad
	 * intrinseca del Wallet saber si puede cubrir un monto.
	 */
	public boolean hasSufficientFunds(BigDecimal amount)
	{
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
			throw new InsufficientFundsException(
				"El monto a debitar debe ser mayor a cero."
			);
		return this.balance.compareTo(amount) >= 0;
	}

	/**
	 * Debita el monto del balance.
	 * Precondicion: hasSufficientFunds debe haber sido validado antes.
	 * Este metodo no valida — solo ejecuta. La validación es responsabilidad
	 * de quien llama (WalletService).
	 */
	public void debit(BigDecimal amount)
	{
		this.balance = this.balance.subtract(amount);
	}

	/**
	 * Acredita el monto al balance.
	 * Útil para tests y para futuros reembolsos.
	 */
	public void credit(BigDecimal amount)
	{
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
			throw new IllegalArgumentException(
				"El monto a acreditar debe ser mayor a cero."
			);
		this.balance = this.balance.add(amount);
	}
}