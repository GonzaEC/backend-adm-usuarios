package com.plataforma.controller;

import com.plataforma.model.Permission;
import com.plataforma.model.Role;
import com.plataforma.model.User;
import com.plataforma.repository.UserRepository;
import com.plataforma.security.JwtUtils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    // ¡CLAVE! Mockeamos el repositorio para que el Filtro JWT encuentre a los
    // usuarios
    @MockBean
    private UserRepository userRepository;

    // --- MÉTODOS DE AYUDA ---

    private String generarTokenYMockearBD(String email, String roleName, String... permisos) {
        // 1. Creamos los permisos
        Set<Permission> perms = Arrays.stream(permisos)
                .map(p -> Permission.builder().name(p).build())
                .collect(Collectors.toSet());

        // 2. Creamos el rol y el usuario
        Role role = Role.builder().name(roleName).permissions(perms).build();
        User mockUser = User.builder().id(1L).email(email).role(role).build();

        // 3. Le decimos a Spring: "Cuando el filtro busque este email, devuelve este
        // usuario"
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        // 4. Generamos el token real
        return jwtUtils.generateToken(mockUser);
    }

    // --- ÍTEM 1: MIDDLEWARE FUNCIONANDO (401 Unauthorized) ---

    @Test
    void debeDevolver401_CuandoNoSeEnviaToken() throws Exception {
        mockMvc.perform(get("/api/proyectos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void debeDevolver401_CuandoSeEnviaTokenInvalido() throws Exception {
        mockMvc.perform(get("/api/proyectos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-basura-123"))
                .andExpect(status().isUnauthorized());
    }

    // --- ÍTEM 2 y 3: VALIDACIÓN POR PERMISO (403 Forbidden vs 200 OK) ---

    @Test
    void debeDevolver403_CuandoUsuarioNoTienePermisoDeBorrado() throws Exception {
        String tokenDev = generarTokenYMockearBD("dev@mail.com", "DEVELOPER", "PROJECT_READ");

        // CAMBIO: Apuntamos al controlador de prueba
        mockMvc.perform(delete("/api/test-security/borrar")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDev))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    void debeDevolver200_CuandoAdminIntentaBorrar() throws Exception {
        String tokenAdmin = generarTokenYMockearBD("admin@mail.com", "ADMIN", "PROJECT_DELETE");

        // CAMBIO: Apuntamos al controlador de prueba
        mockMvc.perform(delete("/api/test-security/borrar")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk()); // 200
    }
}