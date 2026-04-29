// src/main/java/com/plataforma/model/Project.java
package com.plataforma.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

/**
 * ¿Por que usar un User en vez de un long int para el owner?
 * Navegabilidad
 * Con un Long, si se tiene un Proyecto y se quiere saber el nombre del dueño para mostrarlo en frontend,
 *  hay que ir al servicio, llamar al repositorio y buscar al usuario por ese ID.
 *  Con @ManyToOne, se hace: proyecto.getOwner().getName();
 *  JPA se encarga de traer esa info. de forma automática (Lazy o Eager loading).

 *  Integridad de Datos y "Orphans"
 *  Si se usa un Long, la base de datos es solo una colección de números.
 *  Con una relación formal, se puede definir si se borra un Usuario, se borren todos sus proyectos de forma automatica (o se cancelen).
 *    Hibernate valida que el objeto User que se esta asignando realmente sea una entidad valida antes de intentar persistir.

 *  En el mundo de los objetos, el Project no conoce un numero; el proyecto pertenece a alguien.
 *  Al modelarlo como una relación:
 *   Se puede responder preguntas complejas fácilmente:
 *     "Traer todos los proyectos de este usuario cuyo rol sea DEVELOPER".
 *   Se pude ir a la clase User y agregar una List<Project>.
 *   Asi, desde el usuario se puede ver toda su cartera de proyectos sin hacer una consulta manual al repositorio de proyectos.

 *  Consultas más potentes (JPQL / Criteria)
 *  Cuando se escribe consultas personalizadas, se puede usar Joins de forma natural:
 *     Conceptual: SELECT p FROM Project p WHERE p.owner.email = :email
 *  Si se usa el Long, se debe hacer el Join manualmente comparando IDs, lo cual es mas propenso a errores de sintaxis y menos legible.
 */
@Entity
@Table(name="projects")
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Project extends Auditable
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id")
	private User owner;

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserProject> participants = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private ProjectState state = ProjectState.DRAFT;

	private Long maxAmountTokens;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal tokenPrice; // Representa el precio unitario del token al momento de publicar el proyecto.
	// Se usa en InvestmentServiceImpl para calcular el totalAmount.


	public boolean isOwnedBy(User user)
	{
		return this.owner != null && this.owner.equals(user);
	}

	public void advanceState()
	{
		switch (this.state)
		{
			case DRAFT    -> this.state = ProjectState.PRE_OPEN;
			case PRE_OPEN -> this.state = ProjectState.OPEN;
			case OPEN     -> this.state = ProjectState.CLOSED;
			case CLOSED   -> throw new IllegalStateException(
				"El proyecto ya está finalizado"
			);
		}
	}

	public void changeState(ProjectState newState)
	{
		if (newState.ordinal() < this.state.ordinal())
			throw new IllegalStateException(
				"No se puede retroceder de estado"
			);
		this.state = newState;
	}

	public boolean canReceiveInvestments()
	{
		return this.state == ProjectState.PRE_OPEN
			|| this.state == ProjectState.OPEN;
	}
}
