package com.lau.api.servicios;

import com.lau.api.dominio.Personal;
import com.lau.api.dominio.TokenRecuperacion;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.repositorio.PersonalRepositorio;
import com.lau.api.repositorio.TokenRecuperacionRepositorio;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Restablecimiento de contraseña por correo. /new-password solo acepta el token que llegó en el enlace:
 * no recibe usuario ni documento, así que sin el correo no hay forma de cambiar la contraseña.
 */
@Service
public class RecuperacionContrasenaServicio {

    private static final Logger log = LoggerFactory.getLogger(RecuperacionContrasenaServicio.class);
    private static final Duration VIGENCIA = Duration.ofMinutes(30);
    private static final Duration ESPERA_ENTRE_SOLICITUDES = Duration.ofMinutes(2);
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final String ENLACE_INVALIDO = "El enlace no es válido o ya venció. Solicite uno nuevo.";

    private final PersonalRepositorio personal;
    private final TokenRecuperacionRepositorio tokens;
    private final PasswordEncoder encoder;
    private final CorreoServicio correo;
    private final String urlRestablecer;

    public RecuperacionContrasenaServicio(PersonalRepositorio personal, TokenRecuperacionRepositorio tokens,
                                          PasswordEncoder encoder, CorreoServicio correo,
                                          @Value("${businu.recuperacion.url}") String urlRestablecer) {
        this.personal = personal;
        this.tokens = tokens;
        this.encoder = encoder;
        this.correo = correo;
        this.urlRestablecer = urlRestablecer;
    }

    /** No revela si la cuenta existe: el controlador responde lo mismo en todos los casos. */
    @Transactional
    public void solicitar(String numeroIdentificacion) {
        Optional<Personal> candidato = personal.findByNumeroIdentificacion(numeroIdentificacion.trim())
                .filter(p -> p.getRol().usaSistema())
                .filter(Personal::tieneAcceso)
                .filter(p -> p.getEmail() != null && !p.getEmail().isBlank());
        if (candidato.isEmpty()) {
            return;
        }
        Personal p = candidato.get();
        Instant ahora = Instant.now();

        if (tokens.existsByPersonal_IdAndCreadoEnAfter(p.getId(), ahora.minus(ESPERA_ENTRE_SOLICITUDES))) {
            return;     // ya se envió un enlace hace poco: no se llena el buzón
        }
        tokens.anularPendientes(p.getId(), ahora);

        String token = generarToken();
        tokens.save(new TokenRecuperacion(p, hash(token), ahora, ahora.plus(VIGENCIA)));

        String enlace = urlRestablecer + "?token=" + token;
        String cuerpo = """
                Hola, %s.

                Recibimos una solicitud para restablecer su contraseña del Sistema BUSINÚ.
                Para crear una contraseña nueva, abra este enlace (vence en %d minutos y solo se puede usar una vez):

                %s

                Si usted no hizo esta solicitud, ignore este correo: su contraseña actual sigue funcionando.
                """.formatted(p.getNombres(), VIGENCIA.toMinutes(), enlace);
        try {
            correo.enviar(p.getEmail(), "Restablecer contraseña · BUSINÚ", cuerpo);
        } catch (CorreoServicio.CorreoException e) {
            // No se propaga: la respuesta debe ser igual exista o no la cuenta
            log.warn("No se pudo enviar el enlace de recuperación a la persona {}: {}", p.getId(), e.getMessage());
        }
    }

    @Transactional
    public void restablecer(String token, String nuevaContrasena) {
        Instant ahora = Instant.now();
        TokenRecuperacion t = tokens.findByTokenHash(hash(token))
                .filter(x -> x.vigente(ahora))
                .orElseThrow(() -> new BusinessException(ENLACE_INVALIDO));

        Personal p = t.getPersonal();
        if (!p.tieneAcceso()) {                     // retirada o sin acceso después de pedir el enlace
            throw new BusinessException(ENLACE_INVALIDO);
        }
        p.habilitarAcceso(encoder.encode(nuevaContrasena));
        t.marcarUsado(ahora);
        tokens.anularPendientes(p.getId(), ahora);
    }

    private static String generarToken() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
