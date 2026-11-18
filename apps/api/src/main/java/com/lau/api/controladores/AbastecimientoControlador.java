package com.lau.api.controladores;

import com.lau.api.dto.combustible.AbastecimientoRequest;
import com.lau.api.dto.combustible.AbastecimientoResponse;
import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.servicios.AbastecimientoServicio;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@PreAuthorize("hasAnyRole=('SUPERVISOR','OPERADOR_EMBARCACION')")
@RequestMapping("/abastecimiento")
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

    @GetMapping("/{id}")
    public AbastecimientoResponse obtener(@PathVariable UUID id) {
        return abastecimientoServicio.obtener(id);
    }

    @GetMapping
    public PagedModel<AbastecimientoResponse> listarPorEmbarcacion(
            @RequestParam UUID embarcacionId,
            @PageableDefault(size = 20, sort = "fechaHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(abastecimientoServicio.listarPorEmbarcacion(embarcacionId, pageable));
    }

    @GetMapping("/{id}/soporte")
    public Map<String, String> urlSoporte(@PathVariable UUID id) {
        return Map.of("url", abastecimientoServicio.urlSoporte(id));
    }

}
