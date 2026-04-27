// src/main/java/com/plataforma/model/Investement.java
package com.plataforma.model;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(name = "investments")
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
}