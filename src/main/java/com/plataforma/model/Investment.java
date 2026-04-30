// src/main/java/com/plataforma/model/Investement.java
package com.plataforma.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Setter;

@Entity
@Table(name = "investments")
@Data
@Setter
public class Investment extends Auditable
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	private User user;

	@ManyToOne(optional = false)
	private Project project;

	private Long tokensPurchased;

	private BigDecimal amountPaid; // dinero real
	private BigDecimal tokenPrice; // cuanto costaba el token en ese momento.

	public void set(
		User       investor,
		Project    project,
		Long       tokensPurchased,
		BigDecimal amountPaid,
		BigDecimal tokenPrice
	)
	{
		this.setUser(investor);
		this.setProject(project);
		this.setTokensPurchased(tokensPurchased);
		this.setAmountPaid(amountPaid);
		this.setTokenPrice(tokenPrice);
	}
}