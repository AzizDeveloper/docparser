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

    private final String OUTPUT_DIR = "src/main/resources/generated";

    public List<String> convertPdfToBase64Images(MultipartFile file) {
        List<String> base64Images = new ArrayList<>();
        System.out.println("\nSize of the list before: " + base64Images.size());
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            for (int i = 0; i < pageCount; i++) {
//                BufferedImage image = pdfRenderer.renderImageWithDPI(i, 300); // High quality ( I used 700 before)
                BufferedImage image = pdfRenderer.renderImageWithDPI(i, 400); // High quality ( I used 700 before)

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

    public List<String> convertPdfToBase64WholeImages(MultipartFile file) {
        List<String> base64Images = new ArrayList<>();
        System.out.println("\nSize of the list before: " + base64Images.size());
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            for (int i = 0; i < pageCount; i++) {
                // Render full page image with DPI
                BufferedImage image = pdfRenderer.renderImageWithDPI(i, 400); // You can adjust DPI (e.g., 300)

                // Optional: write to disk for debugging
                File outputFile = new File(OUTPUT_DIR + "/page_" + (i + 1) + ".png");
                ImageIO.write(image, "png", outputFile);

                // Convert whole image to Base64
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    ImageIO.write(image, "png", baos);
                    String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                    base64Images.add("data:image/png;base64," + base64);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to process PDF", e);
        }
        System.out.println("\nSize of the list after: " + base64Images.size());
        return base64Images;
    }

    public List<List<String>> convertPdfToBase64AllPages(MultipartFile file) {
        System.out.println("convertPdfToBase64AllPages started");
        List<List<String>> allPagesImages = new ArrayList<>();
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            for (int i = 0; i < pageCount; i++) {
                BufferedImage fullImage = pdfRenderer.renderImageWithDPI(i, 400); // or 300 if you like

                int width = fullImage.getWidth();
                int height = fullImage.getHeight();

                int halfWidth = width / 2;
                int halfHeight = height / 2;

                List<String> pageImages = new ArrayList<>();

                // Top-left
                BufferedImage topLeft = fullImage.getSubimage(0, 0, halfWidth, halfHeight);
                // Top-right
                BufferedImage topRight = fullImage.getSubimage(halfWidth, 0, width - halfWidth, halfHeight);
                // Bottom-left
                BufferedImage bottomLeft = fullImage.getSubimage(0, halfHeight, halfWidth, height - halfHeight);
                // Bottom-right
                BufferedImage bottomRight = fullImage.getSubimage(halfWidth, halfHeight, width - halfWidth, height - halfHeight);

                List<BufferedImage> quarters = List.of(topLeft, topRight, bottomLeft, bottomRight);

                for (BufferedImage quarter : quarters) {
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        ImageIO.write(quarter, "png", baos);
                        String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                        pageImages.add("data:image/png;base64," + base64);
                    }
                }

                // Add this page's images to outer list
                allPagesImages.add(pageImages);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to process PDF", e);
        }
        return allPagesImages;
    }

    public List<List<List<String>>> convert2PdfsToBase64AllPages(MultipartFile[] files) {
        if (files == null || files.length != 2) {
            throw new IllegalArgumentException("Exactly 2 PDF files are required");
        }

        List<List<List<String>>> allPdfsPages = new ArrayList<>();

        for (MultipartFile file : files) {
            List<List<String>> pdfPages = new ArrayList<>();

            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                int pageCount = document.getNumberOfPages();

                for (int i = 0; i < pageCount; i++) {
                    BufferedImage fullImage = pdfRenderer.renderImageWithDPI(i, 400);

                    int width = fullImage.getWidth();
                    int height = fullImage.getHeight();
                    int halfWidth = width / 2;
                    int halfHeight = height / 2;

                    List<String> pageImages = new ArrayList<>();

                    BufferedImage topLeft = fullImage.getSubimage(0, 0, halfWidth, halfHeight);
                    BufferedImage topRight = fullImage.getSubimage(halfWidth, 0, width - halfWidth, halfHeight);
                    BufferedImage bottomLeft = fullImage.getSubimage(0, halfHeight, halfWidth, height - halfHeight);
                    BufferedImage bottomRight = fullImage.getSubimage(halfWidth, halfHeight, width - halfWidth, height - halfHeight);

                    List<BufferedImage> quarters = List.of(topLeft, topRight, bottomLeft, bottomRight);

                    for (BufferedImage quarter : quarters) {
                        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                            ImageIO.write(quarter, "png", baos);
                            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                            pageImages.add("data:image/png;base64," + base64);
                        }
                    }

                    pdfPages.add(pageImages);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to process PDF", e);
            }

            allPdfsPages.add(pdfPages);
        }

        return allPdfsPages;
    }

}
