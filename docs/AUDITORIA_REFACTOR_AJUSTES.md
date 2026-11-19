# Auditoría y ajustes — rama `refactor-ajustes`

Backend BUSINÚ (`apps/api`). Fecha: 18 de noviembre de 2026 · **Actualizado el 19 de noviembre** con tus decisiones (sección H).
Referencias: Anexo Técnico SA-STTM-024-2026 y auditoría de la rama `refactor` (commit `9cbe446`).

**Convenciones**

- **Fuente de la evidencia**: [E] = comportamiento comprobado ejecutando la aplicación (PostgreSQL 16, perfil `dev`, peticiones HTTP con token real de `/auth/login`); [I] = verificado por inspección del código.
- **Numeración de requerimientos**: la tuya y la de la matriz no coinciden en un caso. Lo que llamas **"R16 – capacidad"** es **R20** en la matriz (origen y destino de los usuarios). **R16** en la matriz es el cargue de combustible en la bitácora. Ambos se trabajaron.

---

## A. Git

| Ítem | Valor |
|---|---|
| Rama de origen | `refactor` (remota), commit `85f7fe6` *"operacion editada"* |
| Nueva rama | `refactor-ajustes`, creada desde `origin/refactor` |
| ¿Existía `refactor-ajustes`? | No, ni en local ni en remoto |
| Estado inicial | Árbol de trabajo limpio (sin cambios locales). La rama activa era `claude/backend-gradle` |
| Dónde se hicieron los cambios | Todos en `refactor-ajustes`, verificado con `git branch --show-current` antes de modificar |
| Commits, push, merge, PR | **Ninguno.** Los cambios quedan sin confirmar en el árbol de trabajo |

El commit `85f7fe6`, posterior a la auditoría, incluía tus correcciones de N-10, N-11 (parcial), N-12, N-13 y N-17. También agregaba el campo `Operacion.cargueCombustible`. Cada una se verificó antes de tocarla (ver sección B).

---

## B. Hallazgos

### Hallazgos que corregiste tú

| ID | Estado anterior | Estado actual | Acción realizada | Evidencia |
|---|---|---|---|---|
| N-10 | Crítico: el operador a bordo no podía iniciar sesión | **Resuelto** | Ninguna: `tieneAcceso()` ya usa `contratoActivo`. Verificado | [E] Login del operador durante un viaje → 200 |
| N-12 | Crítico: SpEL inválido, 500 en `/abastecimiento` | **Regresión persistente → resuelto** | Tu ajuste dejó `hasAnyRole=(...)` (con `=`), que sigue siendo SpEL inválido: el log mostraba `SpelParseException` y la respuesta era 500. Solo se quitó el `=` | [E] Antes: 500. Después: POST 201, GET 200, taquillero 403 |
| N-13 | Crítico: el taquillero no veía operaciones | **Resuelto** | Ninguna. Verificado | [E] `/operaciones/abiertas` con taquillero → 200 |
| N-17 | Alto: `GET /embarcaciones/{id}` daba 500 | **Resuelto** | Ninguna: cambiaste la ruta por `GET /embarcaciones?embarcacionId=`. Verificado | [E] 200 |

### Hallazgos que revisé y corregí

Están ordenados por severidad.

