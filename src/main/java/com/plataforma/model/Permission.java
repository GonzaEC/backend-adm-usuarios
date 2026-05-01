// src/main/java/com/plataforma/model/Permission.java
package com.plataforma.model;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "permissions")
@SQLDelete(sql = "UPDATE permissions SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends Auditable
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String name; // Ej: "PROJECT_CREATE", "INVESTMENT_READ"

	@Column(nullable = false)
	private String description;
}