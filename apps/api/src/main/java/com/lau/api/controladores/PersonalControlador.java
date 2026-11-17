package com.lau.api.controladores;

import com.lau.api.dominio.Personal;
import com.lau.api.dto.personal.PersonalDTO;
import com.lau.api.dto.GeneralResponse;
import com.lau.api.dto.personal.PersonalResponse;
import com.lau.api.mappers.PersonalMapper;
import com.lau.api.servicios.PersonalServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin("*")
@PreAuthorize("hasRole('SUPERVISOR')")
@RequestMapping("personal")
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
            @RequestParam("") String numeroIdentificacion
    ) {
        return personalServicio.buscarPorIdentificacion(numeroIdentificacion);
    }

    @PostMapping
    public ResponseEntity<GeneralResponse> crear(
            @Valid() @RequestBody() PersonalDTO personalDTO) {
        Personal personal = personalMapper.requestToPersonal(personalDTO);
        personalServicio.save(personal);
        return ResponseEntity.ok(new GeneralResponse(HttpStatus.OK, "Operación exitosa"));
    }

    @PostMapping("desactivar-cuenta/{id}")
    public GeneralResponse desactivarCuenta(
            @PathVariable UUID id
    ) {
        return personalServicio.desactivarCuenta(id);
    }
}