| ID | Estado anterior | Estado actual | Acción realizada | Evidencia |
|---|---|---|---|---|
| N-11 | Crítico: el retirado seguía entrando | **Resuelto** | Tu commit ya revocaba el acceso. Faltaba: (a) impedir desactivar a quien está en una operación `EN_CURSO`; (b) impedir desactivar dos veces. Se conserva el registro y su historial (decisión N-9) | [E] Navegando → 400. Libre → 200, el login da 401, el registro y el historial de operaciones siguen en la base, y la persona no aparece como disponible. Segunda vez → 400 |
| N-26 *(nuevo)* | — | **Resuelto** | Con un token inválido o vencido, el filtro JWT escribía el error **sin fijar el status** (respondía 200) y sin `charset` (tildes dañadas). Ahora responde 401 en UTF-8, con la cabecera `X-Token-Expired: true` cuando el token venció (ya estaba expuesta en CORS) | [E] Token vencido → 401, `X-Token-Expired: true`, "El token ha expirado" |
| N-14 | Alto | **Resuelto** | `Operacion.agregarTripulante` exige `EN_CURSO` | [E] Agregar a una operación FINALIZADA → 400, y la persona sigue disponible |
| N-15 | Alto | **Resuelto** | `eliminarTripulante` valida antes de quitar (mínimo 2 operadores y 1 guía). La disponibilidad solo la cambia el servicio (antes se cambiaba dos veces) | [E] Dejar 1 operador → 400; quitar el único guía → 400; quitar un 3.er operador → 200 y queda disponible |
| N-16 (R20) | Alto | **Resuelto** | Capacidad acumulada por tramo en `Operacion.validarCapacidad`, llamada desde `UsuarioServicio` (ver C, R20) | [E] Ver sección E |
| N-5 | Alto | **Resuelto** | `/personal/tripulacion-disponible` queda abierto al operador. Filtra `contratoActivo && disponibilidad` | [E] Operador → 200; taquillero → 403; el retirado no aparece |
| N-6 | Alto | **Resuelto** | El abordaje se valida con `admiteAbordajeEn` (cualquier embarcadero del recorrido menos el destino), aunque la parada no esté registrada. Responde 400 y no 404 | [E] Abordaje en Calle 22 sin parada registrada → 201 |
| N-18 | Alto | **Resuelto** | `OperacionSpecs` usa `embarcaderoOrigen` y `embarcaderoDestino` | [E] `?origen=` y `?destino=` → 200 |
| N-19 | Medio | **Resuelto** | El responsable del mantenimiento se valida con `isContratoActivo()` y no con la disponibilidad | [E] Retirado → 400; operador navegando en la otra embarcación → 201 |
| N-20 | Medio | **Resuelto** | `POST /motores` y `PATCH /motores/{id}/horometro` quedan solo para SUPERVISOR. La consulta sigue abierta | [E] Taquillero → 403; supervisor → 200 |
| N-21 | Medio: **regresión** (antes existían estos handlers) | **Resuelto** | Se recuperaron los handlers: `DataIntegrityViolationException` → 409, `ObjectOptimisticLockingFailureException` → 409 y `SdkException` (S3) → 502. Este último también se había perdido | [E] Subida de PDF con S3 inaccesible → 502 controlado (antes 500). [I] Los dos 409, porque no se pueden provocar de forma fiable por HTTP |
| N-22 | Medio | **Resuelto** | `GET /auth/me` implementado. Ahora `/auth/login` es la única ruta pública; el stub `GET /auth` se eliminó | [E] Ver sección E |
| N-23 | Medio | **Resuelto** | Si la parada ya está registrada, el abordaje no puede ser anterior a la llegada a ella | [E] → 400 |
| N-24 | Medio | **Resuelto** | Nueva `ForbiddenException` (403) en las validaciones de rol de los servicios: operación, pasajeros, mantenimiento, combustible, infraestructura, suspensiones y comunicaciones | [E] Infraestructura y pasajeros: 400 → 403 |
| N-25 | Medio | **Resuelto** | `gradlew` con permiso de ejecución (`100644` → `100755`) | [I] `git diff --summary`: *mode change* |
| N-7 | Bajo | **Resuelto** | El personal de prueba queda vinculado hace 1 mes, y no en una fecha futura | [E] 0 registros con fecha futura |

### Hallazgos fuera de esta fase

| ID | Estado | Motivo |
|---|---|---|
| N-9 | **Descartado** | Decisión de negocio: no se elimina personal, se conserva el historial |
| H-37 / H-36 | **Fuera de alcance** | No se amplía la cobertura de pruebas automatizadas. Se ejecutó la prueba existente y una batería funcional por HTTP (sección E) |
| H-25 (`@Setter` en `Personal`) | **Pendiente (bajo)** | No se tocó, para no cambiar el código que ya funciona. Ningún servicio nuevo usa los setters de rol o contraseña |

---

## C. Matriz de requerimientos R1–R51

