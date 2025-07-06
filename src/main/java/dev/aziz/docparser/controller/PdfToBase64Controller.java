package dev.aziz.docparser.controller;

import dev.aziz.docparser.service.AIRequestSenderService;
import dev.aziz.docparser.service.PdfToBase64Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfToBase64Controller {

    private final PdfToBase64Service pdfToBase64Service;
    private final AIRequestSenderService gptRequestSenderService;

    @PostMapping("/convert")
    public ResponseEntity<String> convertPdfToBase64(@RequestParam("file") MultipartFile file) {
        System.out.println("Converting file method started:");

        List<String> base64Images = pdfToBase64Service.convertPdfToBase64Images(file);

//        base64Images.forEach(System.out::println);

        String response = gptRequestSenderService.sendToGptVision(base64Images);

        System.out.println("Converting file method ended.");
        return ResponseEntity.ok(response);
    }


    @PostMapping("/convert/gemini")
    public ResponseEntity<String> convertPdfToBase64WithGemini(@RequestParam("file") MultipartFile file) {
        System.out.println("Converting file with Gemini method started:");

        List<String> base64Images = pdfToBase64Service.convertPdfToBase64Images(file);

//        base64Images.forEach(System.out::println);

        String response = gptRequestSenderService.sendToGemini(base64Images);

        System.out.println("Converting file with Gemini method ended.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/to-base64")
    public ResponseEntity<?> convertToBase64(@RequestParam("file") MultipartFile file) {
        try {
            byte[] imageBytes = file.getBytes();
            String base64 = Base64.getEncoder().encodeToString(imageBytes);

            return ResponseEntity.ok().body(
                    "{\"base64\": \"data:image/png;base64," + base64 + "\"}"
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to convert image: " + e.getMessage());
        }
    }

    @PostMapping("/convert/new/gemini")
    public ResponseEntity<String> convertPdfToBase64WholeImagesToGemini(@RequestParam("file") MultipartFile file) {
        System.out.println("Converting file with Gemini method started:");

        List<String> base64Images = pdfToBase64Service.convertPdfToBase64WholeImages(file);

//        base64Images.forEach(System.out::println);

        String response = gptRequestSenderService.sendToGemini(base64Images);

        System.out.println("Converting file with Gemini method ended.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/convert/multiple/gemini")
    public ResponseEntity<String> convertPdfToBase64WholePdf(@RequestParam("file") MultipartFile file) {
        System.out.println("Converting file with Gemini method started:");

//        List<String> base64Images = pdfToBase64Service.convertPdfToBase64WholeImages(file);
        List<List<String>> base64Images = pdfToBase64Service.convertPdfToBase64AllPages(file);
        System.out.println("convertPdfToBase64AllPages ended.");
//        base64Images.forEach(System.out::println);

        String response = gptRequestSenderService.extractAllPagesFromGeminiWithParallelism(base64Images);

        System.out.println("Converting file with Gemini method ended.");
        return ResponseEntity.ok(response);
    }

}
