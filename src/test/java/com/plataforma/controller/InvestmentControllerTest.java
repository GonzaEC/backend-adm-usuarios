// src/test/java/com/plataforma/controller/InvestmentControllerTest.java
package com.plataforma.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.plataforma.dto.InvestmentRequest;

import com.plataforma.exception.InsufficientFundsException;
import com.plataforma.exception.ProjectNotAvailableException;

import com.plataforma.model.*;
import com.plataforma.repository.ProjectRepository;
import com.plataforma.repository.UserRepository;
import com.plataforma.security.JwtUtils;
import com.plataforma.service.InvestmentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test unitario del InvestmentController.
 *
 * Qué testa: routing, seguridad (JWT + permisos), serialización HTTP.
 * Qué NO testa: lógica de negocio (eso es responsabilidad de InvestmentServiceImpl).
 *
 * Estrategia JWT — igual que SecurityIntegrationTest:
 *   1. @MockBean UserRepository → el filtro JWT encuentra al usuario mockeado
 *   2. JwtUtils real            → genera tokens reales firmados
 *   3. MockMvc                  → simula requests HTTP completos
 *
 * @Tag("unit") → corre con `mvn test` (perfil default, excluye "integration")
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("unit")
@DisplayName("Controller: POST /api/projects/{id}/investments")
class InvestmentControllerTest
{
	@Autowired MockMvc      mockMvc;
	@Autowired JwtUtils     jwtUtils;
	@Autowired ObjectMapper objectMapper;

	// Mocks de infraestructura — el contexto Spring es real, solo estos son simulados
	@MockBean UserRepository    userRepository;
	@MockBean ProjectRepository projectRepository;
	@MockBean InvestmentService investmentService;

	// Datos de prueba reutilizables entre tests
	private User    investorUser;
	private User    basicUser;
	private Project preOpenProject;
	private Project draftProject;
	private String  investorToken;
	private String  basicToken;

	private static final Long PROJECT_ID = 1L;

	@BeforeEach
	void setUp()
	{
		// --- Roles y permisos ---
		Permission investPermission = Permission.builder()
			.name("invest:create")
			.build();

		Role investorRole = Role.builder()
			.name("INVESTOR")
			.permissions(Set.of(investPermission))
			.build();

		Role basicRole = Role.builder()
			.name("BASIC")
			.permissions(Set.of())
			.build();

		// --- Usuarios ---
		investorUser = User.builder()
			.id(1L)
			.email("investor@test.com")
			.role(investorRole)
			.active(true)
			.build();

		basicUser = User.builder()
			.id(2L)
			.email("basic@test.com")
			.role(basicRole)
			.active(true)
			.build();

		// --- Proyectos ---
		preOpenProject = Project.builder()
			.id(PROJECT_ID)
			.name("Parque Solar Mendoza")
			.state(ProjectState.PRE_OPEN)
			.tokenPrice(BigDecimal.valueOf(100))
			.build();

		draftProject = Project.builder()
			.id(2L)
			.name("Proyecto Borrador")
			.state(ProjectState.DRAFT)
			.tokenPrice(BigDecimal.valueOf(50))
			.build();

		// --- Tokens JWT reales ---
		investorToken = jwtUtils.generateToken(investorUser);
		basicToken    = jwtUtils.generateToken(basicUser);

		// --- Mocks del UserRepository (para el filtro JWT) ---
		when(userRepository.findByEmail("investor@test.com"))
			.thenReturn(Optional.of(investorUser));
		when(userRepository.findByEmail("basic@test.com"))
			.thenReturn(Optional.of(basicUser));
	}

	// =========================================================
	// Helpers
	// =========================================================

	private String bearer(String token) { return "Bearer " + token; }

	private String requestBody(long tokensToBuy) throws Exception
	{
		InvestmentRequest req = new InvestmentRequest();
		req.setTokensToBuy(tokensToBuy);
		return objectMapper.writeValueAsString(req);
	}

	// =========================================================
	// Seguridad — sin token / token inválido
	// =========================================================

	@Nested
	@DisplayName("Autenticación")
	class Autenticacion
	{
		@Test
		@DisplayName("401 cuando no se envía token")
		void sinToken_devuelve401() throws Exception
		{
			mockMvc.perform(post("/api/projects/{id}/investments", PROJECT_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody(10)))
				.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("401 cuando el token es inválido")
		void tokenInvalido_devuelve401() throws Exception
		{
			mockMvc.perform(post("/api/projects/{id}/investments", PROJECT_ID)
					.header(HttpHeaders.AUTHORIZATION, "Bearer token-basura")
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestBody(10)))
				.andExpect(status().isUnauthorized());
		}
	}

