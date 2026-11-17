package com.lau.api.controladores;

import com.lau.api.dominio.Adjunto;
import com.lau.api.dto.combustible.AbastecimientoResponse;
import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.servicios.AdjuntoServicio;
import com.lau.api.servicios.PersonalServicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
@RequestMapping("/adjuntos")
public class AdjuntoControlador {

    private final AdjuntoServicio servicio;
    private final PersonalServicio personalServicio;

    public AdjuntoControlador(AdjuntoServicio servicio, PersonalServicio personalServicio) {
        this.servicio = servicio;
        this.personalServicio = personalServicio;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AbastecimientoResponse.SoporteDTO> subir(@RequestParam(defaultValue = "general") String carpeta,
                                                                   @RequestPart("archivo") MultipartFile archivo,
                                                                   @AuthenticationPrincipal PersonalAuth auth) {
        if (!Set.of("combustible", "mantenimiento", "personal", "general").contains(carpeta)) {
            throw new BusinessException("Carpeta no permitida");
        }

        Adjunto a = servicio.guardarPdf(carpeta, archivo, auth.id());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AbastecimientoResponse.SoporteDTO(a.getId(), a.getNombreOriginal(), a.getTamanoBytes(), a.getSha256()));
    }
}