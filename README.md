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

# Uso
## Pruebas locales
### Test Unitarios
Ejecutar Todos menos "IntegrationTest":
```bash
    mvn test -Dtest=!*IntegrationTest
```

### Tests Integracion
1. Levantar postgresdb:
```bash
docker-compose up
```
2. Ejecutar:
```bash
    mvn test -Dtest=*IntegrationTest
```

### Tests Especificos
Ejecutar:
```bash
    mvn test -Dtest=AccessControlServiceTest
```

### Postman y base de datos en disco
1. Levantar postgresdb:
```bash
docker-compose up
```
2. Correr la app con perfil local:
```bash
mvn spring-boot:run "-Dspring-profiles.active=local"
```
3. Correr el suite de test en postman.

### Unit testing con Hibernate
```bash
    mvn clean test -Dspring-profiles.active=local
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