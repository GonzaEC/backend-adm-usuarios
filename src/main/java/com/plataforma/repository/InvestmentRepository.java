// src/main/java/com/plataforma/repository/InvestmentRepository.java
package com.plataforma.repository;

import com.plataforma.model.Investment;
import com.plataforma.model.Project;
import com.plataforma.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, Long>
{
	/**
	 * Historial completo de inversiones de un usuario.
	 * Util para el dashboard del inversor.
	 */
	List<Investment> findByUser(User user);

	/**
	 * Historial completo de inversiones en un proyecto.
	 * Util para el dashboard del desarrollador/admin.
	 */
	List<Investment> findByProject(Project project);

	/**
	 * ¿Ya invirtio este usuario en este proyecto alguna vez?
	 * Util para auditoria — no bloquea reinversion, solo informa.
	 */
	boolean existsByUserAndProject(User user, Project project);
}