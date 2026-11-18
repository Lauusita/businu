package com.lau.api.controladores;

import com.lau.api.dominio.Personal;
import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.dto.personal.PersonalDTO;
import com.lau.api.dto.GeneralResponse;
import com.lau.api.dto.personal.PersonalResponse;
import com.lau.api.mappers.PersonalMapper;
import com.lau.api.servicios.PersonalServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("hasRole('SUPERVISOR')")
@RequestMapping("/personal")
public class PersonalControlador {
    private final PersonalServicio personalServicio;
    private final PersonalMapper personalMapper;

    public PersonalControlador(PersonalServicio personalServicio, PersonalMapper personalMapper) {
        this.personalServicio = personalServicio;
        this.personalMapper = personalMapper;
    }

    @GetMapping
    public List<PersonalResponse> obtenerTodos() {
        return personalServicio.findAll();
    }

    @GetMapping("{id}")
    public PersonalResponse obtenerPorId(
            @Valid @PathVariable UUID id
    ) {
        return personalServicio.buscarPorId(id);
    }

    @GetMapping("buscar-personal")
    public PersonalResponse obtenerPorNumeroIdentificacion(
            @RequestParam String numeroIdentificacion
    ) {
        return personalServicio.buscarPorIdentificacion(numeroIdentificacion);
    }

    @PostMapping
    public PersonalResponse crear(
            @Valid() @RequestBody() PersonalDTO personalDTO) {
        return personalServicio.crear(personalDTO, personalDTO.password());
    }

    @PostMapping("desactivar-cuenta/{id}")
    public void desactivarCuenta(
            @PathVariable UUID id,
            @AuthenticationPrincipal PersonalAuth auth
            ) {
        personalServicio.desactivarCuenta(id, auth.id());
    }

    @GetMapping("/tripulacion-disponible")
    public List<PersonalResponse> tripulacionDisponible() {
        return personalServicio.tripulacionDisponible();
    }

}
