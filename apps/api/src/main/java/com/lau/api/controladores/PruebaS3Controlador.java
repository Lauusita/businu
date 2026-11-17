package com.lau.api.controladores;

import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.servicios.S3Servicio;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Map;

import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/prueba-s3")
public class PruebaS3Controlador {

    private final S3Servicio s3;

    public PruebaS3Controlador(S3Servicio s3) {
        this.s3 = s3;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> subir(
            @RequestPart("archivo") MultipartFile archivo,
            @Param("carpeta") String carpeta
            ) throws IOException {
        byte[] bytes = archivo.getBytes();

        if (bytes.length < 5 || !new String(bytes, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-")) {
            throw new BusinessException("El archivo debe ser un PDF válido");
        }
        String clave = s3.subir(carpeta, bytes, "application/pdf", "pdf");
        return Map.of("clave", clave, "url", s3.urlDescarga(clave, Duration.ofMinutes(5)));
    }

    @GetMapping
    public ResponseEntity<Void> descargar(@RequestParam String clave) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(s3.urlDescarga(clave, Duration.ofMinutes(5))))
                .build();
    }
}