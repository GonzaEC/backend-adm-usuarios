// src/main/java/com/plataforma/repository/UserProjectRepository.java
package com.plataforma.repository;

import com.plataforma.model.Project;
import com.plataforma.model.User;
import com.plataforma.model.UserProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProjectRepository extends JpaRepository<UserProject, Long>
{
	/**
	 * Busca la membresia de un usuario en un proyecto especifico.
	 * Retorna Optional porque puede no existir (primera inversion del usuario).
	 */
	Optional<UserProject> findByUserAndProject(User user, Project project);
}