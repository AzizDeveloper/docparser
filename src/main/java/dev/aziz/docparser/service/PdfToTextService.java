package dev.aziz.docparser.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class PdfToTextService {

    private final Tika tika = new Tika();

    public String extractTextFromPdf() {
        try (InputStream is = getClass().getResourceAsStream("/Aziz_Abdukarimov_Java-dev_CV.pdf")) {
            if (is == null) {
                throw new RuntimeException("PDF file not found in resources");
            }
            return tika.parseToString(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from PDF", e);
        }
    }
}

