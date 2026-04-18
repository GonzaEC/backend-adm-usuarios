// src/main/java/com/plataforma/model/Permission.java
package com.plataforma.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String name; // Ej: "PROJECT_CREATE", "INVESTMENT_READ"

	@Column(nullable = false)
	private String description;
}