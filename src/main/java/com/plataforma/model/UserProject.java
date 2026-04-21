// src/main/java/com/plataforma/model/UserProject.java

package com.plataforma.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
	name = "user_projects",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"user_id", "project_id"})
	}
)
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProject extends Auditable
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id")
	private Project project;

	@Column(name = "tokens_amount", nullable = false)
	private Long tokensAmount;
}