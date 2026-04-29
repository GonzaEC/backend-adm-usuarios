// src/test/java/com/plataforma/service/UserServiceTest.Java
package com.plataforma.service;

// Modelos
import com.plataforma.model.Project;
import com.plataforma.model.Role;
import com.plataforma.model.User;

// Excepciones
import com.plataforma.exception.UnauthorizedAccessException;
import com.plataforma.constant.RoleConstants;
import com.plataforma.exception.InsufficientPermissionsException;
import com.plataforma.exception.OwnershipException;

// JUnit 5 para aserciones y motor tests
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

// Mockito para hacer test "en el aire" sin levantar Spring completo
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Conjuntos de test que revisan si se cumplen las reglas de acceso basicas
 * (user story o reglas de negocio).
 * De pasarse correctamente todos los test:
 *   + Solo los admins pueden borrar los proyectos.
 *   + Solo los admins pueden cambiar el permiso a los usuarios (excepto demotear otros admins).
 *   + Solo los desarrolladores dueños del proyecto pueden modificar el suyo.
 *   + Solo los inversores pueden participar de un proyecto.
 * Que NO contempla?
 *   - El estado del proyecto, todavia no se determino y asigno los estados del proyecto
 *     Se podria hacer INICIALIZADO => NO-CONSOLIDADO (o como se habia def. en la doc) => CONSOLIDADO => FINALIZADO.
 *     Y aplicar test que no se puede volver atras en ciertos estados.
 *   - Integracion con AUTH0 o servicios similares.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AccessControlServiceTest
{
	@InjectMocks
	private AccessControlService accessControlService;

	private Role createRole(String name)
	{
		return Role.builder().name(name).build();
	}

	private User createUserWithRole(String roleName)
	{
		return User.builder()
			.role(createRole(roleName))
			.build();
	}

	@Test
	void testAdminCannotDemoteAdmin()
	{
		User adminActor  = createUserWithRole(RoleConstants.ADMIN);
		User adminTarget = createUserWithRole(RoleConstants.ADMIN);
		Role investorRol = createRole(RoleConstants.INVESTOR);

		assertThrows(UnauthorizedAccessException.class, () -> {
			accessControlService.validateChangeRole(
				adminActor, adminTarget, investorRol
			);
		}, "Un administrador no debe degradar a otro");
	}

	@Test
	void testAdminCanPromoteToDeveloper()
	{
		User admin    = createUserWithRole(RoleConstants.ADMIN);
		User investor = createUserWithRole(RoleConstants.INVESTOR);
		Role devRole  = createRole(RoleConstants.DEVELOPER);

		assertDoesNotThrow(() -> {
			accessControlService.validateChangeRole(
				admin, investor, devRole
			);
		});
	}

	@Test
	void developerCannotInvestInProjects()
	{
		User developer = createUserWithRole(RoleConstants.DEVELOPER);

		assertThrows( InsufficientPermissionsException.class, () -> {
			accessControlService.validateInvest(developer);
		}, "Un desarrollador no debe poder invertir");
	}

	@Test
	void investorCannotCreateProjects()
	{
		User investor = createUserWithRole(RoleConstants.INVESTOR);

		assertThrows(UnauthorizedAccessException.class, () -> {
			accessControlService.validateCreateProject(investor);
		}, "Un inversor no debe poder dar de alta proyectos");
	}

	@Test
	void investorCannotChangePermissions()
	{
		User investor = createUserWithRole(RoleConstants.INVESTOR);
		User target   = createUserWithRole(RoleConstants.INVESTOR);
		Role devRole  = createRole(RoleConstants.DEVELOPER);

		assertThrows(UnauthorizedAccessException.class, () -> {
			accessControlService.validateChangeRole(
				investor, target, devRole
			);
		}, "Un inversor no puede cambiar permisos");
	}

	@Test
	void basicUserCannotChangePermissions()
	{
		User basic   = User.builder().email("basic@mail.com").build();
		User target  = createUserWithRole(RoleConstants.INVESTOR);
		Role devRole = createRole(RoleConstants.DEVELOPER);

		assertThrows(UnauthorizedAccessException.class, () -> {
			accessControlService.validateChangeRole(
				basic, target, devRole
			);
		}, "Un usuario basico no puede cambiar permisos");
	}

	@Test
	void developerCannotChangePermissions()
	{
		User developer = createUserWithRole(RoleConstants.DEVELOPER);
		User target    = createUserWithRole(RoleConstants.INVESTOR);
		Role devRole   = createRole(RoleConstants.DEVELOPER);

		assertThrows(UnauthorizedAccessException.class, () -> {
			accessControlService.validateChangeRole(
				developer, target, devRole
			);
		}, "Un developer no puede cambiar permisos");
	}

	@Test
	void developerCannotDeleteProject()
	{
		Role devRole   = createRole(RoleConstants.DEVELOPER);
		User developer = User.builder().id(10L).role(devRole).build();
		// Incluso si es SU proyecto, segun una regla de negocio, developer no puede darlo de baja
		assertThrows(UnauthorizedAccessException.class, () -> {
			accessControlService.validateDeleteProject(developer);
		}, "Un developer no puede dar de baja un proyecto");
	}

	@Test
	void developerThrowsExceptionWhenModifyingOthersProject()
	{
		Role devRole = createRole(RoleConstants.DEVELOPER);
		User dev     = User.builder().id(10L).role(devRole).build();
		User owner   = User.builder().id(99L).role(devRole).build();

		Project project = Project.builder().id(1L).owner(owner).build();

		assertThrows(
			OwnershipException.class, () -> {
				accessControlService.validateModifyProject(dev, project);
			}, "Un desarrollador no debe poder modificar proyectos ajenos"
		);
	}

	@Test
	void adminCanModifyAnyProject()
	{
		Role devRole   = createRole(RoleConstants.DEVELOPER);
		Role adminRole = createRole(RoleConstants.ADMIN);

		User admin = User.builder().id(1L).role(adminRole).build();
		User owner = User.builder().id(99L).role(devRole).build();

		Project project = Project.builder().id(2L).owner(owner).build();

		assertDoesNotThrow(() -> {
			accessControlService.validateModifyProject(admin, project);
		}, "Un administrador puede modificar cualquier proyecto");
	}

	@Test
	void investorWithoutPermissionsCannotCreateProjects()
	{
		// Crear un inversor SIN el permiso de creación
		Role investorRole = createRole(RoleConstants.INVESTOR); // Solo el rol, sin permisos
		User investor = User.builder().role(investorRole).build();

		assertThrows(UnauthorizedAccessException.class, () -> {
			accessControlService.validateCreateProject(investor);
		}, "Debería fallar porque el inversor no tiene el permiso explícito");
	}
}