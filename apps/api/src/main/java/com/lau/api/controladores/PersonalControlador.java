package com.lau.api.controladores;

import com.lau.api.dominio.Personal;
import com.lau.api.dto.personal.PersonalDTO;
import com.lau.api.dto.GeneralResponse;
import com.lau.api.mappers.PersonalMapper;
import com.lau.api.servicios.PersonalServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("personal")
public class PersonalControlador {
    private final PersonalServicio personalServicio;
    private final PersonalMapper personalMapper;

    public PersonalControlador(PersonalServicio personalServicio, PersonalMapper personalMapper) {
        this.personalServicio = personalServicio;
        this.personalMapper = personalMapper;
    }

    @GetMapping
    public List<Personal> obtenerTodos() {
        return personalServicio.findAll();
    }

    @PostMapping
    public ResponseEntity<GeneralResponse> crear(
            @Valid() @RequestBody() PersonalDTO personalDTO) {
        Personal personal = personalMapper.requestToPersonal(personalDTO);
        personalServicio.save(personal);
        return ResponseEntity.ok(new GeneralResponse(HttpStatus.OK, "Operación exitosa"));
    }
}
