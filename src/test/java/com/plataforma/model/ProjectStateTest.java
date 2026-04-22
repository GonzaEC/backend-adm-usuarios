package com.plataforma.model;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
class ProjectStateTest
{

	@Test
	void projectShouldStartInDraft()
	{
		Project project = Project.builder().build();

		assertEquals(ProjectState.DRAFT, project.getState());
	}

	@Test
	void shouldAdvanceStateCorrectly()
	{
		Project project = Project.builder().build();

		project.advanceState(); // DRAFT -> PRE_OPEN
		assertEquals(ProjectState.PRE_OPEN, project.getState());

		project.advanceState(); // PRE_OPEN -> OPEN
		assertEquals(ProjectState.OPEN, project.getState());
	}

	@Test
	void shouldNotGoBackwards()
	{
		Project project = Project.builder().state(ProjectState.OPEN).build();

		assertThrows(IllegalStateException.class, () -> {
			project.changeState(ProjectState.DRAFT);
		});
	}

	@Test
	void shouldAllowInvestOnlyInValidStates()
	{
		Project project = Project.builder().state(ProjectState.DRAFT).build();

		assertFalse(project.canReceiveInvestments());

		project.changeState(ProjectState.PRE_OPEN);
		assertTrue(project.canReceiveInvestments());

		project.changeState(ProjectState.OPEN);
		assertTrue(project.canReceiveInvestments());

		project.changeState(ProjectState.CLOSED);
		assertFalse(project.canReceiveInvestments());
	}
}