# Frontend BUSINÚ · Migración a React e integración con la API

Rama: `front/diseño-inicial` (desde `dev`). Código en `apps/web`.
Insumos: plantillas web 00–22 (PDF y proyecto HTML exportado) y el backend de `apps/api`.

---

## A. Arquitectura implementada

### Tecnologías

| Uso | Tecnología | Nota |
|---|---|---|
| Base | React 19, TypeScript 6, Vite 8 | Se conservó la configuración existente de `apps/web` |
| Estilos | **Tailwind CSS v4** (`@tailwindcss/vite`) | Tokens del sistema de diseño en `src/styles/index.css` (`@theme`) |
| Rutas | React Router 7 | Carga diferida por módulo (`lazy`) |
| Datos del servidor | TanStack Query 5 | Caché, reintentos e invalidación tras cada escritura |
| HTTP | ky 2 | Ya estaba instalada; cumple el papel de Axios (instancia compartida, hooks) |
| Formularios | React Hook Form + Zod, y estado controlado | Validaciones idénticas a las del backend |
| Iconos | lucide-react | Son los mismos íconos que usan las plantillas HTML |
| Gráficos | Componentes SVG propios | Sin dependencias; reproducen los gráficos de las plantillas |
| Tipografía | Irish Grover (logo), Huninn (títulos, botones, campos), Nunito (texto) | Como define la plantilla 00 |

**Dependencias retiradas** porque no se usaban o chocaban con el diseño:
- Mantine y sus paquetes, y `@tabler/icons-react`: imponían otra apariencia.
- `dayjs`: se usa `Intl` con la zona `America/Bogota`.
- `dexie`, `vite-plugin-pwa`, `workbox-*` y `@vite-pwa/assets-generator`: no se usaban, y había un conflicto de dependencias entre `vite-plugin-pwa` y `assets-generator` que impedía instalar paquetes.

El trabajo sin conexión de la PWA queda para una fase posterior (ver D).

### Estructura

```text
apps/web/src
├── app/          router.tsx, RutaProtegida, permisos.ts (matriz rol → módulo),
│                 layouts/ (AppLayout, MenuLateral, MenuUsuario, Pagina), providers/ (AuthProvider)
├── components/   ui/ (Boton, Campo/Entrada/Selector/AreaTexto/Casilla/Interruptor, Chips, Segmentado,
│                 Pestanas, OpcionTarjeta, Tarjeta/EncabezadoTarjeta/PasoTarjeta/Dato, Kpi, Insignia,
│                 InsigniasEstado, Alerta, Modal/Confirmacion, Avatar, CajaIcono)
│                 tables/ (Tabla, Paginacion) · forms/ (CargaArchivos) · feedback/ (Cargando, Vacio,
│                 ErrorCarga, ErrorFormulario, Avisos) · charts/ (GraficoLineas, GraficoBarras, Dona,
│                 BarrasHorizontales, MapaCorredor, Leyenda) · brand/ (Logo, IlustracionEmbarcacion, Ondas)
├── features/     autenticacion, dashboard, operacion, embarcaciones, mantenimiento, infraestructura,
│                 usuarios, personal, combustible, atencion-usuario, informes, compartido
├── hooks/        consultas compartidas (embarcaciones, motores, tablero…) y usePermiso
├── services/     api/client.ts (HTTP), api/sesion.ts (JWT) y modules/*.service.ts (uno por módulo)
├── styles/       index.css (Tailwind + tokens)
├── types/        api.ts (DTO del backend) y enums.ts (enumeraciones con sus etiquetas)
└── utils/        formato.ts (fechas y cifras) y descargas.ts
```

### Comunicación con la API
- **Instancia única** (`services/api/client.ts`):
  - La URL base sale de `VITE_API_URL`.
  - Agrega el `Authorization: Bearer` a cada petición.
  - Tiempo máximo de 30 s, y 120 s para archivos.
  - Reintenta una sola vez, solo en peticiones GET.
- **Errores normalizados (`ApiError`):** a partir del `ProblemDetail` del backend (`detail` y `errors` por campo).
  - Los errores de validación se muestran junto a su campo cuando el backend los informa, por ejemplo al vincular colaboradores.
  - Hay un mensaje claro por cada código: 400, 401, 403, 404, 409, 413, 429 y 502.
