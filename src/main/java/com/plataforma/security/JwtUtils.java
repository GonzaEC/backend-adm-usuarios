// src/main/java/com/plataforma/security/JwtUtils.java
package com.plataforma.security;

import com.plataforma.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;

import java.security.Key;

import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtils {
	// En produccion, esta clave debe venir de una variable de entorno
	private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

	private final int jwtExpirationMs = 900000; // 15 minutos

	private Claims extractAllClaims(String token)
	{
		return Jwts.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token)
			.getBody();
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver)
	{
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	public String generateToken(User user)
	{
		// check que usuario tenga rol asignado para prevenir NullPointerException
		String roleName = (user.getRole() != null)
			? user.getRole().getName() : "NONE";

		List<String> permissions = (user.getRole() != null && user.getRole().getPermissions() != null)
			? user.getRole().getPermissions().stream()
				.map(p -> p.getName())
				.collect(Collectors.toList())
			: List.of();

		return Jwts.builder()
			.setSubject(user.getEmail())
			.claim("role", roleName)
			.claim("id", user.getId())
			.claim("permissions", permissions)
			.setIssuedAt(new Date())
			.setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
			.signWith(key)
			.compact();
	}
	/**
	 * Dado un token, obtener mail del usuario.
	 * @param token
	 * @return
	 */
	public String getSubject(String token)
	{
		return extractClaim(token, Claims::getSubject);
	}
	/**
	 * Obtener un claim especifico (ej: "role")
	 * @param token
	 * @param claimName
	 * @return
	 */
	public String getClaim(String token, String claimName)
	{
		final Claims claims = extractAllClaims(token);
		return claims.get(claimName, String.class);
	}
	/**
	 * True si el token es estructuralmente correcto y no expiro. False, en caso contrario.
	 * @param token
	 * @return
	 */
	public boolean validateToken(String token)
	{
		try
		  {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		  }
		catch (JwtException | IllegalArgumentException e)
		  {
			// se podria loguear por qué falló (expirado, firma inválida, etc.)
			return false;
		  }
	}
}
