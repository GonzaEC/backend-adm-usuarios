// src/main/java/com/plataforma/model/Role.java
package com.plataforma.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String name; // Ej: "ADMIN", "INVESTOR"

	// Agregado: descripción legible del rol para mostrarlo en el detalle
	private String description;

	// Agregado: para borrado lógico consistente con UserService.deactivateUser()
	// @Builder.Default hace que el builder use true cuando no se especifica el campo
	@Builder.Default
	private boolean active = true;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
		name = "roles_permissions",
		joinColumns = @JoinColumn(name = "role_id"),
		inverseJoinColumns = @JoinColumn(name = "permission_id")
	)
	private Set<Permission> permissions = new HashSet<>();
}