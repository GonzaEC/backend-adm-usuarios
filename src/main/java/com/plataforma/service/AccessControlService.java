// src/main/java/com/plataforma/service/AccessControlService.java
package com.plataforma.service;

import com.plataforma.model.Role;
import com.plataforma.model.User;
import com.plataforma.model.Project;

import lombok.RequiredArgsConstructor;

import com.plataforma.constant.PermissionConstants;
import com.plataforma.constant.RoleConstants;

import com.plataforma.exception.UnauthorizedAccessException;
import com.plataforma.exception.InsufficientPermissionsException;
import com.plataforma.exception.OwnershipException;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessControlService
{
	private boolean hasRole(User user, String roleName)
	{
		return user.getRole() != null
			&& user.getRole().getName().equals(roleName);
	}

	private boolean isAdmin(User user)
	{
		return hasRole(user, RoleConstants.ADMIN);
	}

	public void validateChangeRole(User actor, User target, Role newRole)
	{
		// Esto es jerarquia pura un admin solo puede hacer esto
		if(!isAdmin(actor)) throw new UnauthorizedAccessException(
			"No se puede cambiar los permisos al usuario especificado."
		);
		if(isAdmin(target) && !newRole.getName().equals(RoleConstants.ADMIN))
			throw new UnauthorizedAccessException(
				"No se puede demotear a otro Admin."
			);
	}

	public void validateInvest(User user)
	{

		if(!user.hasPermission(PermissionConstants.INVEST_CREATE))
			throw new InsufficientPermissionsException(
				"El usuario no tiene permitido hacer inversiones."
			);
	}

	public void validateCreateProject(User user)
	{
		// En lugar de checkear RoleConstants.DEVELOPER, checkear el permiso
		if(!user.hasPermission(PermissionConstants.PROJECT_CREATE))
			throw new UnauthorizedAccessException(
				"No puede crear proyectos."
			);
	}
	/**
	 * Dado un usuario y un proyecto se valida si el 1° puede modificar al 2°.
	 * @param user
	 * @param project
	 * @throws UnauthorizedAccessException
	 */
	public void validateModifyProject(User user, Project project)
	{
		if (isAdmin(user)) return; // Admin siempre puede (por monitoreo y auditoria)

		if (hasRole(user, RoleConstants.DEVELOPER))
		  {
			if (!project.isOwnedBy(user))
				throw new OwnershipException(
					"Acceso denegado: No eres propietario de este proyecto."
				);
			return;
		  }
		throw new InsufficientPermissionsException(
			"Tu perfil no tiene permisos para modificar proyectos"
		);
	}
	/**
	 * Dado un usuario, se calida si puede borrar proyectos
	 *
	 * @param user
	 * @throws UnauthorizedAccessException
	 */
	public void validateDeleteProject(User user)
	{
		if(!isAdmin(user)) throw new UnauthorizedAccessException(
			"Solo un admin puede eliminar un proyecto."
		);
	}
}
