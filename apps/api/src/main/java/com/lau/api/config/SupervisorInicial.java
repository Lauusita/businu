package com.lau.api.config;

import com.lau.api.dominio.Personal;
import com.lau.api.dominio.enums.personal.Rol;
import com.lau.api.dominio.enums.personal.TipoIdentificacion;
import com.lau.api.dominio.enums.personal.TipoVinculacion;
import com.lau.api.repositorio.PersonalRepositorio;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea el primer supervisor al arrancar, con datos de variables de entorno, SOLO si todavía no existe
 * ningún supervisor con contrato activo. En producción la base nace vacía y crear personal exige ser
 * supervisor: sin esto nadie podría entrar.
 *
 * <p>Variables: SUPERVISOR_INICIAL_DOCUMENTO, SUPERVISOR_INICIAL_NOMBRES, SUPERVISOR_INICIAL_APELLIDOS,
 * SUPERVISOR_INICIAL_EMAIL y SUPERVISOR_INICIAL_CONTRASENA (obligatorias), y opcionales
 * SUPERVISOR_INICIAL_TIPO_IDENTIFICACION (CC) y SUPERVISOR_INICIAL_VINCULACION (LABORAL).
 * Una vez creado, no se vuelve a usar: las variables se pueden quitar.
 *
 * <p>Corre después de Bootstrap (datos de prueba de dev), que ya trae su propio supervisor.
 */
@Component
@Order(2)
public class SupervisorInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SupervisorInicial.class);

    private final PersonalRepositorio personal;
    private final PasswordEncoder encoder;
    private final String tipoIdentificacion;
    private final String documento;
    private final String nombres;
    private final String apellidos;
    private final String email;
    private final String contrasena;
    private final String vinculacion;

    public SupervisorInicial(PersonalRepositorio personal, PasswordEncoder encoder,
                             @Value("${businu.supervisor-inicial.tipo-identificacion:CC}") String tipoIdentificacion,
                             @Value("${businu.supervisor-inicial.documento:}") String documento,
                             @Value("${businu.supervisor-inicial.nombres:}") String nombres,
                             @Value("${businu.supervisor-inicial.apellidos:}") String apellidos,
                             @Value("${businu.supervisor-inicial.email:}") String email,
                             @Value("${businu.supervisor-inicial.contrasena:}") String contrasena,
                             @Value("${businu.supervisor-inicial.vinculacion:LABORAL}") String vinculacion) {
        this.personal = personal;
        this.encoder = encoder;
        this.tipoIdentificacion = tipoIdentificacion.trim();
        this.documento = documento.trim();
        this.nombres = nombres.trim();
        this.apellidos = apellidos.trim();
        this.email = email.trim();
        this.contrasena = contrasena;
        this.vinculacion = vinculacion.trim();
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        boolean haySupervisor = personal.findByRolIn(java.util.List.of(Rol.SUPERVISOR)).stream()
                .anyMatch(p -> p.isContratoActivo() && p.tieneAcceso());
        if (haySupervisor) {
            return;                                           // caso normal después del primer arranque
        }
        if (documento.isEmpty() || contrasena.isEmpty()) {
            log.warn("No hay ningún supervisor activo y no se configuró SUPERVISOR_INICIAL_DOCUMENTO / "
                    + "SUPERVISOR_INICIAL_CONTRASENA: nadie podrá iniciar sesión hasta configurarlas y reiniciar");
            return;
        }
        String error = validar();
        if (error != null) {
            log.error("Supervisor inicial NO creado: {}", error);   // nunca se escribe la contraseña en el log
            return;
        }
        if (personal.existsByNumeroIdentificacion(documento)) {
            log.error("Supervisor inicial NO creado: ya existe una persona con el documento {} y no es un "
                    + "supervisor activo. Revísela en la base de datos", documento);
            return;
        }
        if (personal.existsByEmail(email)) {
            log.error("Supervisor inicial NO creado: el correo {} ya está registrado", email);
            return;
        }
        Personal p = new Personal(
                TipoIdentificacion.valueOf(tipoIdentificacion.toUpperCase(Locale.ROOT)), documento, nombres, apellidos,
                Rol.SUPERVISOR, TipoVinculacion.valueOf(vinculacion.toUpperCase(Locale.ROOT)), email,
                LocalDate.now(ZoneId.of("America/Bogota")), null);
        p.habilitarAcceso(encoder.encode(contrasena));
        personal.save(p);
        log.info("Supervisor inicial creado: {} {} (documento {}). Ya puede iniciar sesión; las variables "
                + "SUPERVISOR_INICIAL_* se pueden quitar", nombres, apellidos, documento);
    }

    /** Mismas reglas que PersonalDTO al crear personal por la API. */
    private String validar() {
        if (!documento.matches("^[A-Za-z0-9]{5,20}$")) {
            return "SUPERVISOR_INICIAL_DOCUMENTO debe tener entre 5 y 20 letras o números, sin puntos ni espacios";
        }
        if (nombres.length() < 2 || apellidos.length() < 2) {
            return "faltan SUPERVISOR_INICIAL_NOMBRES o SUPERVISOR_INICIAL_APELLIDOS";
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "SUPERVISOR_INICIAL_EMAIL no es un correo válido";
        }
        if (contrasena.length() < 8 || contrasena.length() > 72) {
            return "SUPERVISOR_INICIAL_CONTRASENA debe tener entre 8 y 72 caracteres";
        }
        try {
            TipoIdentificacion.valueOf(tipoIdentificacion.toUpperCase(Locale.ROOT));
            TipoVinculacion.valueOf(vinculacion.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return "SUPERVISOR_INICIAL_TIPO_IDENTIFICACION o SUPERVISOR_INICIAL_VINCULACION no es válido";
        }
        return null;
    }
}
