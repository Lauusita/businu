package com.lau.api.utilidades;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * PDF a partir de plantillas HTML (§4.7, §4.8): mismo concepto que Puppeteer (se diseña en HTML y CSS),
 * pero sin navegador. Las plantillas están en resources/plantillas y deben ser XHTML bien formado.
 */
@Component
public class GeneradorPdf {

    private final TemplateEngine motor;

    public GeneradorPdf() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("plantillas/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.XML);
        resolver.setCharacterEncoding("UTF-8");
        this.motor = new TemplateEngine();
        this.motor.setTemplateResolver(resolver);
    }

    public byte[] generar(String plantilla, Map<String, Object> variables) throws IOException {
        String html = motor.process(plantilla, new Context(Locale.of("es", "CO"), variables));
        String baseUrl = new ClassPathResource("static/").getURL().toExternalForm();
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, baseUrl);
            builder.toStream(salida);
            builder.run();
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el PDF", e);
        }
    }
}
