# API REST de Avistamientos de Aves

API REST en Java (Spring Boot + H2) para gestionar un registro de avistamientos de aves: CRUD en JSON, códigos de estado HTTP correctos y resumen por especie. Trabajo de la Sesión 17 de Ingeniería de Software 2 (ISW-2).

## Tecnologías

- Java 21
- Spring Boot 4.1.1 (Spring Web MVC, Spring Data JPA, Validation)
- H2 en modo archivo (base de datos embebida, los datos persisten entre reinicios)
- springdoc-openapi 3.0.3 (Swagger UI)
- Maven (se incluye el wrapper `mvnw`, no necesitas instalar Maven)

## Requisitos

- **JDK 21** instalado. Verifica con `java -version`.
- No hay que instalar ninguna base de datos: H2 es embebida y se crea sola.

## Instalación y ejecución

```bash
git clone https://github.com/Rescobarb/avistamientos-api.git
cd avistamientos-api
```

Ejecuta la API desde la carpeta que contiene el `pom.xml`:

| Sistema | Comando |
|---|---|
| Windows (cmd o PowerShell) | `mvnw.cmd spring-boot:run` |
| Linux / macOS | `./mvnw spring-boot:run` |

En Linux/macOS, si sale un error de permisos: `chmod +x mvnw`.

La API queda disponible en **http://localhost:8080**. Al primer arranque se crea la carpeta `data/` con la base de datos y la tabla `avistamientos`, de forma automática.

## Modelo

Recurso `avistamiento`:

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | número | Lo asigna el sistema (no se envía al crear) |
| `especie` | texto | Nombre de la especie, por ejemplo `"colibrí"` (obligatorio) |
| `lugar` | texto | Dónde se observó (obligatorio) |
| `fecha` | texto `AAAA-MM-DD` | Fecha de la observación (obligatoria) |
| `observador` | texto | Quién la registró (obligatorio) |

## Endpoints

| Operación | Petición | Respuesta |
|---|---|---|
| Listar todos | `GET /avistamientos` | `200` con la lista |
| Ver uno | `GET /avistamientos/{id}` | `200`, o `404` si no existe |
| Registrar | `POST /avistamientos` | `201` con el creado, o `400` si faltan datos |
| Actualizar | `PUT /avistamientos/{id}` | `200`, `400` si faltan datos, o `404` si no existe |
| Eliminar | `DELETE /avistamientos/{id}` | `204`, o `404` si no existe |
| Resumen por especie (bonus) | `GET /avistamientos/resumen` | `200` con el conteo por especie |

## Documentación interactiva (Swagger)

Con la API corriendo, puedes probar todos los endpoints desde el navegador:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Especificación OpenAPI (JSON): http://localhost:8080/v3/api-docs

## Base de datos

Se usa **H2 en modo archivo**: los datos se guardan en `data/avistamientos.mv.db` y persisten entre reinicios. La carpeta `data/` no se sube al repositorio (está en `.gitignore`); se crea sola al arrancar.

Para inspeccionarla, abre la consola de H2 en http://localhost:8080/h2-console con:

- JDBC URL: `jdbc:h2:file:./data/avistamientos`
- Usuario: `sa`
- Contraseña: *(vacía)*

Si quieres empezar con la base limpia, detén la API y borra la carpeta `data/`.

## Estructura del proyecto

```
src/main/java/com/isw2/avistamientos_api/
├── AvistamientosApiApplication.java   # Clase principal
├── controller/AvistamientoController.java
├── model/AvistamientoModelo.java      # Entidad JPA con validaciones
└── repository/AvistamientoRepository.java
```

## Uso de inteligencia artificial

Se utilizó **Claude** (Anthropic), modelo **Claude Sonnet 5.5**, como apoyo para revisar el código del controlador y del modelo, configurar H2 y Swagger, y redactar este README. El código fue revisado, probado y entendido por el autor.

## Autor

Raúl Sebastian Escobar Banegas — Ingeniería de Software 2 (ISW-2), Sesión 17.
