package com.lau.api.servicios;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;

import com.lau.api.dominio.Adjunto;
import com.lau.api.dto.personal.PersonalAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Service
public class S3Servicio {

    private final S3Client s3;
    private final S3Presigner presigner;
    private final String bucket;

    public S3Servicio(S3Client s3, S3Presigner presigner, @Value("${aws.s3.bucket}") String bucket) {
        this.s3 = s3;
        this.presigner = presigner;
        this.bucket = bucket;
    }

    /**
     * Sube el archivo con un nombre único y devuelve la clave con la que quedó guardado.
     * Si falla, lanza la excepción del SDK: quien llama no debe guardar el registro.
     */
    public String subir(String carpeta, byte[] contenido, String contentType, String extension) {
        String clave = carpeta + "/" + UUID.randomUUID() + "." + extension;
        s3.putObject(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(clave)
                        .contentType(contentType)
                        .serverSideEncryption(ServerSideEncryption.AES256)
                        .build(),
                RequestBody.fromBytes(contenido));
        return clave;
    }

    /** URL temporal para descargar, sin pasar los bytes por la API ni dejar conexiones abiertas. */
    public String urlDescarga(String clave, Duration vigencia) {
        return presigner.presignGetObject(r -> r
                        .signatureDuration(vigencia)
                        .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(clave).build()))
                .url()
                .toString();
    }

    public void eliminar(String clave) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(clave).build());
    }
}