- **Respuestas sin cuerpo (204):** se tratan como `undefined`.
- **Descargas** (PDF, Excel, CSV): usan el nombre de archivo que envía `Content-Disposition`.
- **Servicios por módulo:** `autenticacion`, `viajes`, `usuarios`, `embarcaciones`, `personal`, `combustible`, `mantenimiento`, `infraestructura`, `adjuntos`, `comunicaciones`, `pqrs`, `informes` y `auditoria`. Solo incluyen rutas que existen en los controladores.

### Autenticación y autorización
- **Inicio de sesión:** `POST /auth/login` con número de identificación y contraseña. El rol lo devuelve el backend.
- **Dónde vive el token:** en `sessionStorage`, no en `localStorage`. Se borra al cerrar la pestaña, lo que importa en las taquillas compartidas.
- **Vencimiento:** se respeta `expiresIn`. La sesión se cierra sola al vencer el token, y también ante cualquier 401 de la API, por ejemplo un token alterado o una persona retirada. En ese caso la pantalla de inicio muestra un aviso.
- **Rutas protegidas:** sin sesión, la aplicación lleva al inicio de sesión y, al entrar, regresa a la ruta que se había pedido.
- **Visibilidad por rol:** el menú y las acciones siguen la matriz de `app/permisos.ts`, construida a partir de los `@PreAuthorize`. Una ruta sin permiso muestra «Sin permisos». La API sigue validando cada petición por su cuenta.
- **Recuperación de contraseña:** `/recuperar-contrasena` y `/restablecer-contrasena?token=…` usan los endpoints `request-password-change` y `new-password`. La segunda es la URL que el backend envía por correo.

### Manejo del estado
- **Estado del servidor:** en React Query, con claves por recurso. Cada escritura invalida o actualiza en caché lo que modifica: viajes, personal, motores, infraestructura, etc.
- **Estado de la interfaz:** local en cada página. Los filtros del listado de viajes van en la URL, así que se pueden compartir y sobreviven a una recarga.

---

## B. Matriz de integración

Clasificación: **1** implementada y disponible · **2** parcial · **3** sin endpoint · **4** fuera del alcance del backend.

