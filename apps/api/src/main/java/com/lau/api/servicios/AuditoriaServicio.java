package com.lau.api.servicios;

import com.lau.api.dominio.BaseEntity;
import com.lau.api.dominio.Personal;
import com.lau.api.dominio.auditoria.Revision;
import com.lau.api.dominio.enums.TipoRegistroAuditado;
import com.lau.api.dto.auditoria.RevisionResponse;
import com.lau.api.dto.auditoria.RevisionResponse.Accion;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.repositorio.PersonalRepositorio;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta del historial de cambios de un registro (R5, Envers). */
@Service
@Transactional(readOnly = true)
public class AuditoriaServicio {

    /** Campos técnicos que no se informan como "modificados". */
    private static final Set<String> TECNICOS = Set.of("id", "version", "creadoEn", "actualizadoEn", "creadoPor",
            "modificadoPor", "passwordHash", "ultimoAcceso");

    @PersistenceContext
    private EntityManager em;

    private final PersonalRepositorio personal;

    public AuditoriaServicio(PersonalRepositorio personal) {
        this.personal = personal;
    }

    public List<RevisionResponse> historial(TipoRegistroAuditado tipo, UUID id) {
        @SuppressWarnings("unchecked")
        List<Object[]> filas = AuditReaderFactory.get(em).createQuery()
                .forRevisionsOfEntity(tipo.getEntidad(), false, true)
                .add(AuditEntity.id().eq(id))
                .addOrder(AuditEntity.revisionNumber().asc())
                .getResultList();
        if (filas.isEmpty()) {
            throw new NotFoundException("No hay historial para " + tipo + " " + id);
        }
        Set<UUID> autores = filas.stream().map(f -> ((Revision) f[1]).getPersonalId())
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, String> nombres = personal.findAllById(autores).stream()
                .collect(Collectors.toMap(Personal::getId, Personal::nombreCompleto));

        List<RevisionResponse> historial = new ArrayList<>();
        Object anterior = null;
        for (Object[] f : filas) {
            Object estado = f[0];
            Revision rev = (Revision) f[1];
            RevisionType tipoRev = (RevisionType) f[2];
            Accion accion = switch (tipoRev) {
                case ADD -> Accion.CREACION;
                case MOD -> Accion.MODIFICACION;
                case DEL -> Accion.ELIMINACION;
            };
            List<String> cambios = (accion == Accion.MODIFICACION && anterior != null)
                    ? camposModificados(anterior, estado)
                    : List.of();
            UUID autor = rev.getPersonalId();
            historial.add(new RevisionResponse(rev.getId(), Instant.ofEpochMilli(rev.getFecha()), accion, autor,
                    autor == null ? "Sistema" : nombres.getOrDefault(autor, autor.toString()), cambios));
            anterior = estado;
        }
        return historial;
    }

    // ---------------------------------------------------------------- Diferencias entre dos versiones

    /** Campos simples, embebidos y referencias (por id) que cambiaron. Las colecciones no se comparan. */
    private static List<String> camposModificados(Object antes, Object despues) {
        List<String> cambios = new ArrayList<>();
        for (Field campo : campos(antes.getClass())) {
            if (TECNICOS.contains(campo.getName()) || Collection.class.isAssignableFrom(campo.getType())
                    || Map.class.isAssignableFrom(campo.getType())) {
                continue;
            }
            if (!Objects.equals(valor(campo, antes), valor(campo, despues))) {
                cambios.add(campo.getName());
            }
        }
        return cambios;
    }

    /** Valor comparable: el id para entidades, los campos internos para embebidos, el valor para lo demás. */
    private static Object valor(Field campo, Object objeto) {
        Object v = leer(campo, objeto);
        if (v instanceof BaseEntity entidad) {
            return entidad.getId();
        }
        if (v != null && v.getClass().isAnnotationPresent(Embeddable.class)) {
            return campos(v.getClass()).stream()
                    .collect(Collectors.toMap(Field::getName, c -> Optional.ofNullable(leer(c, v)), (a, b) -> a));
        }
        return v;
    }

    private static List<Field> campos(Class<?> tipo) {
        List<Field> lista = new ArrayList<>();
        for (Class<?> c = tipo; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    lista.add(f);
                }
            }
        }
        return lista;
    }

    private static Object leer(Field campo, Object objeto) {
        try {
            campo.setAccessible(true);
            return campo.get(objeto);
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}
