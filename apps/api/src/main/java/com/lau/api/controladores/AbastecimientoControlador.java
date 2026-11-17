package com.lau.api.controladores;

import com.lau.api.dto.combustible.AbastecimientoRequest;
import com.lau.api.dto.combustible.AbastecimientoResponse;
import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.servicios.AbastecimientoServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("abastecimiento")
public class AbastecimientoControlador {
    private final AbastecimientoServicio abastecimientoServicio;

    public AbastecimientoControlador(AbastecimientoServicio abastecimientoServicio) {
        this.abastecimientoServicio = abastecimientoServicio;
    }

    @PostMapping
    public ResponseEntity<AbastecimientoResponse> registrar(@Valid @RequestBody AbastecimientoRequest request,
                                                            @AuthenticationPrincipal PersonalAuth auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(abastecimientoServicio.registrar(request, auth.id()));
    }

}
