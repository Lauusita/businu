package com.lau.api.servicios;

import com.lau.api.dominio.AyudaNavegacion;
import com.lau.api.dominio.ComunicacionSupervisor;
import com.lau.api.dominio.Personal;
import com.lau.api.dominio.RegistroInfraestructura;
import com.lau.api.dominio.enums.operacion.Embarcaderos;
import com.lau.api.dominio.enums.personal.Rol;
import com.lau.api.dominio.enums.seguimiento.EstadoActivo;
import com.lau.api.dominio.enums.seguimiento.EstadoSeguimiento;
import com.lau.api.dto.infraestructura.RegistroInfraestructuraResponse.*;
import com.lau.api.dominio.enums.seguimiento.TipoRegistroInfraestructura;
import com.lau.api.dto.infraestructura.*;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.exceptions.ForbiddenException;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.repositorio.AyudaNavegacionRepositorio;
import com.lau.api.repositorio.PersonalRepositorio;
import com.lau.api.repositorio.RegistroInfraestructuraRepositorio;
import com.lau.api.especificaciones.RegistroInfraestructuraSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class InfraestructuraServicio {

    /** "Oportunamente" (§4.4): pasado este plazo, el registro aparece como alerta. */
    private final RegistroInfraestructuraRepositorio registros;
    private final AyudaNavegacionRepositorio ayudas;
    private final PersonalRepositorio personal;
    private final AdjuntoServicio adjuntoServicio;

    public InfraestructuraServicio(RegistroInfraestructuraRepositorio registros, AyudaNavegacionRepositorio ayudas,
                                   PersonalRepositorio personal, AdjuntoServicio adjuntoServicio) {
        this.adjuntoServicio = adjuntoServicio;
        this.registros = registros;
        this.ayudas = ayudas;
        this.personal = personal;
    }

    // ---------------------------------------------------------------- Ayudas a la navegación

    @Transactional
    public AyudaResumen crearAyuda(CrearAyudaNavegacionRequest r, UUID usuarioId) {
        exigirRol(buscarPersonal(usuarioId), Rol.SUPERVISOR);
        if (ayudas.existsByCodigo(r.codigo().trim().toUpperCase())) {
            throw new BusinessException("Ya existe una ayuda con el código " + r.codigo());
        }
        AyudaNavegacion a = ayudas.save(new AyudaNavegacion(
                r.codigo(), r.estructura(), r.funcion(),
                r.luminosa(), r.colorLuz(), r.ritmoLuz(), r.descripcion(),
                r.tramoDesde(), r.tramoHasta(), r.referenciaUbicacion(),
                r.latitud(), r.longitud()));
        return new AyudaResumen(
                a.getId(), a.getCodigo(), a.getEstructura(), a.getFuncion(), a.isLuminosa()
        );
    }

    @Transactional
    public void desactivarAyuda(UUID ayudaId, UUID usuarioId) {
        exigirRol(buscarPersonal(usuarioId), Rol.SUPERVISOR);
        buscarAyuda(ayudaId).desactivar();
    }

    public List<AyudaNavegacion> listarAyudasActivas() {
        return ayudas.findByActivaTrueOrderByCodigo();
    }

    // ---------------------------------------------------------------- Registros

    @Transactional
    public RegistroInfraestructuraResponse registrar(CrearRegistroInfraestructuraRequest r, UUID usuarioId) {
        Personal registrador = buscarPersonal(usuarioId);   // cualquier rol con acceso al sistema
        AyudaNavegacion ayuda = r.ayudaId() != null ? buscarAyuda(r.ayudaId()) : null;

        RegistroInfraestructura reg = new RegistroInfraestructura(
                r.fechaHora() != null ? r.fechaHora() : Instant.now(),
                r.embarcadero(), ayuda, r.tipo(), r.componente(), r.estadoObservado(), r.severidad(),
                r.aspectosAfectados(), r.descripcion(), r.accionesRealizadas(),
                r.requiereIntervencionExterna(), r.entidadCompetente(), registrador);

        if (r.evidenciaIds() != null) {                  // R34: misma vinculación que en mantenimiento
            r.evidenciaIds().forEach(id -> reg.agregarEvidencia(adjuntoServicio.vincular(id, registrador)));
        }
        return toResponse(registros.save(reg));
    }

    @Transactional
    public RegistroInfraestructuraResponse comunicarSupervisor(UUID id, ComunicarSupervisorRequest r, UUID usuarioId) {
        Personal supervisor = buscarPersonal(usuarioId);
        exigirRol(supervisor, Rol.SUPERVISOR);
        RegistroInfraestructura reg = buscar(id);
        reg.comunicarSupervisor(r.fecha() != null ? r.fecha() : Instant.now(), r.medio(), r.referencia(), supervisor);
        return toResponse(reg);
    }

    @Transactional
    public RegistroInfraestructuraResponse cerrar(UUID id, CerrarRegistroRequest r, UUID usuarioId) {
        Personal supervisor = buscarPersonal(usuarioId);
        exigirRol(supervisor, Rol.SUPERVISOR);
        RegistroInfraestructura reg = buscar(id);
        reg.cerrar(r.fecha() != null ? r.fecha() : Instant.now(), r.observacion(), supervisor);
        return toResponse(reg);
    }

    // ---------------------------------------------------------------- Consultas

    public RegistroInfraestructuraResponse obtener(UUID id) {
        return toResponse(buscar(id));
    }

    public Page<RegistroInfraestructuraResponse> listar(EstadoSeguimiento estado, Embarcaderos embarcadero,
                                                        UUID ayudaId, TipoRegistroInfraestructura tipo,
                                                        Instant desde, Instant hasta, Pageable pageable) {
        Specification<RegistroInfraestructura> filtro = Specification.allOf(
                RegistroInfraestructuraSpecs.estado(estado),
                RegistroInfraestructuraSpecs.embarcadero(embarcadero),
                RegistroInfraestructuraSpecs.ayuda(ayudaId),
                RegistroInfraestructuraSpecs.tipo(tipo),
                RegistroInfraestructuraSpecs.entre(desde, hasta));
        return registros.findAll(filtro, pageable).map(this::toResponse);
    }

    /** Lo que el supervisor debe informar al supervisor del contrato, del más antiguo al más reciente. */
    public List<RegistroInfraestructuraResponse> pendientesDeComunicar() {
        return registros.findByRequiereIntervencionExternaTrueAndEstadoOrderByFechaHora(EstadoSeguimiento.ABIERTO)
                .stream().map(this::toResponse).toList();
    }

    /** Tablero: estado actual = estado observado en el último registro. */
    public List<EstadoInfraestructuraResponse> tablero() {
        List<EstadoInfraestructuraResponse> filas = new ArrayList<>();
        for (Embarcaderos e : Embarcaderos.values()) {
            Optional<RegistroInfraestructura> ultimo = registros.findFirstByEmbarcaderoOrderByFechaHoraDesc(e);
            filas.add(new EstadoInfraestructuraResponse("EMBARCADERO", e.name(),
                    ultimo.map(RegistroInfraestructura::getEstadoObservado).orElse(EstadoActivo.OPERATIVO),
                    ultimo.map(RegistroInfraestructura::getFechaHora).orElse(null),
                    registros.countByEmbarcaderoAndEstadoNot(e, EstadoSeguimiento.CERRADO)));
        }
        for (AyudaNavegacion a : ayudas.findByActivaTrueOrderByCodigo()) {
            Optional<RegistroInfraestructura> ultimo = registros.findFirstByAyudaIdOrderByFechaHoraDesc(a.getId());
            filas.add(new EstadoInfraestructuraResponse("AYUDA", a.getCodigo(),
                    ultimo.map(RegistroInfraestructura::getEstadoObservado).orElse(EstadoActivo.OPERATIVO),
                    ultimo.map(RegistroInfraestructura::getFechaHora).orElse(null),
                    registros.countByAyudaIdAndEstadoNot(a.getId(), EstadoSeguimiento.CERRADO)));
        }
        return filas;
    }

    // ---------------------------------------------------------------- Auxiliares

    private RegistroInfraestructura buscar(UUID id) {
        return registros.findById(id).orElseThrow(() -> new NotFoundException("Registro no encontrado"));
    }

    private AyudaNavegacion buscarAyuda(UUID id) {
        return ayudas.findById(id).orElseThrow(() -> new NotFoundException("Ayuda a la navegación no encontrada"));
    }

    private Personal buscarPersonal(UUID id) {
        return personal.findById(id).orElseThrow(() -> new NotFoundException("Personal no encontrado"));
    }

    private static void exigirRol(Personal p, Rol rol) {
        if (p.getRol() != rol) {
            throw new ForbiddenException("Solo el rol " + rol + " puede realizar esta acción");
        }
    }

    private RegistroInfraestructuraResponse toResponse(RegistroInfraestructura r) {
        AyudaNavegacion a = r.getAyuda();
        ComunicacionSupervisor c = r.getComunicacion();
        Long horasSinComunicar = r.pendienteDeComunicar()
                ? Duration.between(r.getFechaHora(), Instant.now()).toHours()
                : null;
        return new RegistroInfraestructuraResponse(
                r.getId(), r.getFechaHora(), r.getEmbarcadero(),
                a == null ? null : new AyudaResumen(a.getId(), a.getCodigo(), a.getEstructura(), a.getFuncion(), a.isLuminosa()),
                r.getTipo(), r.getComponente(), r.getEstadoObservado(), r.getSeveridad(),
                r.getAspectosAfectados(), r.getDescripcion(), r.getAccionesRealizadas(),
                r.isRequiereIntervencionExterna(), r.getEntidadCompetente(), r.getEstado(),
                r.pendienteDeComunicar(), horasSinComunicar,
                c == null ? null : new ComunicacionDTO(c.getFecha(), c.getMedio(), c.getReferencia(),
                        c.getComunicadoPor().nombreCompleto()),
                r.getFechaCierre() == null ? null : new CierreDTO(r.getFechaCierre(), r.getObservacionCierre(),
                        r.getCerradoPor().nombreCompleto()),
                r.getRegistradoPor().nombreCompleto(),
                r.getEvidencias().stream()
                        .map(e -> new com.lau.api.dto.combustible.AbastecimientoResponse.SoporteDTO(
                                e.getId(), e.getNombreOriginal(), e.getTamanoBytes(), e.getSha256()))
                        .toList());
    }
}