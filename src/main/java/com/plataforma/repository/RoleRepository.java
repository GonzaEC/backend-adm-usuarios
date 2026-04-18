// src/main/java/com/plataforma/repository/ProjectRepository.java
package com.plataforma.repository;

import com.plataforma.model.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long>
{
	Optional<Role> findByName(String name);
}