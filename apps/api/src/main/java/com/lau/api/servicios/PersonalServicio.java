package com.lau.api.servicios;

import com.lau.api.dominio.Personal;
import com.lau.api.dto.GeneralResponse;
import com.lau.api.dto.personal.PersonalResponse;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.mappers.PersonalMapper;
import com.lau.api.repositorio.PersonalRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
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

    public void save(Personal personal) {
        personal.habilitarAcceso(personal.getPasswordHash());

        String scriptedPassword = passwordEncoder.encode(personal.getPasswordHash());

        personal.setPasswordHash(scriptedPassword);

        if (personalRepositorio.existsByNumeroIdentificacion(personal.getNumeroIdentificacion())) {
            throw new BusinessException("Ya existe una persona con ese número de identificación");
        }
        if (personalRepositorio.existsByEmail(personal.getEmail())) {
            throw new BusinessException("Ya existe una persona con ese correo");
        }

        personalRepositorio.save(personal);
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

    public GeneralResponse desactivarCuenta(UUID id) {
        Personal personal = personalRepositorio.findById(id).orElseThrow(() -> new NotFoundException("Cuenta no encontrada"));
        personal.setActivo(false);
        personal.setFechaRetiro(Instant.now());
        return new GeneralResponse(HttpStatus.OK, "Cuenta desactivada exitosamente.");
    }

}
