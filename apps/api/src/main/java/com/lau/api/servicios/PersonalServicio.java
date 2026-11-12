package com.lau.api.servicios;

import com.lau.api.dominio.Personal;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.repositorio.PersonalRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PersonalServicio {
    private final PersonalRepositorio personalRepositorio;
    private final PasswordEncoder passwordEncoder;
    public PersonalServicio(
            PersonalRepositorio personalRepositorio,
            PasswordEncoder passwordEncoder
    ) {
        this.personalRepositorio = personalRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    public void save(Personal personal) {
        String scriptedPassword = passwordEncoder.encode(personal.getPasswordHash());

        personal.setPasswordHash(scriptedPassword);
        if (personalRepositorio.existsByNumeroIdentificacion(personal.getNumeroIdentificacion())) {
            throw new BusinessException("Ya existe una persona con ese número de identificación");
        }
        if (personalRepositorio.existsByEmail(personal.getEmail())) {
            throw new BusinessException("Ya existe una persona con ese correo");
        }

        personal.habilitarAcceso(personal.getNumeroIdentificacion(), personal.getPasswordHash());
        personalRepositorio.save(personal);
    }

    public List<Personal> findAll() {
        return personalRepositorio.findAll();
    }
    public Personal buscarPorId(UUID id) {
        return personalRepositorio.findById(id).orElseThrow(() -> new NotFoundException("Personal no encontrado"));
    }

    public Personal buscarPorNumeroIdentificacion(String id) {
        return personalRepositorio.findByNumeroIdentificacion(id).orElseThrow(() -> new NotFoundException("No existe registro con el documento de identidad."));
    }
}
