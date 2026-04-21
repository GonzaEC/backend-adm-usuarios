package com.plataforma.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User
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

	@Builder.Default
	private boolean active = true;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updateAt;

	@PrePersist
	protected void onCreate()
	{
		this.createdAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate()
	{
		this.updateAt = LocalDateTime.now();
	}

	public boolean hasPermission(String permissionName)
	{
		if (this.role == null || this.role.getPermissions() == null)
			return false;
		return this.role.getPermissions().stream()
			.anyMatch(p -> p.getName().equals(permissionName));
	}
}