# BUSINÚ – API (Spring Boot 4.1 · Java 21 · Gradle · PostgreSQL 16)

## Requisitos
- JDK 21
- PostgreSQL 16 con una base `businu` (usuario/clave por defecto `businu`/`businu`)

Variables de entorno opcionales: `DB_URL`, `DB_USER`, `DB_PASSWORD`.

## Comandos
```bash
./gradlew test       # pruebas unitarias
./gradlew bootRun    # levanta la API; Flyway crea/actualiza el esquema
```
En Windows: `gradlew.bat test`.

Las pruebas que levantan la app o usan PostgreSQL solo corren si está definida `DB_URL`
(p. ej. `jdbc:postgresql://localhost:5432/businu`).

## Estructura (`com.lau.api`)
| Carpeta | Contenido |
|---|---|
| `config` | Auditoría JPA y reloj en hora de Colombia |
| `controllers` | Endpoints REST (pendiente) |
| `domain` | Entidades JPA: flota, hoja de vida (4.3), usuarios transportados (4.2), personal y usuarios del sistema (4.6), catálogos |
| `domain/enums` | Enumeraciones del dominio |
| `dto` | Objetos de entrada/salida (p. ej. `ProyeccionServicio`) |
| `exceptions` | `ReglaNegocioException` |
| `mappers` | Conversión entidad ↔ DTO (pendiente) |
| `repositories` | Repositorios Spring Data JPA |
| `services` | Calendario operativo, cálculo del próximo servicio de 100 h |

El esquema lo controla Flyway (`src/main/resources/db/migration`); Hibernate solo lo valida.