| # | Requerimiento | Estado | Observación |
|---|---|---|---|
| R1 | Tres embarcaderos | CUMPLE | — |
| R2 | Betancí y Yapel | CUMPLE | — |
| R3 | Estructura del personal | CUMPLE | — |
| R4 | §3 Dos turnos | NO CUMPLE | No se pidió en esta fase y no se implementó |
| R5 | Información verificable | PARCIAL | Hay versión y fechas; no queda registro del **autor** de cada modificación (`@CreatedBy`) |
| R6 | Fecha y embarcación | CUMPLE | — |
| R7 | Ingreso y salida por embarcadero | CUMPLE | — |
| R8 | Recorrido y estaciones | CUMPLE | N-6 corregido |
| R9 | Estado de la nave | CUMPLE | — |
| R10 | Verificación previa | CUMPLE | — |
| R11 | Pasajeros por viaje | CUMPLE | — |
| R12 | Clima | CUMPLE | — |
| R13 | Condiciones mecánicas | CUMPLE | — |
| R14 | Tripulación | CUMPLE | N-14 y N-15 corregidos |
| R15 | Novedades y observaciones | CUMPLE | — |
| R16 | §4.1 Cargue de combustible en la bitácora | CUMPLE *(antes NO CUMPLE)* | Al zarpar, los abastecimientos hechos desde el viaje anterior quedan asociados al viaje. `cargueCombustible` y `cargues[]` aparecen en la bitácora, y el abastecimiento informa su `operacionId` |
| R17 | Bitácoras conservadas | PARCIAL | Las consultas funcionan. `V1__initial_schema.sql` sigue vacío; las migraciones son prerrequisito de producción, fuera de esta fase |
| R18 | Número de usuarios | CUMPLE | — |
| R19 | Grupo etario y sexo | CUMPLE | Obligatorios (`@NotEmpty` y la suma debe coincidir con el total). `NO_REPORTA` no se implementó, por decisión de negocio |
| R20 | Origen y destino + **capacidad** | CUMPLE *(antes PARCIAL)* | Capacidad acumulada por tramo |
| R21 | Fecha y viaje | CUMPLE | — |
| R22 | Consolidado de la demanda | NO CUMPLE — **pendiente de aprobación** | Propuesta en la sección F |
| R23–R28 | §4.3 Hoja de vida y mantenimiento | CUMPLE | N-19 corregido |
| R29–R33 | §4.4 Infraestructura | CUMPLE | — |
| R34 | §4.4 Evidencias | CUMPLE *(antes PARCIAL)* | Evidencias conectadas al registro (`evidenciaIds`); se almacenan en PDF. Una foto JPG/PNG se convierte a PDF al subirla (sección H) |
| R35 | §4.5 Fecha, embarcación y cantidad | CUMPLE *(era REGRESIÓN)* | N-12 corregido |
| R36 | §4.5 Soporte | CUMPLE *(era REGRESIÓN)* | No se modificó la lógica de carga. Probado hasta S3: un PDF válido llega a S3 y, sin acceso, responde 502 controlado. El envío real a S3 no se pudo probar desde este entorno |
| R37 | §4.5 Consumo asociado a la operación | CUMPLE *(antes POSPUESTO)* | Horómetro por motor y viaje, **obligatorio al finalizar y al suspender** (sección H), y consumo entre tanques llenos |
| R38 | §4.5 Observaciones | CUMPLE *(era REGRESIÓN)* | Se registran, se guardan y se devuelven en `POST` y `GET /{id}` |
| R39 | Identificación y rol | CUMPLE | `/auth/me` no expone el hash de la contraseña |
| R40, R41 | §4.6 Documentos y seguridad social | FUERA DE ALCANCE | Fase posterior |
| R42, R43 | §4.7 Informe mensual y PQRS | FUERA DE ALCANCE | Fase posterior |
| R44 | §4.8 Exportación | FUERA DE ALCANCE | Fase posterior |
| R45 | §7.1.1 Horario | CUMPLE | De 05:00 a 21:00. La programación de recorridos y los festivos quedan **POSPUESTOS** |
| R46 | La taquilla registra | CUMPLE | N-13 verificado |
| R47 | Guía a bordo | CUMPLE | — |
| R48 | §7.1.7 Suspensión | CUMPLE *(antes PARCIAL)* | Causa tipificada (enum) con detalle y autoridad. El viaje suspendido queda suspendido de forma definitiva y no bloquea la embarcación (sección H) |
| R49 | Sábados y domingos | CUMPLE | Lunes a domingo por decisión de negocio (el anexo §7.1.1 dice L-V) |
| R50 | §7.1.12 Constancia de comunicación | CUMPLE *(antes PARCIAL)* | Solo correo. La estructura está lista; el envío real depende de configurar Amazon SES (SMTP) |
| R51 | §7.2–7.3 Acta, documentos, sustitución | FUERA DE ALCANCE | La sustitución (N-9) se descartó. Los documentos con vigencia se tratarán con §4.6 |