	// =========================================================
	// Regla 1: inversor puede participar en proyecto PRE_OPEN u OPEN
	// =========================================================
	@Nested
	@DisplayName("Regla 1: proyecto PRE_OPEN o OPEN acepta inversiones")
	class ProyectoAbierto
	{
		@Test
		@DisplayName("201 cuando inversor invierte en proyecto PRE_OPEN")
		void inversoreEnPreOpen_devuelve201() throws Exception
		{
			when(projectRepository.findById(PROJECT_ID))
				.thenReturn(Optional.of(preOpenProject));

			Investment mockInvestment = new Investment();

			mockInvestment.set(
				investorUser,
				preOpenProject,
				10L,
				BigDecimal.valueOf(1000),
				BigDecimal.valueOf(1000)
			);

			when(investmentService.invest(any(User.class), any(Project.class), eq(10L)))
				.thenReturn(mockInvestment);

			mockMvc.perform(post("/api/projects/{id}/investments", PROJECT_ID)
					.header(HttpHeaders.AUTHORIZATION, bearer(investorToken))
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestBody(10)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.tokensPurchased").value(10))
				.andExpect(jsonPath("$.data.amountPaid").value(1000));
		}
	}

	// =========================================================
	// Regla 2: proyecto DRAFT o CLOSED no acepta inversiones
	// =========================================================
	@Nested
	@DisplayName("Regla 2: proyecto DRAFT o CLOSED rechaza inversiones")
	class ProyectoNoDisponible
	{
		@Test
		@DisplayName("409 cuando el proyecto está en DRAFT")
		void proyectoDraft_devuelve409() throws Exception
		{
			when(projectRepository.findById(2L))
				.thenReturn(Optional.of(draftProject));

			when(investmentService.invest(any(), any(), any()))
				.thenThrow(new ProjectNotAvailableException(
					"El proyecto no acepta inversiones en estado DRAFT"
				));

			mockMvc.perform(post("/api/projects/{id}/investments", 2L)
					.header(HttpHeaders.AUTHORIZATION, bearer(investorToken))
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestBody(5)))
				.andExpect(status().isConflict());
		}

		@Test
		@DisplayName("409 cuando el proyecto está CLOSED")
		void proyectoClosed_devuelve409() throws Exception
		{
			Project closedProject = Project.builder()
				.id(3L)
				.name("Proyecto Cerrado")
				.state(ProjectState.CLOSED)
				.tokenPrice(BigDecimal.valueOf(50))
				.build();

			when(projectRepository.findById(3L))
				.thenReturn(Optional.of(closedProject));

			when(investmentService.invest(any(), any(), any()))
				.thenThrow(new ProjectNotAvailableException(
					"El proyecto no acepta inversiones en estado CLOSED"
				));

			mockMvc.perform(post("/api/projects/{id}/investments", 3L)
					.header(HttpHeaders.AUTHORIZATION, bearer(investorToken))
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestBody(5)))
				.andExpect(status().isConflict());
		}
	}

	// =========================================================
	// Regla 3: fondos insuficientes
	// =========================================================
	@Nested
	@DisplayName("Regla 3: fondos insuficientes")
	class FondosInsuficientes
	{
		@Test
		@DisplayName("422 cuando el wallet no tiene saldo suficiente")
		void fondosInsuficientes_devuelve422() throws Exception
		{
			when(projectRepository.findById(PROJECT_ID))
				.thenReturn(Optional.of(preOpenProject));

			when(investmentService.invest(any(), any(), any()))
				.thenThrow(new InsufficientFundsException(
					"Saldo insuficiente para completar la inversión"
				));

			mockMvc.perform(post("/api/projects/{id}/investments", PROJECT_ID)
					.header(HttpHeaders.AUTHORIZATION, bearer(investorToken))
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestBody(9999)))
				.andExpect(status().isUnprocessableEntity());
		}
	}

	// =========================================================
	// Regla extra: usuario sin permiso INVEST_CREATE
	// =========================================================
	@Nested
	@DisplayName("Autorización: permiso invest:create")
	class Autorizacion
	{
		@Test
		@DisplayName("403 cuando un usuario BASIC intenta invertir")
		void usuarioSinPermiso_devuelve403() throws Exception
		{
			when(projectRepository.findById(PROJECT_ID))
				.thenReturn(Optional.of(preOpenProject));

			// NOTA: si no usamos @PreAuthorize en el controller (lo delegamos
			// al service), este test valida que el service lanza la excepción
			// y el GlobalExceptionHandler la mapea a 403.
			when(investmentService.invest(any(), any(), any()))
				.thenThrow(new com.plataforma.exception.InsufficientPermissionsException(
				"El usuario no tiene permitido hacer inversiones."
				));

			mockMvc.perform(post("/api/projects/{id}/investments", PROJECT_ID)
					.header(HttpHeaders.AUTHORIZATION, bearer(basicToken))
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestBody(5)))
				.andExpect(status().isForbidden());
		}
	}
}