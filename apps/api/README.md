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

## Módulos (`com.lau.api`)
| Paquete | Contenido |
|---|---|
| `compartido` | `BaseEntity` (UUID, versión, auditoría), calendario operativo con festivos de Colombia |
| `catalogos` | Embarcaderos y catálogo de ítems (repuestos/insumos y novedades) |
| `flota` | Embarcación (patente única) y motores con horómetro |
| `mantenimiento` | Hoja de vida (4.3) y proyección del servicio de 100 h |
| `pasajeros` | Registro de usuarios transportados (4.2), cuadrícula grupo etario × sexo |
| `personal` | Personal, usuarios del sistema, documentos y requisitos por cargo (4.6) |

El esquema lo controla Flyway (`src/main/resources/db/migration`); Hibernate solo lo valida.
