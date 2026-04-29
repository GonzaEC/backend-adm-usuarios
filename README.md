# RBAC-Springboot
Modulo backend implementado en Spring-Boot. Atiende las peticiones de ["frontend-adm-usuarios"](https://github.com/GonzaEC/frontend-adm-usuarios)


# Requisitos
* [JDK 21 (LTS)+](https://www.oracle.com/java/technologies/downloads/)
* [Spring-Boot 3.2.4+](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot/3.2.4)
* [Maven: 3.9+](https://maven.apache.org/download.cgi?.)

# Dependencias Principales
* Spring Web (**```spring-boot-starter-web```**)
* Spring Security (**```spring-boot-starter-security```**) para manejo de autenticación y autorización.
* Spring Data JPA (**```spring-boot-starter-data-jpa```**) para persistencia con ORM (Hibernate).
* H2 Database (**```com.h2database:h2```**) Base de datos en memoria (runtime)
* Lombok (**```org.projectlombok:lombok```**) para reducir boilerplate (getters, setters, builders, etc.)
* Spring Boot Test (**```spring-boot-starter-test```**) para testing (JUnit, Mockito, etc.)
* Flyway

# Plugins de Build
**Spring Boot Maven Plugin** que permite:
* Empaquetar la app (.jar)
* Ejecutar con ```mvn spring-boot:run```

# Instalacion
## Java (jdk-21)
Una vez instalado el software, puede que Windows no sepa donde está guardado el ejecutable de Java. Para arreglarlo, se debe configurar las Variables de Entorno.
1. **Localizar carpeta de instalación**: Por defecto suele ser: **```C:\Program Files\Java\jdk-21```**. Entrar a esa carpeta y luego a la carpeta bin. Copiá esa ruta (debería ser algo como **```C:\Program Files\Java\jdk-21\bin```**).
2. **Configurar Variables de Entorno**
    * Presionar tecla Windows y escribí: variables de entorno.
    * Seleccionar "Editar las variables de entorno del sistema".
    * En la ventanita que aparece, click abajo de todo en el botón "Variables de entorno...."
    * Hay dos secciones. ir a la de abajo (Variables del sistema):
        - Buscar una llamada "Path", seleccionar y click a "Editar".
        - click en "Nuevo" y pegar la ruta que copiada (la que termina en \bin).
        - Aceptar todo.

3. **Crear variable JAVA_HOME** En la misma ventana de Variables del sistema:
    * click en "Nuevo".
    * Nombre de la variable: JAVA_HOME
    * Valor de la variable: **```C:\Program Files\Java\jdk-21```*** (**NO** lleva el **\bin** al final).
    * Aceptar todo y cerrar ventanas.

4. **Reiniciar la terminal** Cerrar VS Codium por completo y volverlo a abrir. Abrí una terminal nueva y escribir:
```PowerShell
    java -version
```

## Maven (de la pagina web)
1. ir a [**```maven.apache.org```**](https://maven.apache.org/)
2. Descargar zip que dice "Binary zip archive" (ej: **```apache-maven-3.9.x-bin.zip```**).
2. Extraerlo en un path, por ejemplo: **```C:\Program Files\Maven```**.
3. Agregalo al Path del sistema ([Igual que hiciste con Java](#java-jdk-21)):
    * Buscar "Variables de entorno".
    * En Variables del sistema -> Path -> Editar.
    * Agregar la ruta a la carpeta bin de lo descomprimido (ej: **```C:\Program Files\Maven\bin```**).
4. Reiniciar terminal y probar: **```mvn -version```**.
5. Ejecutar:
```Powershell
    mvn clean install
    mvn spring-boot:run
```

# Tests de Integración

## Cómo correrlos

```bash
# 1. Levantar la base de datos local
docker compose up -d

# 2. Correr los tests de integración
mvn test -Pintegration

# 3. (Opcional) Bajar la base de datos al terminar
docker compose down
```

## Otros comandos útiles

```bash
mvn test                # Solo unit tests (H2 en memoria, sin Docker, rápido)
mvn test -Pintegration  # Solo integration tests (requiere Docker)
mvn test -Pall          # Unit + integration juntos (CI/CD completo)
```

---

## Arquitectura del mecanismo

### Visión general

```
┌─────────────────────────────────────────────────────────────┐
│                        mvn test -Pintegration               │
└─────────────────────────────────┬───────────────────────────┘
                                  │
                    activa perfil Maven "integration"
                                  │
              ┌───────────────────▼────────────────────┐
              │          maven-surefire-plugin         │
              │   groups=integration / exclude=unit    │
              │   spring.profiles.active=integration   │
              └───────────────────┬────────────────────┘
                                  │
                    solo corre clases @Tag("integration")
                                  │
              ┌───────────────────▼────────────────────┐
              │         AbstractIntegrationTest        │
              │  @SpringBootTest                       │
              │  @AutoConfigureMockMvc                 │
              │  @ActiveProfiles("integration")        │
              │  @Transactional                        │
              └───────────────────┬────────────────────┘
                                  │
                 levanta contexto Spring completo
                 carga application-integration.properties
                                  │
              ┌───────────────────▼────────────────────┐
              │  application-integration.properties    │
              │  url=jdbc:postgresql://localhost:5432/ │
              │  flyway.enabled=true                   │
              └───────────────────┬────────────────────┘
                                  │
                    Flyway aplica migraciones reales
                                  │
              ┌───────────────────▼────────────────────┐
              │         docker-compose.yml             │
              │         postgres:15-alpine             │
              │         localhost:5432/sip_project     │
              └────────────────────────────────────────┘
```

### Las piezas y su responsabilidad

**`docker-compose.yml`** — levanta un contenedor Postgres real en `localhost:5432`.
Es la única "infraestructura" externa requerida. El desarrollador lo inicia
manualmente antes de correr los tests.

**Perfil Maven `integration`** (`pom.xml`) — cuando se activa con `-Pintegration`,
reconfigura Surefire para que corra exclusivamente los tests marcados con
`@Tag("integration")` e inyecta `spring.profiles.active=integration` como
variable de sistema.

**`application-integration.properties`** — perfil Spring que apunta al Postgres
del docker-compose. Habilita Flyway para que aplique las migraciones reales
antes de que arranquen los tests. El contexto Spring lo carga automáticamente
al detectar el perfil activo `integration`.

**`AbstractIntegrationTest`** — clase base que todos los tests de integración
extienden. Centraliza las cuatro anotaciones clave:
- `@SpringBootTest`: levanta el contexto completo de la aplicación (todos los beans,
  seguridad, repositorios, servicios), a diferencia de los slices parciales como
  `@DataJpaTest` o `@WebMvcTest`.
- `@AutoConfigureMockMvc`: registra el bean `MockMvc` en el contexto, necesario
  para hacer llamadas HTTP simuladas a los controllers.
- `@ActiveProfiles("integration")`: selecciona `application-integration.properties`.
- `@Transactional`: envuelve cada test en una transacción que hace **rollback**
  automático al terminar. Esto mantiene la DB limpia entre tests sin necesidad
  de resetear el contenedor.

**`@Tag("integration")`** en cada clase — etiqueta que Surefire usa para filtrar.
Sin este tag, el test corre también con `mvn test` normal (sin `-Pintegration`),
lo que fallaría porque no hay Docker disponible.

---

## Tips para escribir nuevos tests de integración

### 1. Siempre extender `AbstractIntegrationTest`
```java
@Tag("integration")
class MiNuevoIntegrationTest extends AbstractIntegrationTest {
    // ...
}
```
Sin `extends AbstractIntegrationTest` el test no tiene MockMvc, no apunta
a Postgres y no hace rollback. El `@Tag("integration")` también es obligatorio
para que Surefire lo incluya en el perfil correcto.

### 2. El rollback es automático, pero hay una trampa
`@Transactional` en la clase base hace rollback después de cada test, lo que
mantiene la DB limpia. Sin embargo, si tu código bajo prueba llama a métodos
anotados con `@Transactional(propagation = REQUIRES_NEW)`, esa transacción
interna se commitea igual y el rollback del test no la deshace. En esos casos
hay que limpiar manualmente en un `@AfterEach`.

### 3. Datos de referencia ya existen: no los crees, búscalos
Flyway aplica las migraciones reales al arrancar el contexto, incluyendo el
seed de roles y permisos. Buscalos con el repositorio en lugar de insertarlos:
```java
// Bien
Role devRole = roleRepository.findByName(RoleConstants.DEVELOPER).orElseThrow();

// Mal: puede romper constraints de unicidad o datos existentes
roleRepository.save(new Role("DEVELOPER"));
```

### 4. Usuarios y datos de test: usar valores únicos
Como múltiples tests comparten la misma DB (aunque con rollback), si un test
falla antes del rollback puede dejar datos sucios. Usar valores únicos por
ejecución evita colisiones:
```java
String email = "test+" + System.nanoTime() + "@mail.com";
```

### 5. BigDecimal para campos monetarios
Los campos `NOT NULL` de tipo `BigDecimal` en el modelo deben setearse
explícitamente en el builder. El literal `double` no compila:
```java
// Bien
.tokenPrice(BigDecimal.valueOf(100.00))

// No compila si el campo es BigDecimal
.tokenPrice(100.00)
```

### 6. Separar la responsabilidad del test
- Tests en `service/` → inyectar servicios y repositorios directamente, sin MockMvc.
- Tests en `api/` o `controller/` → usar MockMvc para ejercitar el stack HTTP completo
  (serialización, seguridad, validaciones de request).
- No mezclar: un test de integración de servicio no necesita hacer llamadas HTTP.

### 7. Flujo típico de un test de API con autenticación
```java
@Test
void miTest() throws Exception {
    // 1. Obtener token real haciendo login
    String token = loginYObtenerToken("admin@mail.com", "password");

    // 2. Llamar al endpoint con el token
    mockMvc.perform(get("/api/recurso")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk());
}
```

### 8. Verificar el estado en DB además del HTTP response
Un test de integración tiene acceso a los repositorios. Aprovecharlo para
verificar que los datos realmente se persistieron, no solo que el endpoint
devolvió 200:
```java
mockMvc.perform(post("/api/usuarios").content(body)...)
    .andExpect(status().isCreated());

// Verificar persistencia real
assertTrue(userRepository.findByEmail("nuevo@mail.com").isPresent());
```

# TODO
- [ ] Estructurar la base de datos en Postgres para manejar la relación entre los inversores y los tokens de los proyectos.
- [ ] Implementar los servicios para las acciones específicas de los Inversores (tokens, mercado secundario).
- [x] Manejar los errores de permisos (Exceptions) en Spring.
- [x] Excepciones de Dominio, en vez de devolver true/false, hacer que el servicio lance una excepción propia (ej: InadequatePermissionsException) para que el usuario reciba un error claro.
- [x] Persistencia, configurar el archivo application.properties para que  User y Project se guarden realmente en Postgres (usando H2 para los tests).
- [x] Testear que un Project tenga una relación @ManyToOne real con User en lugar de solo un Long ownerId.
- [ ] Crear un ProjectController para recibir peticiones HTTP reales.
- [ ] ApiResponse, hacer que método success use un constructor o método que acepte el HttpStatus directamente, para que el int status del JSON coincida siempre con el código real de la ResponseEntity.
- [ ] Aplicar la regla "Un admin no puede demotear a otro admin" desde el front-end (revisar el controlador asociado)
- [x] Quitar niveles de anidamiento a JwtAuthenticationFilter.java
- [ ] Limpiar/emprolijar/Simplificar RbacIntegrationTest.java
- [ ] Armar test que asegure que la sumatoria de UserProject.tokensAmount no supere el Project.max_amount_tokens.