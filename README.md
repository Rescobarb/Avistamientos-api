# API REST de Avistamientos de Aves

API REST en Java (Spring Boot + H2) para gestionar un registro de avistamientos de aves: CRUD en JSON, códigos de estado HTTP correctos, validación de datos y resumen por especie. Trabajo de la Sesión 17 de Ingeniería de Software 2 (ISW-2), ampliado en la Sesión 22 con pruebas unitarias y de integración.

## Tecnologías

- Java 21
- Spring Boot 4.1.1 (Spring Web MVC, Spring Data JPA)
- H2 en modo archivo (base de datos embebida, los datos persisten entre reinicios)
- springdoc-openapi 3.0.3 (Swagger UI)
- Pruebas: JUnit 5, Spring Boot Test con MockMvc
- Cobertura: JaCoCo
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
| `especie` | texto | Nombre de la especie, por ejemplo `"colibrí"` |
| `lugar` | texto | Dónde se observó |
| `fecha` | texto `AAAA-MM-DD` | Fecha de la observación |
| `observador` | texto | Quién la registró |

## Regla de negocio (validación)

Un avistamiento es válido si:

- la **especie** tiene entre 3 y 50 caracteres,
- la **fecha** tiene el formato `AAAA-MM-DD` y no es posterior a hoy,
- el **lugar** y el **observador** no están vacíos.

Si no es válido, `POST` y `PUT` responden `400` con un mensaje que indica qué campo falló. Si varios campos fallan a la vez, se reporta el primero en este orden: especie, fecha, lugar, observador. En `PUT`, la validación se hace antes de buscar el id, por lo que un cuerpo inválido da `400` aunque el id no exista.

Ejemplo de respuesta `400`:

```json
{
  "campo": "especie",
  "mensaje": "La especie debe tener entre 3 y 50 caracteres"
}
```

## Endpoints

| Operación | Petición | Respuesta |
|---|---|---|
| Listar todos | `GET /avistamientos` | `200` con la lista |
| Ver uno | `GET /avistamientos/{id}` | `200`, o `404` si no existe |
| Registrar | `POST /avistamientos/crear` | `201` con el creado, o `400` si los datos son inválidos |
| Actualizar | `PUT /avistamientos/actualizar{id}` | `200`, `400` si los datos son inválidos, o `404` si no existe |
| Eliminar | `DELETE /avistamientos/eliminar{id}` | `204`, o `404` si no existe |
| Resumen por especie (bonus) | `GET /avistamientos/resumen` | `200` con el conteo por especie |

Nota: en `actualizar` y `eliminar` el id va pegado a la ruta, sin barra. Por ejemplo: `PUT /avistamientos/actualizar5`.

El `POST` devuelve en el header `Location` la URL para consultar el avistamiento creado (`/avistamientos/{id}`).

## Pruebas

Hay dos niveles de pruebas:

- **Unitarias** (`ValidadorDeAvistamientoTest`, 23 pruebas): verifican la regla de negocio de forma aislada, sin levantar Spring. Los casos se eligieron con partición de equivalencia y valores límite. El reloj se reemplaza por un stub (`RelojFijo`) para que las pruebas de "hoy" y "mañana" no dependan del día real.
- **De integración** (`AvistamientoControllerIntegracionTest`, 17 pruebas): llaman a los endpoints por HTTP con MockMvc y verifican el código de estado, el cuerpo de la respuesta y el efecto (por ejemplo, tras un `DELETE`, el `GET` responde `404`).

Las pruebas de integración usan una base H2 **en memoria** (configurada en `src/test/resources/application.properties`), por lo que no tocan la carpeta `data/`. Cada prueba vacía la tabla antes de ejecutarse y es independiente de las demás.

### Cómo correr las pruebas

| Qué | Windows | Linux / macOS |
|---|---|---|
| Todas las pruebas | `mvnw.cmd test` | `./mvnw test` |
| Solo las unitarias | `mvnw.cmd test -Dtest=ValidadorDeAvistamientoTest` | `./mvnw test -Dtest=ValidadorDeAvistamientoTest` |
| Solo las de integración | `mvnw.cmd test -Dtest=AvistamientoControllerIntegracionTest` | `./mvnw test -Dtest=AvistamientoControllerIntegracionTest` |

### Cómo generar la cobertura

```bash
mvnw.cmd clean verify        # Windows
./mvnw clean verify          # Linux / macOS
```

Se usa JaCoCo. El reporte HTML queda en `target/site/jacoco/index.html`.

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
├── AvistamientosApiApplication.java      # Clase principal
├── controller/
│   ├── AvistamientoController.java       # Endpoints
│   └── ManejadorDeErrores.java           # Traduce los errores de validación a 400
├── model/AvistamientoModelo.java         # Entidad JPA
├── repository/AvistamientoRepository.java
└── validacion/
    ├── ValidadorDeAvistamiento.java      # Regla de negocio, separada del controlador
    ├── AvistamientoInvalidoException.java
    ├── Reloj.java                        # Interfaz para poder sustituir la fecha de hoy
    └── RelojSistema.java                 # Implementación real (LocalDate.now())

src/test/
├── java/com/isw2/avistamientos_api/
│   ├── controller/AvistamientoControllerIntegracionTest.java
│   └── validacion/
│       ├── ValidadorDeAvistamientoTest.java
│       └── RelojFijo.java                # Stub del reloj para las pruebas
└── resources/application.properties      # H2 en memoria solo para pruebas

docs/                                     # Documento de justificación de las pruebas
```

## Uso de inteligencia artificial

Se utilizó **Claude** (Anthropic), modelo **Claude Sonnet 5.5**, como apoyo para redactar este README y guiar la refactorización del validador, el diseño de la tabla de casos y la escritura de las pruebas unitarias y de integración. El código fue revisado, probado y entendido por el autor.

## Autor

Raúl Sebastian Escobar Banegas — Ingeniería de Software 2 (ISW-2)
