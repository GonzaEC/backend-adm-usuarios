// src/main/java/com/plataforma/model/Role.java
package com.plataforma.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "roles")
@SQLDelete(sql = "UPDATE roles SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false AND active = true")
@Data
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends Auditable
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