**Resumen:** 41 CUMPLE, 2 PARCIAL (R5 y R17), 2 NO CUMPLE (R4 y R22, este último pendiente por decisión) y 6 FUERA DE ALCANCE (R40–R44 y R51). No queda ninguna regresión abierta.

### Detalle de lo implementado

**R20 / "R16 capacidad".** `Operacion.validarCapacidad(nuevo)` recorre los tramos del viaje en el orden en que se navegan.
- Al zarpar de cada embarcadero, a bordo = abordados en ese embarcadero o antes − los que tienen destino en ese embarcadero o antes.
- El registro nuevo ya está en la lista, pero se cuenta una sola vez.
- Se valida cada tramo. Así también se cubre un abordaje registrado tarde en un embarcadero anterior.
- El mensaje indica cuántos pueden abordar todavía.
- Se conserva la validación anterior por abordaje en el constructor de `Usuario`.

**R37 (horómetro y consumo).** El diseño ya estaba previsto en el código: `Motor.horometroActual`, `AbastecimientoCombustible.tanqueLleno` (comentado como *"solo entre dos tanques llenos se puede calcular el consumo"*) y el campo `horometro` comentado en el abastecimiento.
- **Horómetro.** Nuevo `@Embeddable LecturaHorometro` (motor, lectura inicial, lectura final) en la tabla `operacion_horometros`.
  - Al zarpar, se guarda la lectura de cada motor: la enviada en `lecturasHorometro` o, si no se envía, la última conocida del motor.
  - Al finalizar o suspender, se registran las lecturas finales (opcionales). Se valida que sean ≥ la inicial y que el motor pertenezca a la embarcación, y se actualiza `Motor.horometroActual`.
  - La operación expone `horometros[]`, `horasMotor` (suma de los motores, solo si hay todas las lecturas finales) y `horasNavegacion` (llegada − zarpe).
- **Consumo**, en `GET /abastecimiento/consumo?embarcacionId=`. Se usa el método de tanque lleno a tanque lleno, sin fórmulas inventadas:
  - Lo cargado después de un llenado (cargues parciales + el siguiente llenado) es lo consumido en los viajes cerrados de ese periodo.
  - Devuelve galones, viajes, horas de navegación, horas de motor, galones por viaje, galones por hora de navegación y galones por hora-motor.
  - Una tasa se omite cuando no hay con qué dividirla.
- El campo heredado `horometroFinal` (una sola lectura, sin indicar el motor) **se conserva** para no romper nada.

**R16.** `OperacionServicio.crear` asocia al viaje los abastecimientos de la embarcación que aún no están asociados y que se registraron entre el cierre del viaje anterior y el zarpe. Con esto se completa el campo `cargueCombustible` que habías agregado.

**R48.** Nuevo `@Embeddable MotivoSuspension`, con el mismo patrón que `NovedadMecanica`: causa de un catálogo, detalle y validación en el constructor.
- **Causas** (`CausaSuspension`, tomadas del §7.1.7): `INSTRUCCION_AUTORIDAD`, `CONDICION_CLIMATICA`, `NIVEL_RIO`, `RIESGO_INUNDACION_CRECIENTE`, `FALLA_TECNICA_MECANICA` y `OTRA`.
- **Validaciones:** con `OTRA`, el detalle es obligatorio; con `INSTRUCCION_AUTORIDAD`, la autoridad es obligatoria.
- **Historial:** nueva entidad `SuspensionEmbarcacion` (embarcación, viaje opcional, motivo, inicio, fin, observación del levantamiento, quién la registra y quién la levanta).
  - Cada suspensión es un registro nuevo y no se sobrescribe.
  - Solo puede haber una vigente por embarcación.
  - Con una suspensión vigente, la embarcación no puede zarpar.
- **Suspender un viaje** (`POST /operaciones/{id}/suspender`) ahora exige `causa`.
  - Guarda el motivo tipificado en la operación (el texto `motivoSuspension` se conserva) y crea la suspensión vigente de la embarcación.
  - El campo `motivo` antiguo se acepta como detalle.
- **Endpoints nuevos:** `GET/POST /embarcaciones/{id}/suspensiones` y `POST /embarcaciones/{id}/suspensiones/{sid}/levantar`.

