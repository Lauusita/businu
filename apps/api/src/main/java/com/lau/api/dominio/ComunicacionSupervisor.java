package com.lau.api.dominio;

import com.lau.api.dominio.enums.seguimiento.MedioComunicacion;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Constancia de que la situación se informó al supervisor del contrato (§4.4). */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ComunicacionSupervisor {

    @Column(name = "comunicado_en")
    private Instant fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "comunicado_medio", length = 30)
    private MedioComunicacion medio;

    /** Número de oficio, asunto del correo, etc. */
    @Column(name = "comunicado_referencia", length = 200)
    private String referencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comunicado_por_id")
    private Personal comunicadoPor;
}