package com.lau.api.servicios;

import com.lau.api.dominio.Operacion;
import com.lau.api.dominio.Personal;
import com.lau.api.dominio.Usuario;
import com.lau.api.dominio.enums.Rol;
import com.lau.api.dto.usuario.UsuarioRequest;
import com.lau.api.dto.usuario.UsuarioResponse;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.mappers.UsuarioMapper;
import com.lau.api.repositorio.OperacionRepositorio;
import com.lau.api.repositorio.PersonalRepositorio;
import com.lau.api.repositorio.UsuarioRepositorio;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UsuarioServicio {
    private final UsuarioRepositorio usuarioRepositorio;
    private final OperacionRepositorio operacionRepositorio;
    private final PersonalRepositorio personalRepositorio;
    private final UsuarioMapper usuarioMapper;

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio, OperacionRepositorio operacionRepositorio,
                           PersonalRepositorio personalRepositorio, UsuarioMapper usuarioMapper) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.operacionRepositorio = operacionRepositorio;
        this.personalRepositorio = personalRepositorio;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional
    public UsuarioResponse registrar(UsuarioRequest request, UUID personalId) {
        Operacion operacion = operacionRepositorio.findById(request.operacionId())
                .orElseThrow(() -> new NotFoundException("Operación no encontrada"));

        Personal registrador = personalRepositorio.findById(personalId)
                .orElseThrow(() -> new NotFoundException("Personal no encontrado"));
        if (registrador.getRol() != Rol.TAQUILLERO && registrador.getRol() != Rol.SUPERVISOR) {
            throw new BusinessException("Solo taquilleros y supervisores registran usuarios transportados");
        }


        // La embarcación sale de la operación: no se pide en el request
        Usuario usuario = usuarioMapper.requestToUsuario(request, operacion, registrador);
        usuarioRepositorio.save(usuario);

        return usuarioMapper.toResponse(usuario);   // dentro de la transacción: LAZY disponible
    }

    public UsuarioResponse obtenerPorId(UUID id) {
        return usuarioRepositorio.findById(id)
                .map(usuarioMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Registro de usuarios no encontrado"));
    }

    public List<UsuarioResponse> obtenerTodos() {
        return usuarioRepositorio.findAll().stream().map(usuarioMapper::toResponse).toList();
    }

    public List<UsuarioResponse> obtenerPorOperacion(UUID operacionId) {
        Operacion operacion = operacionRepositorio.findById(operacionId)
                .orElseThrow(() -> new NotFoundException("Operación no encontrada"));
        return operacion.getUsuarios().stream().map(usuarioMapper::toResponse).toList();
    }
}