**R50.** El proyecto no tenía ningún servicio de correo. Se agregó `spring-boot-starter-mail` con `CorreoServicio` (SMTP). Nueva entidad `ComunicacionFalla`, que guarda:
- Fecha de registro y de envío, destinatario y asunto.
- Referencia a una `OPERACION`, `SUSPENSION` o `MANTENIMIENTO` (se valida que exista).
- Estado `PENDIENTE` / `ENVIADO` / `FALLIDO`, el `Message-ID` del correo, el error, los intentos y quién la registró.

La constancia se guarda PENDIENTE **antes** de enviar y pasa a ENVIADO **solo** si el servidor SMTP aceptó el mensaje. Si no hay servidor configurado o falla el envío, queda FALLIDO con la causa y se puede reintentar. Endpoints: `POST /comunicaciones`, `POST /comunicaciones/{id}/reintentar` y `GET /comunicaciones?referenciaTipo=&referenciaId=`.

**Días de operación.** `operacion.dias` incluye los 7 días, y el valor por defecto de `HorarioOperacion` también. Se mantiene la franja de 05:00 a 21:00 y la tolerancia de cierre.

---

## D. Cambios realizados

**Archivos modificados (32)**

| Área | Archivos |
|---|---|
| Build y config | `build.gradle` (+ mail), `gradlew` (permiso), `application.properties` (días, correo), `Bootstrap`, `HorarioOperacion`, `SecurityConfig`, `JWTAuthorizationFilter` |
| Controladores | `AbastecimientoControlador` (SpEL, `/consumo`), `AuthControlador` (`/me`), `MotorControlador` (roles), `PersonalControlador` (tripulación disponible) |
| Entidades | `Operacion` (tripulación, capacidad, horómetro, cargues, suspensión tipificada), `AbastecimientoCombustible` (`operacion`), `Mantenimiento` (responsable) |
| Servicios | `OperacionServicio`, `AbastecimientoServicio`, `AuthServicio`, `PersonalServicio`, `UsuarioServicio`, `MantenimientoServicio`, `InfraestructuraServicio` |
| DTOs | `OperacionResponse` (campos al final), `AbastecimientoResponse` (`operacionId` al final), `CrearOperacionRequest` y `FinalizarOperacionRequest` (lecturas opcionales), `SuspenderOperacionRequest` (`causa`) |
| Otros | `GlobalExceptionHandler` (409/502), `OperacionMapper`, `UsuarioMapper`, `OperacionSpecs`, `AbastecimientoRepositorio`, `OperacionRepositorio` |

**Archivos nuevos (22)**

| Tipo | Archivos |
|---|---|
| Entidades y embebibles | `LecturaHorometro`, `MotivoSuspension`, `SuspensionEmbarcacion`, `ComunicacionFalla` |
| Enums | `CausaSuspension`, `EstadoEnvio`, `TipoReferenciaFalla` |
| Servicios | `SuspensionServicio`, `CorreoServicio`, `ComunicacionServicio` |
| Controladores | `SuspensionControlador`, `ComunicacionControlador` |
| Repositorios | `SuspensionEmbarcacionRepositorio`, `ComunicacionFallaRepositorio` |
| DTOs | `LecturaHorometroRequest`, `ConsumoPeriodoResponse`, `SuspenderEmbarcacionRequest`, `LevantarSuspensionRequest`, `SuspensionResponse`, `ComunicacionRequest`, `ComunicacionResponse` |
| Excepciones | `ForbiddenException` |

**Migraciones:** no se agregaron ni modificaron (`V1__initial_schema.sql` sigue vacío). En `dev`, Hibernate crea el esquema. Para la migración futura hay que incluir:
- **Tablas nuevas:** `operacion_horometros`, `suspensiones_embarcacion` y `comunicaciones_falla`.
- **Columnas nuevas:** `operaciones.causa_suspension`, `operaciones.detalle_suspension`, `operaciones.autoridad_suspension` y `abastecimientos_combustible.operacion_id`.

**Cambios de contrato para el frontend** (`apps/web` aún no consume la API):
1. `POST /operaciones/{id}/suspender` exige `causa`.
2. `/auth/**` ya no es público; solo `/auth/login`. `GET /auth` se eliminó.
3. Las respuestas de la operación y del abastecimiento tienen campos nuevos **al final**.
4. Un token inválido o vencido responde 401, y no 200.

**Configuración nueva** (variables de entorno): `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`, `CORREO_SUPERVISOR` y `CORREO_REMITENTE`.

---

## E. Pruebas

