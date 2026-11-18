package com.lau.api.servicios;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Envío de correo por SMTP (R50). El servidor se configura con spring.mail.* (variables SPRING_MAIL_HOST,
 * SPRING_MAIL_PORT, SPRING_MAIL_USERNAME, SPRING_MAIL_PASSWORD). Sin esa configuración no hay envío:
 * se lanza una excepción, nunca se simula un envío exitoso.
 */
@Service
public class CorreoServicio {

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String remitente;

    public CorreoServicio(ObjectProvider<JavaMailSender> mailSender,
                          @Value("${businu.correo.remitente}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    /** Envía el correo y devuelve la constancia del servidor. Lanza CorreoException si no se pudo enviar. */
    public Envio enviar(String destinatario, String asunto, String cuerpo) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            throw new CorreoException("El servicio de correo no está configurado (spring.mail.host)");
        }
        try {
            MimeMessage mensaje = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpo, false);
            sender.send(mensaje);                       // saveChanges() asigna el Message-ID antes de enviar
            return new Envio(Instant.now(), mensaje.getMessageID());
        } catch (MessagingException | MailException e) {
            throw new CorreoException("No se pudo enviar el correo: " + e.getMessage());
        }
    }

    public record Envio(Instant fecha, String messageId) { }

    public static class CorreoException extends RuntimeException {
        public CorreoException(String mensaje) {
            super(mensaje);
        }
    }
}
