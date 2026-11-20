# Auditoría y ajustes — rama `refactor-ajustes`

Backend BUSINÚ (`apps/api`). Fecha: 18 de noviembre de 2026 · **Actualizado el 19 de noviembre** con tus decisiones (sección H) con la rama `refactor-ajustes-2` (sección I) y con la rama `fase-4-6-a-4-8` (sección J).
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
| R4 | §3 Dos turnos | FUERA DE ALCANCE | Decisión de negocio: los turnos son organización del personal y no se modelan en el software |
| R5 | Información verificable | CUMPLE *(antes PARCIAL)* | `creado_por`/`modificado_por` en cada registro, más el historial de cada modificación con autor y fecha (Envers) y `GET /auditoria/{tipo}/{id}` (sección I) |
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
| R17 | Bitácoras conservadas | CUMPLE *(antes PARCIAL)* | Migración `V1__initial_schema.sql` completa. `dev` y `prod` arrancan con Flyway y `ddl-auto=validate`, y los datos sobreviven a los reinicios (sección I) |
| R18 | Número de usuarios | CUMPLE | — |
| R19 | Grupo etario y sexo | CUMPLE | Obligatorios (`@NotEmpty` y la suma debe coincidir con el total). `NO_REPORTA` no se implementó, por decisión de negocio |
| R20 | Origen y destino + **capacidad** | CUMPLE *(antes PARCIAL)* | Capacidad acumulada por tramo |
| R21 | Fecha y viaje | CUMPLE | — |
| R22 | Consolidado de la demanda | CUMPLE *(antes NO CUMPLE)* | `GET /pasajeros/consolidado`, solo para el supervisor (sección I) |
| R23–R28 | §4.3 Hoja de vida y mantenimiento | CUMPLE | N-19 corregido |
| R29–R33 | §4.4 Infraestructura | CUMPLE | — |
| R34 | §4.4 Evidencias | CUMPLE *(antes PARCIAL)* | Evidencias conectadas al registro (`evidenciaIds`); se almacenan en PDF. Una foto JPG/PNG se convierte a PDF al subirla (sección H) |
| R35 | §4.5 Fecha, embarcación y cantidad | CUMPLE *(era REGRESIÓN)* | N-12 corregido |
| R36 | §4.5 Soporte | CUMPLE *(era REGRESIÓN)* | No se modificó la lógica de carga. Probado hasta S3: un PDF válido llega a S3 y, sin acceso, responde 502 controlado. El envío real a S3 no se pudo probar desde este entorno |
| R37 | §4.5 Consumo asociado a la operación | CUMPLE *(antes POSPUESTO)* | Horómetro por motor y viaje, **obligatorio al finalizar y al suspender** (sección H), y consumo entre tanques llenos |
| R38 | §4.5 Observaciones | CUMPLE *(era REGRESIÓN)* | Se registran, se guardan y se devuelven en `POST` y `GET /{id}` |
| R39 | Identificación y rol | CUMPLE | `/auth/me` no expone el hash de la contraseña |
| R40, R41 | §4.6 Documentos y seguridad social | CUMPLE | Documentación por persona en S3 (`documentacion/{ROL}/{cédula}/{TIPO}/`), requisitos faltantes por rol y planilla PILA por mes (sección J) |
| R42, R43 | §4.7 Informe mensual y PQRS | CUMPLE | Informe con las 16 secciones, cierre con cifras congeladas, entrega y PDF; PQRS por formulario público con QR y gestión del supervisor (sección J) |
| R44 | §4.8 Exportación | CUMPLE | Excel y CSV de 8 conjuntos y Excel completo; hoja de vida e informe en PDF (sección J) |
| R45 | §7.1.1 Horario | CUMPLE | De 05:00 a 21:00. La programación de recorridos y los festivos quedan **POSPUESTOS** |
| R46 | La taquilla registra | CUMPLE | N-13 verificado |
| R47 | Guía a bordo | CUMPLE | — |
| R48 | §7.1.7 Suspensión | CUMPLE *(antes PARCIAL)* | Causa tipificada (enum) con detalle y autoridad. El viaje suspendido queda suspendido de forma definitiva y no bloquea la embarcación (sección H) |
| R49 | Sábados y domingos | CUMPLE | Lunes a domingo por decisión de negocio (el anexo §7.1.1 dice L-V) |
| R50 | §7.1.12 Constancia de comunicación | CUMPLE *(antes PARCIAL)* | Solo correo. La estructura está lista; el envío real depende de configurar Amazon SES (SMTP) |
| R51 | §7.2–7.3 Acta, documentos, sustitución | FUERA DE ALCANCE | La sustitución (N-9) se descartó. Los documentos con vigencia se tratarán con §4.6 |