| Prueba | Resultado |
|---|---|
| `./gradlew clean build` | **BUILD SUCCESSFUL** |
| Pruebas existentes (`ApiApplicationTests.contextLoads`) | **1/1**, antes y después de los cambios |
| Arranque Spring Boot, perfil `dev` | OK, con y sin servidor de correo configurado |
| Perfil `prod` | No probado: no hay configuración de producción (fuera de alcance) |
| Batería funcional HTTP (login real, base limpia) | **96/96 casos OK**, más 8 casos complementarios |

**Endpoints comprobados [E]**

| Endpoint | Casos |
|---|---|
| `/auth/login` | Funciona |
| `/auth/me` | Con token → 200 sin hash; sin token → 401; token inválido → 401; token vencido → 401 con `X-Token-Expired`; ignora parámetros del cliente |
| `/operaciones/abiertas` | Taquillero → 200 |
| `/personal/tripulacion-disponible` | Operador → 200; taquillero → 403; excluye retirados y ocupados |
| `/pasajeros` | Capacidad, R19, N-6, N-23, roles |
| `/abastecimiento` | POST y GET por id → 201/200 con observaciones; listado → 200; taquillero → 403; soporte reutilizado → 400; `/consumo` |
| `/motores` | Roles |
| `/embarcaciones` | `?embarcacionId=` y hoja de vida |
| `/mantenimientos` | N-19 y roles |
| `/embarcaciones/{id}/suspensiones` | Casos de la tabla de reglas |
| `/comunicaciones` | Casos de la tabla de reglas |
| `/adjuntos` | PDF válido con S3 inaccesible → 502 |

**Reglas de negocio comprobadas [E]**

| Regla | Resultado |
|---|---|
| Acceso del operador durante un viaje | Puede iniciar sesión (N-10) |
| Desactivación de personal | No se puede si está navegando; después el login da 401, el historial se conserva y la persona no aparece como disponible |
| Capacidad acumulada | Embarcación de 40: abordan 40 en Rancho Grande (10 con destino Calle 22). Un pasajero más en Rancho Grande → 400. En Calle 22: 11 → 400 ("máximo 10"); 10 → 201. Total del viaje: 50, sin contar dos veces |
| Tripulación | Se conservan 2 operadores y 1 guía; no se puede agregar a una operación cerrada; la disponibilidad se devuelve |
| Fin de semana | Zarpe un **sábado** 08:00 → 201; un **domingo** 08:00 → 201; las 04:30 siguen rechazadas |
| Horómetro | Lecturas iniciales y finales por motor, final < inicial → 400, motor de otra embarcación → 400. Se actualiza `Motor.horometroActual`. 2 viajes con 1 h de motor cada uno (por motor) → `horasMotor` 2,0 y 2,0 |
| Consumo | Llenado de 60 gal, parcial de 5 gal, 2 viajes y llenado de 20 gal → **25 gal**, 2 viajes, 4 h-motor, **6,25 gal/h-motor**, 12,5 gal/viaje y 1,58 h de navegación |
| Cargues en la bitácora | El llenado inicial queda en el 1.er viaje y el parcial en el 2.º, sin repetirse |
| Suspensión | Exige la causa; la autoridad es obligatoria si la ordena una autoridad; la tripulación queda libre; no se zarpa con una suspensión vigente; levantar exige observación y rol (taquillero → 403); no se levanta dos veces; no se crea una segunda vigente; el historial se acumula |
| Correo | Con SMTP local → `ENVIADO` con `Message-ID` (el servidor recibió el mensaje). SMTP caído → `FALLIDO` con la causa e `intentos = 1`; al reintentar con SMTP activo → `ENVIADO`, `intentos = 2`. Sin servidor configurado → `FALLIDO` ("no está configurado"). Un correo enviado no se reenvía. Referencia inexistente → 404. Taquillero → 403 |

**Errores pendientes:** ninguno conocido en lo intervenido.

**Lo que no se pudo probar en este entorno:**
- La subida real a S3 (no hay acceso ni credenciales).
- El envío por un proveedor SMTP real (se probó con un servidor SMTP local).
- Los 409 de bloqueo optimista e integridad, que se verificaron por inspección.

---

## F. R22 — Propuesta funcional del consolidado de la demanda (sin implementar)

**Qué pide el anexo (§4.2):** *"consolidar la información relacionada con: número de usuarios transportados, edad o grupo etario, sexo, embarcadero de origen, destino, fecha y, cuando resulte pertinente, viaje o recorrido correspondiente… de manera que permita establecer el comportamiento de la demanda y las características generales de uso del servicio"*.

