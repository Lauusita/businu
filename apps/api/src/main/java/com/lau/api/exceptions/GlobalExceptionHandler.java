package com.lau.api.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.core.exception.SdkException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Errores de validación de DTO
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Uno o más campos tienen errores"
        );

        Map<String, String> errores = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );

        ex.getBindingResult().getGlobalErrors().forEach(error ->
                errores.put("validacionGeneral", error.getDefaultMessage())
        );

        problem.setProperty("errors", errores);

        return problem;
    }

    // Recurso no encontrado
    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
    }

    // Reglas de negocio
    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    // Errores de integridad de la base de datos
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {

        String constraint = "";

        Throwable causa = ex;

        while (causa != null) {
            if (causa instanceof org.hibernate.exception.ConstraintViolationException cve) {
                constraint = cve.getConstraintName() != null
                        ? cve.getConstraintName()
                        : "";
                break;
            }
            causa = causa.getCause();
        }

        String mensaje = switch (constraint) {
            case "uk_personal_numero_identificacion" ->
                    "Ya existe una persona con ese número de identificación";
            case "uk_personal_email" ->
                    "Ya existe una persona con ese correo";
            default ->
                    "El registro viola una restricción de la base de datos";
        };

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                mensaje
        );
    }

    // Conflicto de concurrencia por bloqueo optimista
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(
            ObjectOptimisticLockingFailureException ex) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "El registro fue modificado por otro usuario. Actualice la información e intente nuevamente."
        );
    }

    // Errores del SDK de AWS, por ejemplo S3
    @ExceptionHandler(SdkException.class)
    public ProblemDetail handleAws(SdkException ex) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                "No fue posible completar la operación con el servicio de almacenamiento"
        );
    }

    // No autorizado
    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
        );
    }
}