**Resumen (con la rama `fase-4-6-a-4-8`):** 49 CUMPLE y 2 FUERA DE ALCANCE (R4 y R51). No queda ningún requerimiento parcial ni no cumplido, ni ninguna regresión abierta.

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
| **Primer usuario en producción** | Sin él nadie puede entrar en `prod`. Se elige entre la opción (a) o (b) de la sección I |

**Configuración pendiente**

- **R50:** configurar Amazon SES (SMTP): `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT=587`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`, `CORREO_REMITENTE` (identidad verificada en SES) y `CORREO_SUPERVISOR`. Las propiedades de STARTTLS y autenticación ya están en `application.properties`. Hasta entonces, cada constancia queda FALLIDO y se puede reintentar.

**Pospuestos por decisión expresa**

- Evidencias fotográficas de infraestructura.
- Programación de recorridos y festivos.
- `NO_REPORTA`.
- Cobertura extensa de pruebas (H-37).

**Fuera de alcance de esta fase**

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

---

## I. Rama `refactor-ajustes-2` (19 de noviembre)

Rama creada desde `refactor-ajustes` en `3d8fb97`, que incluye tu commit de H-25 y el cambio de carpeta de `ConversorPdf`.

### Decisiones aplicadas

| Tema | Decisión | Resultado |
|---|---|---|
| R22 | Implementar la propuesta; solo supervisor; incluir viajes suspendidos; sexo y edad por franja horaria, sin cruzar con destino | Implementado |
| R5 | Registrar el autor de cada modificación | Implementado |
| R17 | Crear el esquema SQL para Flyway | Implementado |
| R4 | Los turnos no se modelan en el software | FUERA DE ALCANCE |
| H-25 | Revisar tu corrección | Revisado (ver abajo) |
| R50 | Lo configuras tú con Amazon SES y cPanel | Sin cambios de código |
| Límite de intentos de login | No por ahora: cada contratista recibe guía y contraseña | Descartado |

### R22 — `GET /pasajeros/consolidado` (solo SUPERVISOR)

**Parámetros:** `desde` y `hasta` (fechas, ambas incluidas; por defecto, el mes en curso), `embarcacion`, `origen` y `agrupacion` (`DIA`, `SEMANA` o `MES`; por defecto `DIA`).

**Respuesta**, toda en hora de Colombia:

| Campo | Contenido |
|---|---|
| Totales | `totalUsuarios`, `viajes` y `promedioPorViaje` |
| `suspendidos` | Viajes y usuarios de viajes suspendidos. Están **incluidos** en los totales y además se informan aparte |
| Distribuciones | `porSexo`, `porGrupoEtario`, `porOrigen`, `porDestino` y la matriz `origenDestino` |
| **`porFranjaHoraria`** | Para **mañana** (05:00–11:59), **tarde** (12:00–17:59) y **noche** (18:00–21:00): usuarios, **sexo** y **grupo etario** de cada franja |
| `porHora` | El mismo detalle hora a hora, para mirar más fino dentro de una franja |
| `serie` | Usuarios y viajes por día, por semana (empieza el lunes) o por mes |

Cada distribución trae todas las categorías, aunque valgan 0, para que las gráficas sean estables.

**Por qué esas franjas:** dividen la jornada de 05:00 a 21:00 en mañana, tarde y noche, que es como pediste evaluarla. El detalle por hora permite reagrupar después sin cambiar la API.

**Cómo se calcula:** las sumas se hacen en PostgreSQL, sin cargar los registros uno por uno. Sexo, grupo etario y destino se suman por separado y no se cruzan entre sí, como pediste.

### R5 — Autor de cada modificación

1. **En cada registro:** columnas `creado_por` y `modificado_por` (id del Personal del token), con `@CreatedBy` y `@LastModifiedBy`. Las llena el sistema y no el cliente.
2. **Historial completo con Hibernate Envers:** cada transacción que crea, modifica o elimina datos genera una *revisión* (tabla `revisiones`) con fecha y autor. Las tablas `*_aud` guardan el estado de cada registro en cada revisión. Se auditan las 12 entidades.
3. **Consulta: `GET /auditoria/{tipo}/{id}`** (solo SUPERVISOR). Devuelve la lista de cambios de un registro: revisión, fecha, acción (`CREACION`, `MODIFICACION` o `ELIMINACION`), autor (o "Sistema" si no hubo usuario) y `camposModificados` respecto de la revisión anterior. Tipos disponibles: `OPERACION`, `PASAJEROS`, `MANTENIMIENTO`, `ABASTECIMIENTO`, `PERSONAL`, `EMBARCACION`, `MOTOR`, `INFRAESTRUCTURA`, `AYUDA_NAVEGACION`, `SUSPENSION`, `COMUNICACION` y `ADJUNTO`.

**Excepciones deliberadas**

- **El hash de la contraseña** no se copia al historial.
- **El login** (`ultimoAcceso`) no genera revisiones.
- **Seis listas de objetos embebidos no se versionan** dentro del historial: paradas, horómetros y novedades mecánicas de la operación, y repuestos, novedades y recomendaciones del mantenimiento.
  - **Por qué:** es una limitación conocida de Envers con este tipo de listas. Hacía fallar las transacciones que las crean y las modifican a la vez, como el zarpe y la llegada.
  - **Qué se conserva:** el cambio sigue quedando registrado con autor y fecha, porque toda modificación de esas listas incrementa la versión del registro dueño. En ese caso `camposModificados` sale vacío.

### R17 — Migración Flyway

- **`V1__initial_schema.sql`:** 45 tablas (negocio, colecciones, `*_aud` y `revisiones`) con sus restricciones, índices y llaves foráneas.
  - **Cómo se generó:** a partir del esquema real que produce Hibernate.
  - **Cómo se validó:** la aplicación arranca **solo con Flyway** y `ddl-auto=validate`, sin ninguna diferencia.
- **`dev` deja de usar `ddl-auto=create`:** ahora usa Flyway y `validate`, igual que producción. Los datos ya no se borran en cada reinicio.

**Importante para tu entorno local:** si tu base de datos local ya tiene tablas creadas por Hibernate, Flyway no va a aplicar V1 sobre ellas. Debes borrar y volver a crear la base una vez, por ejemplo con `DROP DATABASE businu; CREATE DATABASE businu;`.

**Regla desde ahora:** todo cambio de entidades se acompaña de una migración nueva (`V2__...`). Nunca se edita un V1 ya aplicado.

### H-25 — Revisión de tu corrección

Correcto en lo importante: `rol` y `passwordHash` ya no tienen setter, así que solo cambian por los métodos de negocio (`habilitarAcceso` y `revocarAcceso`).

Quedan setters en `contratoActivo`, `fechaRetiro` y `disponibilidad`, que permitirían saltarse `desactivarPersonal()` y `devolverDisponibilidad()`. Hoy solo `OperacionServicio` usa `setDisponibilidad(false)`, de forma correcta. No es un riesgo actual, solo una puerta abierta, así que no lo modifiqué.

### R50 — Notas para Amazon SES con cPanel (sin cambios de código)

El rebote que describes es típico cuando SES y el correo de cPanel se pisan en el DNS. Recomendaciones:

1. **Registros MX:** no los toques. Deben seguir apuntando al servidor de cPanel, que es el que **recibe** el correo. SES solo **envía**.
2. **DKIM de SES:** agrega solo los **3 registros CNAME** que te da SES. No reemplazan nada de cPanel.
3. **SPF:** debe haber **un solo** registro TXT SPF que incluya a los dos. Por ejemplo: `v=spf1 +a +mx include:amazonses.com ~all`, más el `include` que use cPanel si aplica. Dos registros SPF separados invalidan los dos.
4. **"MAIL FROM" personalizado:** si lo usas en SES, usa un **subdominio** (por ejemplo `envios.tudominio.com`) con su propio MX hacia SES. Nunca el dominio raíz, porque eso es lo que choca con el MX de cPanel.
5. **"Email Routing" en cPanel:** debe estar en *Local Mail Exchanger* si los buzones viven en cPanel.
6. **Remitente:** `CORREO_REMITENTE` debe ser una identidad verificada en SES. La cuenta de SES debe salir del modo *sandbox* para enviar a cualquier destinatario.

### Pruebas

| Prueba | Resultado |
|---|---|
| `./gradlew clean build` y la prueba existente | Correctos (1/1) |
| Batería de regresión (sobre una base creada solo por Flyway) | **109/109** |
| Batería nueva de R22 y R5 | **34/34**. El consolidado se comparó contra sumas hechas directamente en la base: total, viajes, por hora en hora de Colombia, por franja (con sexo y edad que suman lo de cada franja), por origen y destino, suspendidos, series de día, semana (lunes) y mes, filtros y permisos. La auditoría cubre autor de creación y modificación, campos modificados, "Sistema" para los datos de prueba, que el login no genere revisiones y que el hash no esté en el historial |
| Reinicio sobre una base ya migrada | Flyway "up to date" y no se pierden datos |
| Perfil `prod` sobre una base vacía | Arranca, aplica V1 y no carga datos de prueba |
| Correo | No se probó, como pediste |

### Pendiente nuevo detectado

**Primer usuario en producción.** En `prod` el Bootstrap no corre, así que la base queda **sin ningún usuario**. Crear personal exige el rol SUPERVISOR, por lo que nadie podría entrar la primera vez. Hay dos opciones:
- **(a)** Una migración `V2` con el supervisor inicial y una contraseña temporal que se cambia al primer ingreso.
- **(b)** Al arrancar, crear el supervisor solo si no existe ninguno, con datos tomados de variables de entorno.

Cualquiera de las dos se integra bien con la guía y las contraseñas preestablecidas que vas a entregar a los contratistas.

---

## J. Rama `fase-4-6-a-4-8`: §4.6, §4.7 y §4.8 (19 de noviembre)

Rama creada desde `main` en `924a102`, que ya incluye los PR #2 y #3.

### Decisiones aplicadas

| Tema | Decisión |
|---|---|
| Documentación | Un PDF por documento, en una carpeta por persona identificada por su cédula. Sin control de vencimientos ni bloqueo del zarpe: la revisión previa la hacen los contratistas |
| Informe mensual | Endpoint calculado más una entidad que se cierra y se entrega |
| PQRS | Solo por un formulario público con un único QR. El personal no puede crearlas ni modificarlas; solo el supervisor las consulta y registra su gestión |
| Exportaciones | Excel y CSV para los datos, y PDF con plantillas HTML |

### §4.6 Documentación del personal — `/personal/{id}/documentos` (solo SUPERVISOR)

- **Subir un documento:** `POST` multipart con `archivo` (PDF, JPG o PNG; las fotos se convierten a PDF), `tipo`, `periodo` (`yyyy-MM`, solo planilla PILA) y `observaciones`.
- **Ruta en S3:** `documentacion/{ROL}/{cédula}/{TIPO}/{año-mes}/{id}.pdf`. Cada archivo tiene su SHA-256.
- **Tipos** (§7.3.1): cédula, RUT, hoja de vida en formato de función pública, certificado académico, certificados de experiencia, licencia o permiso de tripulante (solo operadores), certificados de competencia, afiliación a seguridad social, planilla PILA (por mes) y otro.
- **Reemplazos:** al subir un documento del mismo tipo (y mes, si es periódico), el anterior queda en el **historial**; no se borra.
- **`GET`:** devuelve los documentos vigentes (`?historial=true` incluye los anteriores), los **requisitos faltantes** según el rol y los meses con planilla PILA.
- **`GET /{documentoId}/url`:** enlace temporal de 5 minutos al PDF.

### §4.7 PQRS — formulario público con un único QR

- **Página: `GET /pqrs`**, la URL que va en el QR, sin token. Es un HTML de un solo archivo pensado para celular y servido por la API.
  - **Campos:** tipo, descripción, embarcadero, fecha del hecho y contacto opcional con la autorización de datos (Ley 1581).
  - **Respuesta:** al enviar, muestra el **número de radicado** (`PQRS-2026-000001`, tomado de una secuencia de la base).
- **Envío: `POST /pqrs/radicar`**, público.
  - **Protecciones contra abuso:** un campo trampa invisible para robots y un límite de 5 envíos por hora por IP, con respuesta 429. La IP se toma del último valor de `X-Forwarded-For`, que es el que no puede falsificar el cliente.
  - **Inmutabilidad:** el texto del ciudadano no se puede modificar.
- **Gestión: `/pqrs/gestion`**, solo SUPERVISOR. Permite listar, ver y gestionar: `RECIBIDA` → `EN_GESTION` → `RESPONDIDA` (exige respuesta) o `TRASLADADA` (exige entidad) → `CERRADA` (no se reabre). Cada paso queda en el historial (R5).

### §4.7 Informe mensual — `/informes/mensual/{yyyy-MM}` (solo SUPERVISOR)

| Método y ruta | Uso |
|---|---|
| `GET` | Estado, texto del supervisor y cifras: **en vivo** mientras es borrador, **congeladas** después del cierre |
| `PUT` | Redacta el resumen de actividades, las conclusiones, las recomendaciones y otros documentos (solo en BORRADOR) |
| `POST /cerrar` | Solo cuando el mes ya terminó y con el texto completo. Congela las cifras (JSON más su huella SHA-256) y bloquea la edición |
| `POST /entregar` | Marca el informe como entregado (fecha y quién lo entregó) |
| `GET /pdf` | El informe en PDF con las **16 secciones del §4.7**. En borrador lleva la marca "BORRADOR" |

**Cifras que incluye:**
- Viajes por estado y por recorrido.
- Pasajeros y su caracterización por franja, con sexo y edad (R22).
- Combustible y consumo entre tanques llenos.
- Estado de las embarcaciones.
- Mantenimientos y alertas: recomendaciones, novedades graves y próximas intervenciones.
- Infraestructura.
- Relación del personal.
- Planillas PILA del mes: quién la tiene y quién no.
- Bitácoras, novedades y PQRS.

**Dónde está el PDF:** se genera a partir de las cifras congeladas cada vez que se pide; no se guarda en S3. Por eso siempre muestra exactamente lo que se cerró.

### §4.8 Exportaciones — `/exportaciones/{conjunto}` (solo SUPERVISOR)

- **Conjuntos:** `viajes`, `pasajeros` (con sexo, grupo etario y destino en columnas), `combustible`, `consumo`, `mantenimientos`, `novedades` (mecánicas, operacionales, clima, suspensiones e infraestructura), `personal`, `pqrs` y `completo` (un Excel con las 8 hojas).
- **Parámetros:** `desde`, `hasta` (por defecto, el mes en curso) y `formato=xlsx|csv`.
- **CSV:** en UTF-8 con BOM, separador `;` y decimales con coma, para que Excel en español lo abra bien.
- **Excel:** encabezado fijo con filtros.
- **Datos personales:** las exportaciones **no incluyen datos de contacto** del personal ni de las PQRS (Ley 1581).
- **Hoja de vida en PDF:** `GET /embarcaciones/{id}/hoja-de-vida/pdf`.

**Tecnología** (Puppeteer sin navegador): Thymeleaf para las plantillas HTML, OpenHTMLtoPDF 1.1.28 para convertirlas a PDF (sobre PDFBox 3) y Apache POI 5.4.1 para Excel. No requiere Chromium en App Runner.

### Migración V2

`V2__documentacion_pqrs_informe.sql` contiene:
- **Tablas:** `documentos_personal`, `pqrs` e `informes_mensuales`, con sus tablas `*_aud`.
- **Secuencia:** `pqrs_consecutivo_seq`.

Se generó a partir del esquema real de Hibernate. La aplicación arranca con Flyway V1 + V2 y `validate` sin diferencias.

### Pruebas

| Prueba | Resultado |
|---|---|
| `./gradlew clean build` y la prueba existente | Correctos (1/1) |
| Baterías de regresión | 109/109 y 34/34 |
| **Batería de esta fase** | **69/69** |

**Cómo se probó S3 sin AWS:** con un servidor S3 local (moto) en este entorno. Así se probaron de punta a punta la subida, la ruta de documentación, la conversión de foto a PDF y la descarga por enlace temporal.

**PDF:** el informe en borrador, el informe entregado y la hoja de vida se generaron y se revisaron visualmente.

**Errores encontrados y corregidos durante las pruebas:**
1. La plantilla usaba una expresión SpEL que no existe en Thymeleaf independiente (OGNL). Daba 500 al generar un PDF con filas.
2. `autorizaDatos` ausente era rechazado por Jackson 3 (`boolean` primitivo).
3. Las columnas de sexo del PDF salían vacías porque las claves del mapa eran enums.

### Pendientes y notas

- **Primer usuario en producción:** la opción recomendada es crearlo al arrancar desde variables de entorno, solo si no existe ningún supervisor. Queda por implementar cuando lo confirmes.
- **Consulta pública del estado de una PQRS por radicado:** no se implementó. Hoy el ciudadano pregunta en la taquilla. Si se quiere, necesita un código de verificación además del radicado, para que nadie consulte PQRS ajenas.
- **Notificación al ciudadano por correo** cuando su PQRS sea respondida: se puede hacer cuando SES esté configurado.
- **Límite de envíos de PQRS:** vive en memoria. Basta con una sola instancia de App Runner; con varias instancias, cada una lleva su propia cuenta.