**Lo que ya existe.** Cada abordaje (`Usuario`) guarda la fecha y hora, el viaje, el embarcadero de abordaje, el total y tres distribuciones (por sexo, por grupo etario y por destino). `GET /pasajeros` filtra y pagina registros **individuales**: por embarcación, abordaje, destino, rango de cantidad y un día. **No suma nada.**

**Lo que falta:** agregados por periodo y por dimensión.

**Limitación del modelo que conviene conocer:** sexo, grupo etario y destino se guardan como **distribuciones independientes** de cada abordaje. Por eso se puede sumar cada dimensión por separado, pero **no** cruzar dimensiones, por ejemplo "mujeres que van a Calle 22". El cruce origen × destino **sí** es posible, porque cada registro tiene su embarcadero de abordaje y su distribución por destino.

**Propuesta: un endpoint**

```
GET /pasajeros/consolidado?desde=2026-11-01&hasta=2026-11-30
    &embarcacion=BETANCI        (opcional)
    &origen=RANCHO_GRANDE       (opcional)
    &agrupacion=DIA|SEMANA|MES  (opcional, por defecto DIA)
```

Respuesta (todas las fechas en hora de Colombia):

```json
{
  "periodo": { "desde": "2026-11-01", "hasta": "2026-11-30" },
  "totalUsuarios": 4210,
  "viajes": 380,
  "promedioPorViaje": 11.1,
  "porSexo": { "FEMENINO": 2190, "MASCULINO": 2020 },
  "porGrupoEtario": { "PRIMERA_INFANCIA": 120, "...": 0 },
  "porOrigen": { "RANCHO_GRANDE": 1900, "CENTRO_CALLE_22": 700, "CENTRO_VERDE": 1610 },
  "porDestino": { "...": 0 },
  "origenDestino": [ { "origen": "RANCHO_GRANDE", "destino": "CENTRO_VERDE", "usuarios": 1500 } ],
  "porFranjaHoraria": [ { "hora": 6, "usuarios": 410 } ],
  "serie": [ { "fecha": "2026-11-03", "usuarios": 190, "viajes": 18 } ]
}
```

La consulta se haría con agregaciones en la base de datos sobre `usuarios`, `usuarios_sexo`, `usuarios_grupo_etario`, `usuarios_destino` y `operaciones`, sin cargar los registros uno a uno. Serviría también de base para el informe mensual (§4.7) y la exportación (§4.8) de la fase siguiente.

**Decisiones que quedan pendientes de tu aprobación**

1. **Franjas horarias:** ¿por hora (5, 6 … 20), por bloques (mañana, mediodía, tarde, noche) o por los dos turnos de 8 h?
2. **Agrupación de la serie:** ¿basta con día, semana y mes?
3. **Viajes suspendidos:** ¿sus pasajeros cuentan en la demanda? Propuesta: sí, pero informados aparte.
4. **Cruces entre dimensiones** (sexo × destino, etc.): con el modelo actual no se pueden obtener. ¿Se necesitan? Si es así, habría que cambiar cómo registra el taquillero.
5. **Acceso:** ¿solo SUPERVISOR, o también los operadores?

---

## G. Pendientes

**Sin resolver (dentro del alcance)**

| Requerimiento | Qué falta |
|---|---|
| **R22** | Se deja así por ahora (decisión del 19 de noviembre). La propuesta de la sección F queda como referencia |
| **R5** | Registro del autor de cada modificación (`@CreatedBy` / `@LastModifiedBy`) |
| **R17** | Migración V1 con el esquema completo (prerrequisito de producción) |
| **R4** | Turnos (no pedido en esta fase) |
| **H-25** | `@Setter` a nivel de clase en `Personal` (bajo) |

**Configuración pendiente**

