package com.lau.api.servicios;

import com.lau.api.dominio.Adjunto;
import com.lau.api.dominio.Personal;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.repositorio.AdjuntoRepositorio;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

/** Guarda PDFs en S3 y registra sus metadatos en la tabla adjuntos. */
@Service
public class AdjuntoServicio {

    private static final long MAX_BYTES = 10L * 1024 * 1024;   // 10 MB
    private static final ZoneId COLOMBIA = ZoneId.of("America/Bogota");

    private final AdjuntoRepositorio adjuntos;
    private final S3Servicio s3;

    public AdjuntoServicio(AdjuntoRepositorio adjuntos, S3Servicio s3) {
        this.adjuntos = adjuntos;
        this.s3 = s3;
    }

    private record TipoPermitido(String contentType, String extension) { }

    private static TipoPermitido detectarTipo(byte[] b) {
        if (b.length >= 5 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F' && b[4] == '-') {
            return new TipoPermitido("application/pdf", "pdf");
        }
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return new TipoPermitido("image/jpeg", "jpg");
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return new TipoPermitido("image/png", "png");
        }
        throw new BusinessException("Solo se aceptan archivos PDF, JPG o PNG");
    }

    @Transactional
    public Adjunto guardar(String carpeta, MultipartFile archivo, Personal subidoPor) {
        byte[] bytes = leer(archivo);                 // vacío / > 10 MB, como antes
        TipoPermitido tipo = detectarTipo(bytes);
        String clave = s3.subir(carpeta + "/" + YearMonth.now(COLOMBIA), bytes, tipo.contentType(), tipo.extension());
        try {
            return adjuntos.save(new Adjunto(clave, nombreSeguro(archivo), tipo.contentType(),
                    bytes.length, sha256(bytes), subidoPor));
        } catch (RuntimeException e) {
            s3.eliminar(clave);
            throw e;
        }
    }

    /**
     * Sube el PDF y crea el Adjunto. Debe llamarse DENTRO de la transacción de quien lo usa:
     * si esa transacción falla, el archivo se borra de S3.
     *
     * @param carpeta p. ej. "combustible", "mantenimiento", "personal"
     */
    @Transactional
    public Adjunto guardarPdf(String carpeta, MultipartFile archivo, Personal subidoPor) {
        byte[] bytes = leerPdf(archivo);
        String clave = s3.subir(carpeta + "/" + YearMonth.now(COLOMBIA), bytes, "application/pdf", "pdf");
        try {
            return adjuntos.save(new Adjunto(clave, nombreSeguro(archivo), "application/pdf",
                    bytes.length, sha256(bytes), subidoPor));
        } catch (RuntimeException e) {
            s3.eliminar(clave);   // si la base falla, no dejar el archivo
            throw e;
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Adjunto vincular(UUID adjuntoId, Personal quien) {
        Adjunto a = adjuntos.findById(adjuntoId)
                .orElseThrow(() -> new NotFoundException("Soporte no encontrado: súbalo primero en /adjuntos"));
        a.vincular(quien);
        return a;
    }

    /** URL temporal para ver el archivo. */
    public String urlDescarga(UUID adjuntoId) {
        Adjunto a = adjuntos.findById(adjuntoId).orElseThrow(() -> new NotFoundException("Adjunto no encontrado"));
        return s3.urlDescarga(a.getClaveAlmacenamiento(), Duration.ofMinutes(5));
    }

    // ---------------------------------------------------------------- Auxiliares

    private static byte[] leerPdf(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessException("Adjuntar el documento es obligatorio");
        }
        if (archivo.getSize() > MAX_BYTES) {
            throw new BusinessException("El soporte no puede superar 10 MB");
        }
        try {
            byte[] bytes = archivo.getBytes();
            // La extensión y el content-type los decide el navegador: se revisa la firma real del archivo
            if (bytes.length < 5 || !new String(bytes, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-")) {
                throw new BusinessException("El soporte debe ser un archivo PDF válido");
            }
            return bytes;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el archivo", e);
        }
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);   // SHA-256 siempre existe en la JVM
        }
    }

    private static String nombreSeguro(MultipartFile archivo) {
        String nombre = Objects.requireNonNullElse(archivo.getOriginalFilename(), "soporte.pdf")
                .replaceAll("[\\\\/]", "_");
        return nombre.length() > 255 ? nombre.substring(nombre.length() - 255) : nombre;
    }

    /** S3 no participa en la transacción de la base: si la base hace rollback, se deshace a mano. */
    private void borrarDeS3SiHayRollback(String clave) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    try {
                        s3.eliminar(clave);
                    } catch (RuntimeException ignorada) {
                        // queda huérfano en S3; no debe tapar el error original
                    }
                }
            }
        });
    }
}