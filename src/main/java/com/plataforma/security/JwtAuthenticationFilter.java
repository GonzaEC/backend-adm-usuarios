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

			if (authHeader == null || !authHeader.startsWith("Bearer "))
			{
				filterChain.doFilter(request, response);
				return;
			}
			String token = authHeader.substring(7);

			if (!jwtUtils.validateToken(token))
			{
				filterChain.doFilter(request, response);
				return;
			}

			String email = jwtUtils.getSubject(token);

			// Buscar usuario
			User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

			// Cargar Roles Y Permisos
			List<SimpleGrantedAuthority> authorities = new ArrayList<>();

			Role role = user.getRole();

			if (user.getRole() == null) {
				log.error("USER SIN ROLE: {}", user.getEmail());
			}

			// Agregar el rol único
			authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

			// Agregar los permisos de ese rol
			for (Permission permission : role.getPermissions()) authorities.add(
				new SimpleGrantedAuthority(permission.getName())
			);

			// Auth
			UsernamePasswordAuthenticationToken authToken =
				new UsernamePasswordAuthenticationToken(user, null, authorities);


			authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authToken);
		}
		catch (Exception e)
		{
			// para que si el token es inválido o expiró, la app no se rompa.
			log.error("JWT FILTER ERROR", e);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
			return;
		}
		filterChain.doFilter(request, response);
	}
}