// src/main/java/com/plataforma/security/JwtAuthenticationFilter.java
package com.plataforma.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.plataforma.model.Role;
import com.plataforma.model.User;
import com.plataforma.repository.UserRepository;
import com.plataforma.model.Permission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j // Agregamos log de Lombok para manejar errores
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtils jwtUtils;
	private final UserRepository userRepository;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		try {
			String authHeader = request.getHeader("Authorization");

			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				String token = authHeader.substring(7);

				if (jwtUtils.validateToken(token)) {
					String email = jwtUtils.getSubject(token);

					// Buscamos al usuario real para sacar sus permisos
					User user = userRepository.findByEmail(email)
							.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

					// Cargamos Roles Y Permisos (Para usuarios con un solo rol)
					List<SimpleGrantedAuthority> authorities = new ArrayList<>();

					Role role = user.getRole(); 

					// 1. Agregamos el rol único
					authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

					// 2. Agregamos los permisos de ese rol
					for (Permission permission : role.getPermissions()) {
						authorities.add(new SimpleGrantedAuthority(permission.getName()));
					}

					// Pasamos el objeto 'user' completo en lugar de solo el email
					// Útil para después obtener el ID del usuario logueado en los controladores
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
							user, null, authorities);

					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
		} catch (Exception e) {
			// para que si el token es inválido o expiró, la app no se rompa.
			log.error("No se pudo establecer la autenticación: {}", e.getMessage());
		}

		filterChain.doFilter(request, response);
	}
}