| Plantilla | Pantalla (ruta) | Endpoints integrados | Operaciones | Estado | Limitaciones |
|---|---|---|---|---|---|
| 00 | Sistema de diseño | — | Tokens, botones, campos, insignias y controles como componentes | 1 | — |
| 01 | Inicio de sesión (`/login`) y recuperación | `POST /auth/login`, `request-password-change`, `new-password` | Iniciar sesión, solicitar enlace, restablecer | 1 | Se quitó «Rol» y «Recordar sesión» (ver C) |
| 02 | Panel principal (`/`) | `GET /pasajeros/consolidado` (mes y hoy, por embarcación), `/embarcaciones`, `/embarcaciones/{id}/hoja-de-vida`, `/operaciones/abiertas`, `/operaciones`, `/infraestructura/tablero`, `/infraestructura/registros`, `/abastecimiento` | Indicadores Hoy/Semana/Mes, serie por embarcación, pasajeros por hora, estado de embarcaciones, alertas, actividad | 2 | Viajes y pasajeros solo para SUPERVISOR (el consolidado es solo suyo). Las demás personas ven los viajes en curso. Sin posición de las embarcaciones en el mapa |
| 03 | Listado de viajes (`/viajes`) | `GET /operaciones` (filtros y paginación del backend) | Pestañas por estado con totales, filtros por fecha, embarcación, origen y destino, exportar | 1 | El filtro de fecha es de un día (no hay rango en el backend). Sin columna de tripulación ni pestaña «Con novedad» (no están en `OperacionResumen` ni en el filtro) |
| 04 | Nueva bitácora (`/viajes/nuevo`) | `POST /operaciones`, `/personal/tripulacion-disponible`, `/motores` | Zarpe con tripulación, recorrido, verificación previa, clima, nivel del río, novedades mecánicas y horómetros iniciales | 1 | Sin evidencias en el zarpe (el DTO no las recibe) |
| 05 | Detalle de viaje (`/viajes/:id`) | `GET /operaciones/{id}`, `PATCH`, `POST /paradas`, `/finalizar`, `/suspender`, `POST/DELETE /tripulacion/{id}`, `/pasajeros/operacion/{id}`, `/comunicaciones` (y reintentar), `/auditoria/OPERACION/{id}` | Paradas, actualizar condiciones, finalizar y suspender con horómetros, agregar o retirar tripulantes, comunicar al supervisor, trazabilidad, imprimir | 1 | No hay PDF de la bitácora en el backend; se ofrece «Imprimir» |
| 06 | Embarcaciones (`/embarcaciones`) | `GET /embarcaciones`, hoja de vida, `/suspensiones`, consolidado por semana, `/abastecimiento` | Tarjetas con estado, mantenimientos y alertas; comparativo del mes | 2 | «Días fuera de servicio» y novedades por embarcación no tienen endpoint |
| 07 | Detalle de embarcación (`/embarcaciones/:id`) | Hoja de vida y su PDF, `/operaciones?embarcacion=`, `/motores` y `PATCH /horometro`, `/suspensiones` (crear y levantar), comunicaciones, auditoría | Pestañas: resumen, operaciones, mantenimientos, repuestos, motores, suspensiones | 1 | No hay endpoint de documentación técnica de la embarcación; no se muestra |
| 08 | Registrar mantenimiento (`/mantenimiento`) | `POST /mantenimientos`, `POST /adjuntos`, `/personal` o `/tripulacion-disponible`, `/motores`, hoja de vida | Preventivo, correctivo y predictivo; sistema y motor; repuestos; hallazgos; recomendaciones; próxima intervención; soportes | 1 | Sin campo «estado de la embarcación» ni casilla de aviso (no están en el DTO) |
| 09 | Embarcaderos (`/infraestructura`) | `/infraestructura/tablero`, `/registros`, `/registros/pendientes-comunicar`, `POST /registros` | Mapa con el estado real, tarjetas por embarcadero, registros, aviso de pendientes | 1 | La estación U. de Córdoba aparece como información estática (no está en el backend: «no entra en piloto») |
| 10 | Detalle de embarcadero (`/infraestructura/embarcaderos/:codigo`) | `/registros?embarcadero=`, `POST /registros`, `/comunicar`, `/cerrar` | Estado por componente según el último registro, deterioros abiertos, historial, registro de revisión, comunicar y cerrar | 1 | Las evidencias se listan pero no se pueden descargar (D3) |
| 11 | Ayudas a la navegación (`/infraestructura/ayudas`) | `/ayudas`, `POST /ayudas`, `/ayudas/{id}/desactivar`, tablero, registros | Indicadores, crear ayuda (con la regla de color DIMAR), desactivar, registrar revisión | 2 | `AyudaResumen` no trae tramo ni ubicación: no se muestran ni se ubican en el mapa |
| 12 | Registro de usuarios (`/usuarios/registro`) | `POST /pasajeros`, `/operaciones/abiertas?embarcadero=`, `GET /pasajeros?fecha&puertoAbordaje` | Registro de una persona y por grupo (sexo × grupo etario y destinos), resumen del día | 1 | El backend no admite «No informa» en sexo o edad |
| 13 | Caracterización (`/usuarios`) | `GET /pasajeros/consolidado`, `GET /pasajeros`, exportación | Filtros, indicadores, serie, sexo, origen, hora, franja, grupo etario, matriz origen–destino, registros | 1 | Sin filtro por grupo etario (el backend no lo tiene). Solo SUPERVISOR; los demás roles van al registro |
| 14 | Personal (`/personal`) | `GET /personal`, `/personal/{id}/documentos`, `/tripulacion-disponible`, `POST /personal` | Conteo por rol, alertas de documentos y PILA, búsqueda y filtros, vincular | 2 | Disponibilidad solo para la tripulación. El listado incluye personas retiradas sin poder distinguirlas (D1) |
| 15 | Colaborador (`/personal/:id`) | `GET /personal/{id}`, documentos (multipart), URL firmada, `desactivar-cuenta`, auditoría | Ficha, documentos exigibles y faltantes, planillas PILA, cargar y ver documentos, desactivar | 2 | No hay endpoint para editar los datos de la persona (D2) |
| 16 | Gestión de turnos | — | — | 4 | R4: los turnos no se modelan en el software (decisión de negocio) |
| 17 | Registrar abastecimiento (`/combustible/nuevo`) | `POST /abastecimiento`, `POST /adjuntos`, último por embarcación | Suministro con soporte obligatorio, tanque lleno, observaciones | 1 | Sin n.º de soporte, proveedor ni lista de seguridad (no están en el DTO) |
| 18 | Historial de combustible (`/combustible`) | `GET /abastecimiento`, `/abastecimiento/consumo`, `/{id}/soporte` | Por embarcación: indicadores, galones por cargue, rendimiento por periodo, tabla paginada y soporte | 2 | El backend lista por embarcación y sin rango de fechas |
| 19 | Registrar novedad (`/novedades/nueva`) | `PATCH /operaciones/{id}`, `POST /infraestructura/registros`, `POST /comunicaciones` | Mecánica, operacional y climática en la bitácora del viaje en curso; seguridad e infraestructura como registro; aviso al supervisor | 2 | No existe una entidad «novedad»: no hay responsable, estado ni fecha compromiso propios |
| 20 | Atención al usuario (`/atencion-usuario`) | `GET /pqrs/gestion` (filtros y paginación), `/gestion/{id}`, `POST /gestion/{id}`, formulario público | Totales por tipo y estado, lista, detalle y gestión (respuesta, traslado, nota interna) | 2 | La respuesta no se envía al correo del usuario (D11). Radicar abre el formulario público del backend |
| 21 | Encuestas de satisfacción | — | — | 3 | No hay endpoints ni entidad |
| 22 | Informes mensuales (`/informes`) | `GET/PUT /informes/mensual/{mes}`, `/cerrar`, `/entregar`, `/pdf`, `/exportaciones/{conjunto}` | Secciones con estado según las cifras, redacción, cerrar, entregar, PDF, exportaciones a Excel o CSV | 1 | «Agregar anexo» no tiene endpoint; el texto «Otros documentos» lo reemplaza |

