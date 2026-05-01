// src/main/java/com/plataforma/model/User.java
package com.plataforma.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false AND active = true")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends Auditable
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String email;

	private String password;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserProject> projects = new ArrayList<>();

	@Builder.Default
	private boolean active = true;

	public boolean hasPermission(String permissionName)
	{
		if (this.role == null || this.role.getPermissions() == null)
			return false;
		return this.role.getPermissions().stream()
			.anyMatch(p -> p.getName().equals(permissionName));
	}
}