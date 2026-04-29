// src/test/java/com/plataforma/model/ProjectDomainTest.java
package com.plataforma.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests de dominio puro.
 * Sin Spring, sin base de datos, sin mocks de infraestructura.
 * Solo instancias Java y reglas de negocio.
 *
 * Si estos tests fallan, el problema está en el modelo de dominio,
 * no en la infraestructura.
 */
@DisplayName("Dominio: Project")
@Tag("unit")
class ProjectDomainTest
{
	// =========================================================
	// Helpers de construccion — evitan repeticion en cada test
	// =========================================================

	private Project projectInState(ProjectState state)
	{
		return Project.builder()
			.id(1L)
			.name("Parque Solar Mendoza")
			.tokenPrice(BigDecimal.valueOf(100))
			.state(state)
			.build();
	}

	@Nested
	@DisplayName("constructor()")
	class Constructor
	{
		@Test
		@DisplayName("El proyecto debe iniciarse como 'Borrador'")
		void projectShouldStartInDraft()
		{
			Project project = Project.builder().build();

			assertEquals(ProjectState.DRAFT, project.getState());
		}
	}

	@Nested
	@DisplayName("canReceiveInvestments()")
	class CanReceiveInvestments
	{
		@Test
		@DisplayName("PRE_OPEN acepta inversiones")
		void preOpenAcceptsInvestments()
		{
			Project project = projectInState(ProjectState.PRE_OPEN);
			assertThat(project.canReceiveInvestments()).isTrue();
		}

		@Test
		@DisplayName("OPEN acepta inversiones")
		void openAcceptsInvestments()
		{
			Project project = projectInState(ProjectState.OPEN);
			assertThat(project.canReceiveInvestments()).isTrue();
		}

		@Test
		@DisplayName("DRAFT no acepta inversiones")
		void draftRejectsInvestments()
		{
			Project project = projectInState(ProjectState.DRAFT);
			assertThat(project.canReceiveInvestments()).isFalse();
		}

		@Test
		@DisplayName("CLOSED no acepta inversiones")
		void closedRejectsInvestments()
		{
			Project project = projectInState(ProjectState.CLOSED);
			assertThat(project.canReceiveInvestments()).isFalse();
		}
	}

	@Nested
	@DisplayName("advanceState()")
	class AdvanceState
	{
		@Test
		@DisplayName("DRAFT → PRE_OPEN")
		void draftAdvancesToPreOpen()
		{
			Project project = projectInState(ProjectState.DRAFT);
			project.advanceState();
			assertThat(project.getState()).isEqualTo(ProjectState.PRE_OPEN);
		}

		@Test
		@DisplayName("PRE_OPEN → OPEN")
		void preOpenAdvancesToOpen()
		{
			Project project = projectInState(ProjectState.PRE_OPEN);
			project.advanceState();
			assertThat(project.getState()).isEqualTo(ProjectState.OPEN);
		}

		@Test
		@DisplayName("OPEN → CLOSED")
		void openAdvancesToClosed()
		{
			Project project = projectInState(ProjectState.OPEN);
			project.advanceState();
			assertThat(project.getState()).isEqualTo(ProjectState.CLOSED);
		}

		@Test
		@DisplayName("CLOSED lanza excepción al avanzar")
		void closedCannotAdvance()
		{
			Project project = projectInState(ProjectState.CLOSED);
			assertThatThrownBy(project::advanceState)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("finalizado");
		}
	}

	@Nested
	@DisplayName("changeState()")
	class ChangeState
	{
		@Test
		@DisplayName("No se puede retroceder de estado")
		void cannotGoBackwards()
		{
			Project project = projectInState(ProjectState.OPEN);
			assertThatThrownBy(() -> project.changeState(ProjectState.DRAFT))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("retroceder");
		}

		@Test
		@DisplayName("Se puede avanzar saltando estados (caso admin)")
		void canSkipStates()
		{
			Project project = projectInState(ProjectState.DRAFT);
			project.changeState(ProjectState.OPEN);
			assertThat(project.getState()).isEqualTo(ProjectState.OPEN);
		}
	}
}