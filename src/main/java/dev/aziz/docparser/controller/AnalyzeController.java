package dev.aziz.docparser.controller;

//import dev.aziz.docparser.service.FileReaderService;
//import dev.aziz.docparser.service.GeminiService;
import dev.aziz.docparser.service.PdfToTextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/analyze")
@RequiredArgsConstructor
public class AnalyzeController {

//    private final FileReaderService fileReader;
//    private final GeminiService geminiService;
    private final PdfToTextService pdfService;

    @GetMapping("/pdf-text")
    public String getText() {
        return pdfService.extractTextFromPdf();
    }

    @GetMapping
    public ResponseEntity<String> analyzeFile(@RequestParam String fileName) throws IOException {
//        String docText = fileReader.readFromResource(fileName);

        String prompt = """
            Extract invoice details from the document.
            Return JSON with fields: customerName, invoiceDate, totalAmount.
        """;

//        String response = geminiService.callGemini(prompt, docText);
//        return ResponseEntity.ok(response);
        return null;
    }
}

