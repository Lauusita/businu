package com.lau.api.servicios;

import com.lau.api.exceptions.BusinessException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

/**
 * Convierte una foto (JPG o PNG) en un PDF de una página, para que todos los soportes y evidencias
 * se almacenen como PDF. La imagen se ajusta a una hoja carta con margen, sin deformarla; la
 * orientación de la hoja sigue la de la foto. Un JPG se incrusta sin recomprimir.
 */
@Component
public class ConversorPdf {

    private static final float MARGEN = 36f;   // 0,5 pulgadas

    public byte[] imagenAPdf(byte[] imagen, String nombre) {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            PDImageXObject img;
            try {
                img = PDImageXObject.createFromByteArray(doc, imagen, nombre);
            } catch (IOException | IllegalArgumentException e) {
                throw new BusinessException("La imagen está dañada o no es un JPG/PNG válido");
            }
            boolean horizontal = img.getWidth() > img.getHeight();
            PDRectangle hoja = horizontal
                    ? new PDRectangle(PDRectangle.LETTER.getHeight(), PDRectangle.LETTER.getWidth())
                    : PDRectangle.LETTER;
            PDPage pagina = new PDPage(hoja);
            doc.addPage(pagina);

            float ancho = hoja.getWidth() - 2 * MARGEN;
            float alto = hoja.getHeight() - 2 * MARGEN;
            float escala = Math.min(ancho / img.getWidth(), alto / img.getHeight());
            float w = img.getWidth() * escala;
            float h = img.getHeight() * escala;
            try (PDPageContentStream contenido = new PDPageContentStream(doc, pagina)) {
                contenido.drawImage(img, (hoja.getWidth() - w) / 2, (hoja.getHeight() - h) / 2, w, h);
            }
            doc.save(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo convertir la imagen a PDF", e);
        }
    }
}