**Exportaciones (4.8):** el botón «Exportar» está en Viajes, Caracterización, Combustible y Atención al usuario. Usa `GET /exportaciones/{conjunto}` con rango de fechas y formato Excel o CSV.

---

## C. Correcciones realizadas

### Visuales y de consistencia
- **Una sola fuente de estilos:** los tokens (`@theme`) y los componentes compartidos. Así, menú lateral, encabezado, botones, campos, tablas y tarjetas son idénticos en todas las pantallas. En el HTML original, cada página repetía estilos en línea.
- **Paleta institucional:** la exigida, con dorado `#EDC92A`. Los colores de estado (éxito, advertencia, error, información, fondo, bordes y texto secundario) son los de la plantilla 00.
- **Horario del menú lateral:** el diseño decía «Lunes a viernes · sábados mantenimiento». Se corrigió a «Lunes a domingo · 5:00 a. m. – 9:00 p. m.», que es lo que tiene configurado el backend (`operacion.dias`).
- **Insignias «Datos demostrativos»:** se retiraron, porque todas las cifras vienen de la API.
- **Elementos sin respaldo en el backend, retirados en lugar de dejarlos decorativos:**
  - La búsqueda global y la campana de notificaciones del encabezado.
  - El menú «Configuración».
  - El conmutador «Tarjetas / Tabla» de Embarcaciones.
- **Diseño adaptable:**
  - Menú lateral en cajón para tableta y móvil.
  - Tablas con desplazamiento horizontal propio.
  - Formularios en una columna.
  - Barras de acción fijas solo desde tableta.
  - Verificado a 390 px sin desborde horizontal.

### Funcionales: adaptación a los contratos reales
- **Inicio de sesión:**
  - El selector «Rol» y el campo «Usuario o correo» se reemplazaron por **número de identificación** (`LoginRequest`); el rol lo devuelve el backend.
  - Se quitó «Recordar sesión» y la mención a «30 min de inactividad», que no existe en el backend.