- **R50:** configurar Amazon SES (SMTP): `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT=587`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`, `CORREO_REMITENTE` (identidad verificada en SES) y `CORREO_SUPERVISOR`. Las propiedades de STARTTLS y autenticación ya están en `application.properties`. Hasta entonces, cada constancia queda FALLIDO y se puede reintentar.

**Pospuestos por decisión expresa**

- Evidencias fotográficas de infraestructura.
- Programación de recorridos y festivos.
- `NO_REPORTA`.
- Cobertura extensa de pruebas (H-37).

**Fuera de alcance de esta fase**

- R40–R44: §4.6, §4.7 y §4.8.
- R51.
- N-9.

---

## H. Ajustes por las decisiones del 19 de noviembre

| Decisión | Cambio | Evidencia |
|---|---|---|
| **R22:** dejarlo así | Sin cambios | — |
| **R34/R36:** todo en PDF; si llega una imagen, convertirla | `AdjuntoServicio` acepta PDF, JPG o PNG, reconociendo el formato por la firma real del archivo. El nuevo `ConversorPdf` (Apache PDFBox 3.0.5) convierte la foto en un PDF de una página tamaño carta, con margen, sin deformar la imagen y orientado según la foto (un JPG se incrusta sin recomprimir). Se guarda en S3 como `application/pdf`, con nombre `.pdf` y SHA-256 del PDF. Aplica a **todos** los soportes: combustible, mantenimiento e infraestructura. Además, las evidencias quedan conectadas al registro de infraestructura (`evidenciaIds` en el request y `evidencias` en la respuesta) | [E] Registro con 2 evidencias → 201, se conservan al consultar; evidencia ya usada o de otra persona → 400; sin evidencias → 201. Carga: JPG válido → convierte y llega a S3 (502 aquí, sin S3); JPG dañado → 400 sin tocar S3; texto → 400; PDF → sigue igual. Conversión probada aparte: JPG 1600×1200 → PDF horizontal de 1 página, PNG 800×1200 → PDF vertical |
| **R37:** horómetro obligatorio | `lecturasHorometro` es obligatorio (`@NotEmpty`) al finalizar y al suspender, con una lectura por **cada** motor; el dominio también lo exige (`exigirHorometroCompleto`). La lectura al zarpar sigue opcional: si no se envía, se toma la última del motor. El `Bootstrap` registra horómetros en los viajes de prueba | [E] Sin lecturas → 400; con un solo motor → 400 ("Falta la lectura del horómetro del motor …"); con ambos → 200, `horasMotor` 2,0 |
| **R48:** un viaje suspendido queda suspendido y ya | Suspender un viaje ya **no** crea una suspensión de la embarcación ni bloquea el siguiente zarpe, y no hay nada que levantar. La causa tipificada queda en la operación (`suspension`, `motivoSuspension`), y el historial se consulta con `GET /operaciones?embarcacion=…&estado=SUSPENDIDA`. Se quitó `operacion` de `SuspensionEmbarcacion`. **Se conserva, por decisión tuya**, la suspensión de una embarcación **en el muelle** (`/embarcaciones/{id}/suspensiones`): por ejemplo, una orden de la autoridad antes de zarpar. Bloquea los zarpes hasta que se levanta y deja su historial | [E] Suspender un viaje → el historial de la embarcación queda vacío y la embarcación vuelve a zarpar → 201; el viaje suspendido no se reabre (400). Suspensión en el muelle: bloquea el zarpe hasta levantarla |
| **R50:** no probar con servidor de correo | No se volvió a probar el envío. Se agregaron las propiedades para Amazon SES (STARTTLS obligatorio y autenticación), que solo se activan cuando se configura `SPRING_MAIL_HOST` | — |

**Pruebas de esta actualización:** `./gradlew clean build` correcto; prueba existente 1/1; batería HTTP **109/109** (sin la sección de correo). La batería usa una hora de referencia fija dentro del horario (14:30, hora de Colombia), porque las validaciones de horario rechazan con razón las horas reales fuera de él.

**Archivos de esta actualización:**
- **Nuevo:** `ConversorPdf`.
- **Modificados:** `build.gradle` (PDFBox), `application.properties` (SES), `AdjuntoServicio`, `InfraestructuraServicio`, `OperacionServicio`, `SuspensionServicio`, `Operacion`, `SuspensionEmbarcacion`, `Bootstrap`, `CrearRegistroInfraestructuraRequest`, `RegistroInfraestructuraResponse`, `FinalizarOperacionRequest`, `SuspenderOperacionRequest`, `SuspensionResponse` y `SuspensionEmbarcacionRepositorio`.
- **Migración futura:** la columna `operacion_id` ya no existe en `suspensiones_embarcacion`.

**Cambio de contrato para el frontend:** `POST /operaciones/{id}/finalizar` y `/suspender` exigen `lecturasHorometro` con todos los motores. `POST /adjuntos` acepta también fotos JPG/PNG.
