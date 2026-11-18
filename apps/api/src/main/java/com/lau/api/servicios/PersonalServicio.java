package com.lau.api.servicios;

import com.lau.api.dominio.Personal;
import com.lau.api.dominio.enums.personal.Rol;
import com.lau.api.dto.personal.PersonalDTO;
import com.lau.api.dto.personal.PersonalResponse;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.mappers.PersonalMapper;
import com.lau.api.repositorio.PersonalRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PersonalServicio {
    private final PersonalRepositorio personalRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final PersonalMapper personalMapper;

    public PersonalServicio(
            PersonalRepositorio personalRepositorio,
            PasswordEncoder passwordEncoder, PersonalMapper personalMapper
    ) {
        this.personalRepositorio = personalRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.personalMapper = personalMapper;
    }

    @Transactional
    public PersonalResponse crear(PersonalDTO personalDTO, String contrasena) {
        Personal personal = personalMapper.requestToPersonal(personalDTO);

        if (personalRepositorio.existsByNumeroIdentificacion(personal.getNumeroIdentificacion())) {
            throw new BusinessException("Ya existe una persona con ese número de identificación");
        }
        if (personalRepositorio.existsByEmail(personal.getEmail())) {
            throw new BusinessException("Ya existe una persona con ese correo");
        }
        if (personal.getRol().usaSistema()) {
            personal.habilitarAcceso(passwordEncoder.encode(contrasena));
        } else {
            personal.revocarAcceso();
        }
        return personalMapper.toResponse(personalRepositorio.save(personal));
    }

    @Transactional
    public void desactivarCuenta(UUID id, UUID quienDesactiva) {
        if (id.equals(quienDesactiva)) {
            throw new BusinessException("No puede desactivar su propia cuenta");
        }
        Personal p = personalRepositorio.findById(id).orElseThrow(() -> new NotFoundException("Cuenta no encontrada"));
        p.desactivarPersonal();          // método de dominio: activo = false, fechaRetiro = hoy
    }

    public List<PersonalResponse> findAll() {
        List<Personal> personales = personalRepositorio.findAll();
        return personales.stream().map(personalMapper::toResponse).toList();
    }
    public PersonalResponse buscarPorId(UUID id) {
        Personal personal = personalRepositorio.findById(id).orElseThrow(() -> new NotFoundException("Personal no encontrado"));
        return personalMapper.toResponse(personal);
    }

    public PersonalResponse buscarPorIdentificacion(String id) {
        Personal personal = personalRepositorio.findByNumeroIdentificacion(id).orElseThrow(() -> new NotFoundException("Identificación no registrada"));
        return personalMapper.toResponse(personal);
    }


    public List<PersonalResponse> tripulacionDisponible() {
        List<Personal> tripulacion = personalRepositorio.findByRolIn(List.of(Rol.GUIA_TURISTICA, Rol.OPERADOR_EMBARCACION));
        return tripulacion.stream().map(personalMapper::toResponse).toList();
    }
}
