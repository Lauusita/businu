package com.lau.api.dominio;

import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Metadatos de un archivo guardado fuera de la base (S3 o disco). La base nunca guarda los bytes. */
@Getter
@Entity
@Table(name = "adjuntos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Adjunto extends BaseEntity {

    /** Ruta dentro del almacenamiento, p. ej. "combustible/2026/11/3f1c....pdf". */
    @Column(name = "clave_almacenamiento", nullable = false, unique = true, length = 300)
    private String claveAlmacenamiento;

    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "tamano_bytes", nullable = false)
    private long tamanoBytes;

    /** Huella del archivo: permite verificar que el soporte no fue alterado (información "verificable"). */
    @Column(nullable = false, length = 64)
    private String sha256;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subido_por_id", nullable = false)
    private Personal subidoPor;

    @Column(nullable = false)
    private boolean vinculado = false;

    /** Lo usa el servicio que lo asocia a un registro (combustible, mantenimiento...). */
    public void vincular(Personal quien) {
        if (vinculado) {
            throw new BusinessException("Este soporte ya está asociado a otro registro");
        }
        if (!subidoPor.getId().equals(quien.getId())) {
            throw new BusinessException("Solo quien subió el soporte puede asociarlo");
        }
        this.vinculado = true;
    }

    public Adjunto(String claveAlmacenamiento, String nombreOriginal, String contentType,
                   long tamanoBytes, String sha256, Personal subidoPor) {
        this.claveAlmacenamiento = claveAlmacenamiento;
        this.nombreOriginal = nombreOriginal;
        this.contentType = contentType;
        this.tamanoBytes = tamanoBytes;
        this.sha256 = sha256;
        this.subidoPor = subidoPor;
    }
}