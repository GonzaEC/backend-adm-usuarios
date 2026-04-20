// src/main/java/com/plataforma/service/UserService.java
package com.plataforma.service;

import com.plataforma.constant.RoleConstants;

import com.plataforma.model.Role;
import com.plataforma.model.User;

import com.plataforma.repository.RoleRepository;
import com.plataforma.repository.UserRepository;

import com.plataforma.exception.RoleNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * Reglas de "Dominio", el dominio es un nivel de abstraccion que maneja la logica pura.
 * No se ocupa de llamar a servicios como HTTP, POSTGRESQL, RABBIT, etc.
 * Nivel de abstraccion alto donde uno puede expresar reglas de negocio.
 * Otras clases/servicios lo incorporaran.
 * 
 * ¿Para que sirve?
 * Independencia tecnologica.
 * Si mas adelante se decide que el inversor puede subir proyectos, solo se cambia una linea en el UserService.
 *  el resto de la plataforma (controladores, tokens, base de datos) no se entera.
 * Seguridad por capas.
 * Si bien se usara Auth0 (o similar) para saber quien es el usuario.
 *   Esta logica de UserService es la que decide que se puede hacer.
 *   La ultima línea de defensa.
 */
@Service
@RequiredArgsConstructor
public class UserService
{
	private final UserRepository  userRepository;
	private final RoleRepository  roleRepository;
	private final PasswordEncoder passwordEncoder;

	public User registerUser(User user)
	{
		// Encriptamos la password antes de persistir
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		if (user.getRole() == null)
		  {
			Role defaultRole = roleRepository.findByName(RoleConstants.BASIC)
				.orElseThrow(() -> new RoleNotFoundException(
					"Critico: Rol no config."
				));
			user.setRole(defaultRole);
		  }
		user.setActive(true);
		return userRepository.save(user);
	}

	public User updateUser(Long id, User details)
	{
		User user = getUserById(id)
			.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		user.setEmail(details.getEmail());
		// se puede agregar mas campos aca
		return userRepository.save(user);
	}

	public void deactivateUser(Long id)
	{
		User user = getUserById(id)
			.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
		user.setActive(false);
		userRepository.save(user);
	}

	public void activateUser(Long id)
	{
		User user = getUserById(id)
			.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
		user.setActive(true);
		userRepository.save(user);
	}

	public Page<User> getAllUsers(Pageable pageable)
	{
		return userRepository.findAll(pageable);
	}

	public Optional<User> getUserById(Long id)
	{
		return userRepository.findById(id);
	}

	public Optional<Role> getRoleById(Long id)
	{
		return roleRepository.findById(id);
	}

	public User assignRole(User user, Role role)
	{
		user.setRole(role);
		return userRepository.save(user);
	}
}