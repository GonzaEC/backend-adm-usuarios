// src/main/java/com/plataforma/repository/UserRepository.java
package com.plataforma.repository;

import com.plataforma.model.User;
import com.plataforma.model.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long>
{
	Optional<User> findByEmail(String email);
	boolean existsByRole(Role role);
}