- **Identificadores de viaje:** el diseño usa códigos como `V-2026-1244`, que el backend no tiene. Se muestra un código derivado del UUID (`V-XXXXXXXX`).
- **Verificación previa:** se usan los siete puntos de `VerificacionPreviaDTO`. El diseño traía puntos que no existen: refrigerante, líquido hidráulico y limpieza.
- **Bitácora:**
  - El estado «Deficiente» corresponde a `MALO`.
  - Las condiciones climáticas son las del enum `CondicionClimatica`.
  - Se respetan las reglas de `Operacion`:
    - No se zarpa en estado malo ni sin chalecos.
    - Hace falta justificación si algún punto no está conforme.
    - Se exigen 2 operadores y un guía.
    - Origen y destino deben ser distintos.
    - Los horómetros no pueden bajar.
- **Usuarios:** 6 grupos etarios del enum, en lugar de 5 más «No informa». Los totales por sexo, edad y destino siempre coinciden.
- **Combustible:** se agregaron tipo de combustible, valor total y tanque lleno, que el DTO exige. Se retiraron los campos que no recibe.
- **Mantenimiento:** se agregaron sistema intervenido, motor, costos y tipo predictivo. Se retiró «Estado de la embarcación», porque ese estado lo administra el backend.
- **Infraestructura:**
  - Los componentes se filtran según si el activo es un embarcadero o una ayuda, y según si la ayuda es luminosa.
  - Los aspectos afectados son obligatorios en novedad, deterioro y alerta (`esRutinario`).
  - El color de la luz se valida contra la función de la marca (Manual DIMAR).
- **Textos por defecto:** el backend guarda «Sin novedades», «Sin observaciones» y «No aplica». La interfaz los trata como vacíos y no los cuenta como novedades.
- **Fechas:**
  - Se muestran siempre en hora de Bogotá.
  - Los campos que empiezan en «ahora» envían `null` si la persona no los cambia, para que el backend use su hora exacta. Antes se rechazaba levantar una suspensión en el mismo minuto en que se creó.

### Técnicas (encontradas en las pruebas)
- **`CargaArchivos`** actualizaba el estado del formulario dentro de un `setState`, y React lo advertía en consola. Se corrigió.
- **Etiquetas de campos obligatorios:** el asterisco quedaba dentro del nombre accesible. Ahora es `aria-hidden` y el campo lleva `aria-required`.
- **ESLint 10** fallaba con el `typescript-eslint` de la plantilla. Se actualizó y quedó sin errores, incluidas las reglas del React Compiler.

---

## D. Pendientes del backend

No se modificó el backend. Estas son las funciones que necesitan un endpoint nuevo o un cambio:

1. **`PersonalResponse` incompleto:** no trae `contratoActivo`, `fechaVinculacion`, `tipoVinculacion` ni la disponibilidad, y `GET /personal` incluye a las personas retiradas. Sin esos campos no se puede filtrar a los retirados ni mostrar la disponibilidad de taquilla y supervisión.
2. **Editar personal** (`PUT /personal/{id}`): la plantilla 15 tiene el botón «Editar», pero no hay endpoint.
3. **Descargar evidencias:** solo los soportes de abastecimiento y los documentos de personal tienen URL firmada. Hace falta `GET /adjuntos/{id}/url`, o uno equivalente, para las evidencias de infraestructura y los soportes de mantenimiento.
4. **Rangos de fecha:**
   - `GET /operaciones` solo filtra por un día.
   - `GET /abastecimiento` no tiene fechas y exige la embarcación.
   - Hacen falta `desde` y `hasta` para los filtros de periodo de las plantillas 03 y 18.
