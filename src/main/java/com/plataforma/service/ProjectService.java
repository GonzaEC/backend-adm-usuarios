package com.plataforma.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.plataforma.model.Investment;
import com.plataforma.model.Project;

@Service
public class ProjectService
{
	/**
	 * De momento esto es una plantilla al futuro user story implementado en codigo
	 * Integrara otros servicios y logica de proyecto.
	 * Esto es un stub, no debe ejecutarse hasta que se creen y testeen el resto de los componentes.
	 * @param user
	 * @param project
	 * @param amount
	 */
	/*
	public void invest(User user, Project project, BigDecimal amount)
	{

		// Check Permisos
		accessControlService.validateInvest(user);

		// Check estado del proyecto
		if (!project.canReceiveInvestments())
			throw new IllegalStateException(
				"El proyecto no acepta inversiones en su estado actual"
		);

		// Check otras reglas de negocio
		if (amount.compareTo(BigDecimal.ZERO) <= 0)
			throw new IllegalArgumentException("Monto inválido");

		// Crear inversión (ejemplo)
		Investment investment = new Investment();
		investment.setUser(user);
		investment.setProject(project);
		investment.setAmountPaid(amount);

		// persistir...
	}
	*/
}