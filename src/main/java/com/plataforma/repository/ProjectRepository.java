// src/main/java/com/plataforma/repository/ProjectRepository.java
package com.plataforma.repository;

import com.plataforma.model.Project;
import com.plataforma.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>
{
	List<Project> findByOwner(User owner);
}