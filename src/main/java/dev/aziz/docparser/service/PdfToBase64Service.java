package dev.aziz.docparser.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class PdfToBase64Service {

    private static final String OUTPUT_DIR = "src/main/resources/generated";

    public List<String> convertPdfToBase64Images(MultipartFile file) {
        List<String> base64Images = new ArrayList<>();
        System.out.println("\nSize of the list before: " + base64Images.size());
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            for (int i = 0; i < pageCount; i++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(i, 300); // High quality ( I used 700 before)

                int w = image.getWidth();
                int h = image.getHeight();

                // Define and process 4 quadrants
                BufferedImage[] quadrants = new BufferedImage[]{
                        image.getSubimage(0, 0, w / 2, h / 2),           // top-left
                        image.getSubimage(w / 2, 0, w / 2, h / 2),       // top-right
                        image.getSubimage(0, h / 2, w / 2, h / 2),       // bottom-left
                        image.getSubimage(w / 2, h / 2, w / 2, h / 2)    // bottom-right
                };

                for (int q = 0; q < quadrants.length; q++) {
                    BufferedImage part = quadrants[q];

                    // Write to disk
                    File outputFile = new File(OUTPUT_DIR + "/page_" + (i + 1) + "_part_" + (q + 1) + ".png");
                    ImageIO.write(part, "png", outputFile);

                    // Convert to base64
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        ImageIO.write(part, "png", baos);
                        String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                        base64Images.add("data:image/png;base64," + base64);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to process PDF", e);
        }


/*    public List<String> convertPdfToBase64Images(MultipartFile file) {
        List<String> base64Images = new ArrayList<>();

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            for (int i = 0; i < pageCount; i++) {

//                BufferedImage image = pdfRenderer.renderImageWithDPI(i, 300); // 300 DPI
                BufferedImage image = pdfRenderer.renderImageWithDPI(i, 500); // 300 DPI

                File imageFile = new File(OUTPUT_DIR + "/page_" + (i + 1) + ".png");
                ImageIO.write(image, "png", imageFile);

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     FileInputStream fis = new FileInputStream(imageFile)) {

                    byte[] imageBytes = fis.readAllBytes();
                    String base64 = Base64.getEncoder().encodeToString(imageBytes);
                    base64Images.add("data:image/png;base64," + base64);
                }

//                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//                    ImageIO.write(image, "png", baos);
//                    String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
//                    base64Images.add("data:image/png;base64," + base64);
//
//                }
            }*/
        System.out.println("\nSize of the list after: " + base64Images.size());
        return base64Images;
    }
}
