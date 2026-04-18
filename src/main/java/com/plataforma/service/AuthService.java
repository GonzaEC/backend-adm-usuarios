// src/main/java/com/plataforma/service/AuthService.java
package com.plataforma.service;

import com.plataforma.exception.InsufficientPermissionsException;
import com.plataforma.exception.UnauthorizedAccessException;
import com.plataforma.exception.UserNotFoundException;

import com.plataforma.model.User;

import com.plataforma.repository.UserRepository;

import com.plataforma.security.JwtUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService
{

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtils jwtUtils; // Clase que crearemos para manejar el token

	public String login(String email, String password)
	{
		User user = userRepository.findByEmail(email)
			.orElseThrow(
				() -> new UnauthorizedAccessException("Credenciales inválidas")
			);

		if (!passwordEncoder.matches(password, user.getPassword()))
			throw new UnauthorizedAccessException("Credenciales inválidas");

		if (!user.isActive()) throw new InsufficientPermissionsException(
			"Tu cuenta está desactivada."
		);

		return jwtUtils.generateToken(user);
	}

	@Transactional
	public void changePassword(Long userId, String oldPassword, String newPassword)
	{
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserNotFoundException(userId));

		if (!passwordEncoder.matches(oldPassword, user.getPassword()))
			throw new UnauthorizedAccessException(
			"La contraseña actual es incorrecta."
		);

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
	}
}