package com.lau.api.servicios;

import com.lau.api.dominio.Personal;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.repositorio.PersonalRepositorio;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PersonalServicio {
    public PersonalRepositorio personalRepositorio;

    public Personal save(Personal personal) {
        return personalRepositorio.save(personal);
    };

    public Personal buscarPorId(UUID id) {
        return personalRepositorio.findById(id).orElseThrow(() -> new NotFoundException("Personal no encontrado"));
    }
}
