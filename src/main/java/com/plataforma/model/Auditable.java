package com.plataforma.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
/**
 * Clase base reutilizable que agrega las columnas auditables como:
 * + created_at
 * + updated_at
 */
@MappedSuperclass
public abstract class Auditable
{
	@Column(name = "created_at", nullable = false, updatable = false)
	protected LocalDateTime createdAt;

	@Column(name = "updated_at")
	protected LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate()
	{
		this.createdAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate()
	{
		this.updatedAt = LocalDateTime.now();
	}
}