5. **Datos del panel para roles que no son supervisor:** `GET /pasajeros/consolidado` es solo para SUPERVISOR. Si el panel de operadores y taquilla debe mostrar viajes y pasajeros, hace falta un resumen permitido para esos roles.
6. **Ayudas a la navegación:** `AyudaResumen` no trae tramo, referencia de ubicación, color ni coordenadas, y no hay endpoint para editar una ayuda.
7. **Encuestas de satisfacción (plantilla 21, §7.1.6):** no hay entidad ni endpoints.
8. **Notificaciones y búsqueda global** (encabezado): no hay endpoints.
9. **Posición de las embarcaciones** en el mapa: no se reporta.
10. **Código legible de viaje:** un consecutivo como `V-2026-0001` en lugar del UUID.
11. **Respuesta de PQRS al correo del usuario:** hoy la respuesta solo queda registrada en el sistema.
12. **Cambio de contraseña con la sesión iniciada:** solo existe la recuperación por correo.
13. **Novedades como entidad propia** (plantilla 19), con responsable, estado y fecha compromiso.
14. **Documentación técnica de la embarcación** (manuales, acta de entrega), con carga y consulta.
15. **`UsuarioResponse` sin `operacionId`:** no se puede contar cuántos viajes se atendieron por embarcadero.
16. **Opción «No informa»** en sexo y grupo etario, si el registro en taquilla debe admitirla.

---

## E. Validaciones

| Validación | Resultado |
|---|---|
| Instalación de dependencias (`npm install`) | Correcta |
| TypeScript (`tsc -b`) | Sin errores |
| ESLint | Sin errores ni advertencias |
| Build de producción (`npm run build`) | Correcto. Código dividido por página; el paquete principal pesa ≈117 kB gzip |
| Recorrido de **22 rutas** como supervisor contra la API real | 0 errores de consola y 0 respuestas HTTP con error |
| **16 flujos de escritura** de punta a punta contra la API real | **16/16 correctos**, sin errores de red ni de consola |
| **35 pruebas** de roles, sesión, recuperación y móvil | 35/35 correctas (una falla inicial era del script, no de la aplicación) |

**Cómo se probó:** la API corría en perfil `dev` con los datos de Bootstrap, contra PostgreSQL local. S3 era local (moto) y el correo iba a un servidor SMTP falso; ningún correo salió a Internet.

**Los 16 flujos de escritura:**
- **Bitácora:** zarpe con validaciones, registro de una persona y de un grupo, parada, actualización, comunicación por correo (estado «Enviado») y finalización con horómetros.
- **Combustible y mantenimiento:** abastecimiento con soporte subido a S3; mantenimiento con repuesto y soporte.
- **Embarcación:** suspensión y levantamiento; hoja de vida en PDF.
- **Infraestructura:** deterioro con evidencia, comunicación y cierre; ayuda con la regla de color DIMAR; novedad de infraestructura.
- **Personal:** vinculación con validaciones y carga de un documento.
- **PQRS:** gestión de una solicitud radicada por el formulario público.
- **Informe y exportación:** redacción del informe guardada y verificada tras recargar, su PDF, y exportación a Excel.

**Las 35 pruebas:**
- **Taquillero y operador:**
  - El menú muestra exactamente los módulos permitidos.
  - Las rutas sin permiso muestran «Sin permisos».
  - El panel no hace peticiones que respondan 403.
  - No aparecen «Exportar» ni «Descargar hoja de vida».
- **Sesión:**
  - Un token alterado se rechaza (401) y la sesión se cierra con aviso.
  - Un token vencido no se reutiliza.
  - Tras iniciar sesión, la aplicación regresa a la ruta que se había pedido.
- **Recuperación de contraseña:** la solicitud se recibe; con un token inválido, el backend responde con su mensaje.
- **Móvil (390 px):** 9 pantallas sin desborde horizontal; el menú en cajón navega y se cierra.

**Errores encontrados y corregidos durante la validación:**
- El aviso de React por `setState` en `CargaArchivos`.
- El nombre accesible de los campos obligatorios.
- Los aspectos afectados obligatorios en infraestructura.
- El rechazo al levantar una suspensión en el mismo minuto en que se creó.
- Los textos por defecto que se contaban como novedades.
- La insignia del indicador que se salía de la tarjeta en móvil.

**Limitaciones que permanecen:**
- Las de la columna «Limitaciones» de la matriz y la lista de la sección D.
- La comparación con el PDF se hizo de forma visual, pantalla por pantalla, a partir de las capturas; no hay pruebas automáticas de regresión visual.
- Las pruebas E2E son scripts de Playwright que se corrieron en el entorno de trabajo; todavía no están integrados al repositorio ni al CI.
