// src/main/java/com/plataforma/controller/InvestmentController.java
package com.plataforma.controller;

import com.plataforma.dto.ApiResponse;
import com.plataforma.dto.InvestmentRequest;
import com.plataforma.dto.InvestmentResponse;
import com.plataforma.exception.ProjectNotFoundException;
import com.plataforma.model.Investment;
import com.plataforma.model.Project;
import com.plataforma.model.User;
import com.plataforma.repository.ProjectRepository;
import com.plataforma.service.InvestmentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class InvestmentController
{
	private final InvestmentService investmentService;
	private final ProjectRepository projectRepository;

	/**
	 * POST /api/projects/{projectId}/investments
	 *
	 * El inversor autenticado compra tokens de un proyecto.
	 *
	 * - projectId:  viene del path (el proyecto es el recurso padre — RESTful)
	 * - tokensToBuy: viene del body
	 * - investor:   viene del JWT vía @AuthenticationPrincipal (no del body)
	 *
	 * Respuestas:
	 *   201 Created              Inversion exitosa, devuelve el comprobante
	 *   400 Bad Request          tokensToBuy <= 0
	 *   403 Forbidden            usuario no tiene permiso INVEST_CREATE
	 *   404 Not Found            proyecto no existe
	 *   409 Conflict             proyecto no acepta inversiones (DRAFT o CLOSED)
	 *   422 Unprocessable Entity fondos insuficientes
	 */
	@PostMapping("/{projectId}/investments")
	public ResponseEntity<ApiResponse<InvestmentResponse>> invest(
		@PathVariable Long projectId,
		@RequestBody InvestmentRequest request,
		@AuthenticationPrincipal User investor
	)
	{
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new ProjectNotFoundException(
				"Proyecto no encontrado: " + projectId
			));

		Investment investment = investmentService.invest(
			investor,
			project,
			request.getTokensToBuy()
		);

		InvestmentResponse response = InvestmentResponse.from(investment);

		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.success("Inversión realizada", response));
	}
}