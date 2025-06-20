package dev.aziz.docparser.controller;

import dev.aziz.docparser.service.GmailEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/doc")
public class DocumentController {

    //    private final TikaDocumentReader tikaReader = new TikaDocumentReader();
    private final GmailEmailService gmailEmailService;

    @Value("${spring.ai.gemini.api-key}")
    private String geminiApiKey;

//    TikaDocumentReader tikaReader = new TikaDocumentReader(new InputStreamResource(file.getInputStream()));

    @PostMapping("/parse")
    public ResponseEntity<String> parseDocument(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Extract text from file using Apache Tika

            TikaDocumentReader tikaReader = new TikaDocumentReader(new InputStreamResource(file.getInputStream()));
            List<Document> documents = tikaReader.read();

            if (documents.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No readable content found in the file.");
            }

            String text = documents.get(0).getText();

            // 2. Prepare Gemini API request
            String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey;
//
//            String instruction = "The following is a product list. " +
//                    "Please extract products fields and vendor/suppliers information in to the entity form." +
//                    "Vendor has these fields: private String name;\n" +
//                    "    private String description;\n" +
//                    "    private String email;\n" +
//                    "    private String district;\n" +
//                    "    private String city;\n" +
//                    "    private String address;\n" +
//                    "    private String contactName;\n" +
//                    "    private Integer deliveryTime;" +
//                    "skip other fields if presented in the file and if some field data is not presented then put null." +
//                    "Product:     " +
//                    "    private String id;\n" +
//                    "    private String code;\n" +
//                    "    private ProductName productName;\n" +
//                    "    private BigDecimal price;\n" +
//                    "    private BigDecimal amount;\n" +
//                    "    private Warehouse warehouse;\n" +
//                    "    private MaterialMeasureType materialMeasureType;" +
//                    "skip other fields if presented in the file and if some field data is not presented then put null." +
//                    " Return structured JSON.\n\n" +
//                    "Here is the text:\n\n";

            String shortPrompt = """
                    You are given a document/file that may contain vendor (supplier) information and a list of products.\nYour task is to extract only the relevant fields and return the data as structured JSON objects.\n\nIgnore any unrelated data.\nIf there are no relevant data then response: No relevant data found.\nIf you couldn't read the file properly or text doesn't exists then response: No text data found.\n---\n\nVendor fields to extract (set missing fields to null if not found):\n- name : String\n- description : String\n- email : String\n- district : String\n- city : String\n- address : String\n- contactName : String\n- deliveryTime : Integer\n\n---\n\nProduct fields to extract (can be multiple products, set missing fields to null):\n- code : String\n- productName : String\n- price : BigDecimal\n- amount : BigDecimal\n- warehouse : String\n- materialMeasureType : String\n\nDo NOT include any extra fields not mentioned above.\n\nReturn a JSON with this structure:\n{\n  "vendor": { ... },\n  "products": [ { ... }, { ... }, ... ]\n}
                    """;

            /*String sprompt = "You are given a document/file that may contain vendor (supplier) information and a list of products. \n" +
                    "                    Your task is to extract only the relevant fields and return the data as structured JSON objects.\n" +
                    "                    \n" +
                    "                    Ignore any unrelated data.\n" +
                    "                    If there are no relevant data then response: No relevant data found.\n" +
                    "                    If you couldn't read the file properly or text doesn't exists then response: No text data found.\n" +
                    "                    ---\n" +
                    "                    \n" +
                    "                    Vendor fields to extract (set missing fields to null if not found):\n" +
                    "                    - name : String\n" +
                    "                    - description : String\n" +
                    "                    - email : String\n" +
                    "                    - district : String\n" +
                    "                    - city : String\n" +
                    "                    - address : String\n" +
                    "                    - contactName : String\n" +
                    "                    - deliveryTime : Integer\n" +
                    "                    \n" +
                    "                    ---\n" +
                    "                    \n" +
                    "                    Product fields to extract (can be multiple products, set missing fields to null):\n" +
                    "                    - code : String\n" +
                    "                    - productName : String\n" +
                    "                    - price : BigDecimal\n" +
                    "                    - amount : BigDecimal\n" +
                    "                    - warehouse : String\n" +
                    "                    - materialMeasureType : String\n" +
                    "                    \n" +
                    "                    UNITS(1, \"amount\", \"Amount\", \"platform.measureTypes.units\"),\n" +
                    "                    WEIGHTED_KILO(2, \"kg\", \"KG\", \"platform.measureTypes.weighted_kilo\"),\n" +
                    "                    WEIGHTED_GRAM(3, \"gr\", \"GR\", \"platform.measureTypes.weighted_gram\"),\n" +
                    "                    VOLUME_L(4, \"ltr\", \"LTR\", \"platform.measureTypes.volume_litres\"),\n" +
                    "                    VOLUME_ML(5, \"ml\", \"ML\", \"platform.measureTypes.volume_mLitres\"),\n" +
                    "                    SQUARE_M(6, \"square meters\", \"sq. m\", \"platform.measureTypes.square_meters\"),\n" +
                    "                    LINEAR_METERS(7, \"linear meters\", \"ln. m\", \"platform.measureTypes.linear_meters\"),\n" +
                    "                    CUBIC_M(8, \"cubic meters\", \"cub. m\", \"platform.measureTypes.cubic_meters\"),\n" +
                    "                    TONS(9, \"tons\", \"t\", \"platform.measureTypes.tons\");\n" +
                    "                    \n" +
                    "                    platform.measureTypes.name=Ед.изм.  - means Единица измерения\n" +
                    "                    platform.measureTypes.units=шт - штук\n" +
                    "                    platform.measureTypes.weighted_kilo=кг - килограмм\n" +
                    "                    platform.measureTypes.weighted_gram=г - грамм\n" +
                    "                    platform.measureTypes.volume_litres=л - литр\n" +
                    "                    platform.measureTypes.volume_mLitres=мл - миллилитр\n" +
                    "                    platform.measureTypes.square_meters=м² - квадратный метр\n" +
                    "                    platform.measureTypes.linear_meters=п.м. - погонные метры\n" +
                    "                    platform.measureTypes.cubic_meters=м³ - кубический метр\n" +
                    "                    platform.measureTypes.tons=т - тонна\n" +
                    "                    \n" +
                    "                    when creating result use those enums like UNITS or TONS or etc, not raw т or кг.\n" +
                    "                    \n" +
                    "                    Do NOT include any extra fields not mentioned above.\n" +
                    "                    \n" +
                    "                    Return a JSON with this structure:\n" +
                    "                    {\n" +
                    "                      \"vendor\": { ... },\n" +
                    "                      \"products\": [ { ... }, { ... }, ... ]\n" +
                    "                    }";*/

            String instruction = """
                    You are given a document/file that may contain vendor (supplier) information and a list of products. 
                    Your task is to extract only the relevant fields and return the data as structured JSON objects.
                    
                    Ignore any unrelated data.
                    If there are no relevant data then response: No relevant data found.
                    If you couldn't read the file properly or text doesn't exists then response: No text data found.
                    ---
                    
                    Vendor fields to extract (set missing fields to null if not found):
                    - name : String
                    - description : String
                    - email : String
                    - district : String
                    - city : String
                    - address : String
                    - contactName : String
                    - deliveryTime : Integer
                    
                    ---
                    
                    Product fields to extract (can be multiple products, set missing fields to null):
                    - code : String
                    - productName : String
                    - price : BigDecimal
                    - amount : BigDecimal
                    - warehouse : String
                    - materialMeasureType : String
                    
                    UNITS(1, "amount", "Amount", "platform.measureTypes.units"),
                    WEIGHTED_KILO(2, "kg", "KG", "platform.measureTypes.weighted_kilo"),
                    WEIGHTED_GRAM(3, "gr", "GR", "platform.measureTypes.weighted_gram"),
                    VOLUME_L(4, "ltr", "LTR", "platform.measureTypes.volume_litres"),
                    VOLUME_ML(5, "ml", "ML", "platform.measureTypes.volume_mLitres"),
                    SQUARE_M(6, "square meters", "sq. m", "platform.measureTypes.square_meters"),
                    LINEAR_METERS(7, "linear meters", "ln. m", "platform.measureTypes.linear_meters"),
                    CUBIC_M(8, "cubic meters", "cub. m", "platform.measureTypes.cubic_meters"),
                    TONS(9, "tons", "t", "platform.measureTypes.tons");
                    
                    platform.measureTypes.name=Ед.изм.  - means Единица измерения
                    platform.measureTypes.units=шт - штук
                    platform.measureTypes.weighted_kilo=кг - килограмм
                    platform.measureTypes.weighted_gram=г - грамм
                    platform.measureTypes.volume_litres=л - литр
                    platform.measureTypes.volume_mLitres=мл - миллилитр
                    platform.measureTypes.square_meters=м² - квадратный метр
                    platform.measureTypes.linear_meters=п.м. - погонные метры
                    platform.measureTypes.cubic_meters=м³ - кубический метр
                    platform.measureTypes.tons=т - тонна
                    
                    when creating result use those enums like UNITS or TONS or etc, not raw т or кг.
                    
                    Do NOT include any extra fields not mentioned above.
                    
                    Return a JSON with this structure:
                    {
                      "vendor": { ... },
                      "products": [ { ... }, { ... }, ... ]
                    }
                    
                    ---
                    
                    Here is the text content:
                    """;

            //                    measureTypes.name=Ед.изм.
            //                    measureTypes.units=шт
            //                    measureTypes.weighted_kilo=кг
            //                    measureTypes.weighted_gram=г
            //                    measureTypes.volume_litres=л
            //                    measureTypes.volume_mLitres=мл
            //                    measureTypes.square_meters=м²
            //                    measureTypes.linear_meters=п.м.
            //                    measureTypes.cubic_meters=м³
            //                    measureTypes.tons=т
//TODO: add material types in russian шт. etc.

            String promptText = instruction + text;  // text is the extracted text from Tika

            Map<String, Object> request = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of(
                                                    "text", promptText
                                            )
                                    )
                            )
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            // 3. Send to Gemini
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(geminiApiUrl, entity, Map.class);

            // 4. Extract response
            var candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            var content = (Map<String, Object>) candidates.get(0).get("content");
            var parts = (List<Map<String, Object>>) content.get("parts");
            String result = (String) parts.get(0).get("text");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/email")
    public ResponseEntity<String> email() {
//        gmailEmailService.fetchLatestEmail();
        return ResponseEntity.ok(gmailEmailService.fetchLatestEmail());
    }
}
