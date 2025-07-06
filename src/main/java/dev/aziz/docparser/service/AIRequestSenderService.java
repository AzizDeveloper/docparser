package dev.aziz.docparser.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIRequestSenderService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${spring.ai.gemini.api-key}")
    private String geminiApiKey;

    private static final String OUTPUT_DIR = "src/main/resources/generated";

    //    public String sendToGptVision(String base64Image) {
    public String sendToGptVision(List<String> base64Images) {
        String endpoint = "https://api.openai.com/v1/chat/completions";
// Daurens prevPromptsExamples
// Ты — профессионал по техническому анализу строительных спецификаций. Всегда анализируй и отвечай, используя исключительно данные из присланного пользователем файла. Игнорируй любые сторонние предположения и не подставляй значения “по умолчанию”. Итог всегда представляй в виде табличного JSON: в отдельной секции “specification” — базовая информация о документе, в “materials” — только сырьевые элементы с количеством или массой больше нуля, каждый тип арматурного стержня — отдельная позиция по диаметру. Строго соблюдай структуру, приведённую в примере, и не добавляй лишних комментариев или описаний.

//                    {
//                      "role": "system",
//                      "content": "You are a precise OCR tool. Extract all visible text and numbers exactly as they appear, including formatting, spacing, and alignment. Do not correct or interpret data—just transcribe it accurately."
//                    },

// There are four images, but they are actually first page of the pdf file. Order: Top left, top right, bottom left, bottom right. Don't response with like top left or top right, but create one whole page of information. Stick them together. Please extract all numbers and text from these images exactly as they appear. Images can have cyrillic symbols.
//  Есть четыре изображения, но на самом деле это первая страница файла pdf. Порядок: верхний левый, верхний правый, нижний левый, нижний правый. Не отвечайте типа верхний левый или верхний правый, а создайте одну целую страницу информации. Склейте их вместе. Пожалуйста, извлеките все числа и текст из этих изображений точно так, как они появляются. Изображения могут содержать кириллические символы.
//
//        String jsonRequest = """
//                {
//                  "model": "gpt-4.1",
//                  "messages": [
//                    {
//                      "role": "system",
//                      "content": "You are a precise OCR tool. Extract all visible text and numbers exactly as they appear, including formatting, spacing, and alignment. Do not correct or interpret data—just transcribe it accurately."
//                    },
//                    {
//                      "role": "user",
//                      "content": [
//                        {
//                          "type": "image_url",
//                          "image_url": {
//                            "url": "%s"
//                          }
//                        },
//                        {
//                          "type": "image_url",
//                          "image_url": {
//                            "url": "%s"
//                          }
//                        },
//                        {
//                          "type": "image_url",
//                          "image_url": {
//                            "url": "%s"
//                          }
//                        },
//                        {
//                          "type": "image_url",
//                          "image_url": {
//                            "url": "%s"
//                          }
//                        },
//                        {
//                          "type": "text",
//                          "text": "There are four images, but they are actually first page of the pdf file. Order: Top left, top right, bottom left, bottom right. Don't response with like top left or top right, but create one whole page of information. Stick them together. Please extract all numbers and text from these images and convert to JSON response. If products or items have the same name then attach it's unique field to it's name. Images can have cyrillic symbols."
//                        }
//                      ]
//                    }
//                  ]
//                }
//                """.formatted(base64Images.get(0), base64Images.get(1), base64Images.get(2), base64Images.get(3));
////                """.formatted(base64Images.get(1), base64Images.get(2), base64Images.get(3), base64Images.get(4));

        // Add text object
        String gptPrompt = "";
        try {
            // Replace with the actual path to your prevPromptsExamples file
//            Path filePath = Path.of("src/main/resources/gptprompt.txt");
            Path filePath = Path.of("src/main/resources/testprompt.txt");
//            Path filePath = Path.of("src/main/resources/gptweightprompt.txt");

            // Read entire content into one String
            gptPrompt = Files.readString(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("GPT Prompt: \n" + gptPrompt);

        ObjectMapper mapper = new ObjectMapper();

// Content array: a list of maps
        List<Map<String, Object>> contentArray = new ArrayList<>();

// Add image objects

/*
        for (String base64Image : base64Images) {
            Map<String, Object> imageObject = new HashMap<>();
            imageObject.put("type", "image_url");

            Map<String, String> imageUrl = new HashMap<>();
            imageUrl.put("url", base64Image);

            imageObject.put("image_url", imageUrl);
            contentArray.add(imageObject);
        }
        */

        for (int i = 0; i < 4; i++) {
            Map<String, Object> imageObject = new HashMap<>();
            imageObject.put("type", "image_url");

            Map<String, String> imageUrl = new HashMap<>();
            imageUrl.put("url", base64Images.get(i));

            imageObject.put("image_url", imageUrl);
            contentArray.add(imageObject);
        }

        Map<String, Object> textObject = new HashMap<>();
        textObject.put("type", "text");
        textObject.put("text", gptPrompt);

        /*textObject.put("text", "There are images, but they are actually pages of the pdf file. " +
                "Order: Top left, top right, bottom left, bottom right. Every 4 images are 1 pdf page. " +
                "Don't response with like top left or top right, but create one whole page of information. " +
                "Stick them together. Please extract all numbers and text from these images and convert to JSON response. " +
                "If products or items have the same name then attach it's unique field to it's name. " +
                "Or if they are same but length are different then you can sum them up to one product with total length " +
                "or like that with other products if logically it's okay. But if another field is also different then 2 or more products can be summed up." +
                "Images can have cyrillic symbols. I do not need any other words than JSON response in the response." +
                "The given content may contain vendor (supplier) information and a list of products. \n" +
                "                    Your task is to extract only the relevant fields and return the data as structured JSON objects.\n" +
                "                    \n" +
                "                    Ignore any unrelated data.\n" +
                "                    If there are no relevant data then response: No relevant data found.\n" +
                "                    If you couldn't read the file properly or text doesn't exists then response: No text data found.\n" +
                "                    ---\n" +
                "                    \n" +
//                "                    Vendor fields to extract (set missing fields to null if not found):\n" +
                "                    Vendor fields to extract (skip missing fields if not found):\n" +
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
//                "                    Product fields to extract (can be multiple products, set missing fields to null):\n" +
                "                    Product fields to extract (can be multiple products, skip missing fields): \n" +
                "                    - code : String\n" +
                "                    - productName : String\n" +
                "                    - List<ProductType> internalProducts (if some products contains of another product then it must be placed inside of his internalProducts list)\n" +
                "                    - ProductLevel productLevel\n" +
                "                    - price : BigDecimal\n" +
                "                    - amount : BigDecimal\n" +
                "                    - warehouse : String\n" +
                "                    - materialMeasureType : String\n" +
                "                    ProductLevel is enum class and it's values: LEVEL_1, LEVEL_2, LEVEL_3.\n" +
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
                "                    when creating result use those enums like UNITS or TONS or etc, not raw т or кг.\n" +
                "MANDATORY LOGIC:\n" +
                "1. There are 3 levels:\n" +
                "   - LEVEL_1: Raw Materials\n" +
                "   - LEVEL_2: Detail (must be made ONLY from LEVEL_1)\n" +
                "   - LEVEL_3: Finished Product (can be made from LEVEL_1 and/or LEVEL_2)\n" +
                "2. If a product (LEVEL_2 or LEVEL_3) does NOT contain internalProducts (i.e., sub-components), then it must be considered a LEVEL_1 product instead.\n" +
                "If there is no clear description about which product is what level then main product is level 3 and others level 1 and use nested structure." +
                "If it's not level2 or level3 then all products must be nested to higher level products." +
                "3. You must logically detect which items are actual products. Ignore rows or items that are clearly not products (e.g., headers, metadata, or totals).\n" +
                "4. If multiple products have the same or very similar name (e.g., same Cyrillic root word), sum their amounts. \n" +
                " If weight, lengths (millimeter or centimeter -> convert them to meter) and amount are given, calculate their total amount = length × amount and put measure type to meter. Then group by product name and sum.\n" +
                "5. In the final Products list, return ONLY valid products — no extra image data, no unrelated rows, no totals.\n" +
                "6. Cyrillic product names must be OCRed correctly. Pay extra attention to the first 3–4 characters. DO NOT guess or replace letters incorrectly.\n " +
                "                    Do NOT include any extra fields not mentioned above.");*/
//        textObject.put("text", "There are images, but they are actually pages of the pdf file. Pdf can have more pages than one, I need all pdf pages information not only first page (first 4 images). I need you to handle all pages from. Like if pdf has 3 pages then images could be 12 and you must handle all 12 pages. If you cannot handle all images with some problem please declare that in the beginning of the response. Images order is like these: 1 - Top left, 2- top right, 3- bottom left, 4 - bottom right. From those 4 images create one pdf page information, do not response like page 1, page 2, they are images of one page. So response with pdf page 1 (image page1 or etc). Images can have cyrillic symbols. Response with table if needed. Also add information about how many images you got in the beginning.");
//        textObject.put("text", "You MUST process ALL provided images. Do NOT summarize just the first 4. Process ALL images grouped in 4s per page, and return full extracted text or tables for EACH PDF page.\n" +
//                "If you cannot do so due to token or model limitations, reply with a message that processing all pages is not possible. Also add how many images did you get.\n");
        contentArray.add(textObject);

// User message
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", contentArray);

// System message
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a precise OCR tool. Extract all visible text and numbers exactly as they appear, including formatting, spacing, and alignment. Do not correct or interpret data—just transcribe it accurately.");

// Message array
        List<Object> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.add(userMessage);

// Root object
        Map<String, Object> root = new HashMap<>();
        root.put("model", "gpt-4.1");
        root.put("messages", messages);
        root.put("temperature", 0);

//        System.out.println("root:");
//        System.out.println(root);

// Convert to JSON string
        String jsonRequest = null;
        try {
            jsonRequest = mapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            log.error("Error while creating JSON object", e);
            throw new RuntimeException(e);
        }


        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

//        System.out.println("jsonRequest:");
//        System.out.println(jsonRequest);

        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);

//        System.out.println(response.getBody());

//        return response.getBody();
        Object json = null;
        String prettyJson = null;
        try {
            json = mapper.readValue(response.getBody(), Object.class);
            prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode rootResponse = null;
        try {
            rootResponse = mapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String onlyResponse = rootResponse
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
        return onlyResponse;

    }

    public String sendToGemini(List<String> base64Images) {
//        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey;
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent?key=" + geminiApiKey;

        String geminiPrompt = "";
        try {
            // Replace with the actual path to your prevPromptsExamples file
//            Path filePath = Path.of("src/main/resources/gptprompt.txt");
            Path filePath = Path.of("src/main/resources/testprompt.txt");
//            Path filePath = Path.of("src/main/resources/gptweightprompt.txt");

            // Read entire content into one String
            geminiPrompt = Files.readString(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectMapper mapper = new ObjectMapper();

        // Content parts
        List<Map<String, Object>> parts = new ArrayList<>();

        // Add image parts (up to 4 as in original, adjust as needed)
        for (int i = 0; i < 4 && i < base64Images.size(); i++) {
            Map<String, Object> imagePart = new HashMap<>();
            Map<String, String> inlineData = new HashMap<>();
            inlineData.put("mimeType", "image/png");  // or image/jpeg if that's your base64
            inlineData.put("data", base64Images.get(i).replace("data:image/png;base64,", ""));

            imagePart.put("inlineData", inlineData);
            parts.add(imagePart);
        }

        // Add text part (your instruction)
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", geminiPrompt);
        parts.add(textPart);

        // Root request structure
        Map<String, Object> content = new HashMap<>();
        content.put("parts", parts);

        Map<String, Object> root = new HashMap<>();
        root.put("contents", Collections.singletonList(content));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("responseMimeType", "application/json");
        root.put("generationConfig", generationConfig);

        // Build JSON
        String jsonRequest;
        try {
            jsonRequest = mapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            log.error("Error while creating JSON object", e);
            throw new RuntimeException(e);
        }

//        System.out.println("Gemini JSON request:");
//        System.out.println(jsonRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
//        System.out.println("Gemini response:");
//        System.out.println(response.getBody());

        JsonNode rootResponse = null;
        try {
            rootResponse = mapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String geminiResponse = rootResponse
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();

        System.out.println("Gemini Response: " + geminiResponse);

        JsonNode actualJsonNode;

        byte[] excelBytes = null;
        try {

            actualJsonNode = mapper.readTree(rootResponse
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text").asText());

            excelBytes = generateExcelFromJson(actualJsonNode);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = LocalDateTime.now().format(formatter);

            saveExcelToResources(excelBytes, "products_" + timestamp + ".xlsx");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return geminiResponse;
    }

//}

//before
/*
*         String jsonRequest = """
                {
                  "model": "gpt-4.1",
                  "messages": [
                    {
                      "role": "system",
                      "content": "You are a precise OCR tool. Extract all visible text and numbers exactly as they appear, including formatting, spacing, and alignment. Do not correct or interpret data—just transcribe it accurately."
                    },
                    {
                      "role": "user",
                      "content": [
                        {
                          "type": "image_url",
                          "image_url": {
                            "url": "%s"
                          }
                        },
                        {
                          "type": "image_url",
                          "image_url": {
                            "url": "%s"
                          }
                        },
                        {
                          "type": "image_url",
                          "image_url": {
                            "url": "%s"
                          }
                        },
                        {
                          "type": "image_url",
                          "image_url": {
                            "url": "%s"
                          }
                        },
                        {
                          "type": "text",
                          "text": "There are four images, but they are actually first page of the pdf file. Order: Top left, top right, bottom left, bottom right. Don't response with like top left or top right, but create one whole page of information. Stick them together. Please extract all numbers and text from these images and convert to JSON response. If products or items have the same name then attach it's unique field to it's name. Images can have cyrillic symbols. "
                        }
                      ]
                    }
                  ]
                }
                """.formatted(base64Images.get(0), base64Images.get(1), base64Images.get(2), base64Images.get(3));
* */

//after
/*
*
JSONArray contentArray = new JSONArray();
for (String base64Image : base64Images) {
    JSONObject imageObject = new JSONObject();
    imageObject.put("type", "image_url");
    imageObject.put("image_url", new JSONObject().put("url", base64Image));
    contentArray.put(imageObject);
}

JSONObject textObject = new JSONObject();
textObject.put("type", "text");
textObject.put("text", "Please extract all numbers and text from these images...");

contentArray.put(textObject);

JSONObject messageObject = new JSONObject();
messageObject.put("role", "user");
messageObject.put("content", contentArray);

JSONArray messages = new JSONArray();
messages.put(new JSONObject()
    .put("role", "system")
    .put("content", "You are a precise OCR tool..."));
messages.put(messageObject);

JSONObject root = new JSONObject();
root.put("model", "gpt-4.1");
root.put("messages", messages);

String jsonRequest = root.toString();  // Ready to send

* */

    public String sendToGeminiWithWholePages(List<String> base64Images) {
//        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey;
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent?key=" + geminiApiKey;

        String geminiPrompt = "";
        try {
            // Replace with the actual path to your prevPromptsExamples file
//            Path filePath = Path.of("src/main/resources/gptprompt.txt");
            Path filePath = Path.of("src/main/resources/wholepageprompt.txt");
//            Path filePath = Path.of("src/main/resources/gptweightprompt.txt");

            // Read entire content into one String
            geminiPrompt = Files.readString(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectMapper mapper = new ObjectMapper();

        // Content parts
        List<Map<String, Object>> parts = new ArrayList<>();

        // Add image parts (up to 4 as in original, adjust as needed)
        for (int i = 0; i < base64Images.size(); i++) {
            Map<String, Object> imagePart = new HashMap<>();
            Map<String, String> inlineData = new HashMap<>();
            inlineData.put("mimeType", "image/png");  // or image/jpeg if that's your base64
            inlineData.put("data", base64Images.get(i).replace("data:image/png;base64,", ""));

            imagePart.put("inlineData", inlineData);
            parts.add(imagePart);
        }

        // Add text part (your instruction)
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", geminiPrompt);
        parts.add(textPart);

        // Root request structure
        Map<String, Object> content = new HashMap<>();
        content.put("parts", parts);

        Map<String, Object> root = new HashMap<>();
        root.put("contents", Collections.singletonList(content));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("responseMimeType", "application/json");
        root.put("generationConfig", generationConfig);

        // Build JSON
        String jsonRequest;
        try {
            jsonRequest = mapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            log.error("Error while creating JSON object", e);
            throw new RuntimeException(e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);

        JsonNode rootResponse = null;
        try {
            rootResponse = mapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String geminiResponse = rootResponse
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();

        JsonNode actualJsonNode;

        byte[] excelBytes = null;
        try {

            actualJsonNode = mapper.readTree(rootResponse
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text").asText());

            excelBytes = generateExcelFromJson(actualJsonNode);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = LocalDateTime.now().format(formatter);

            saveExcelToResources(excelBytes, "products_" + timestamp + ".xlsx");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return geminiResponse;
    }

/*    public List<String> extractAllPagesFromGemini(List<List<String>> pdfPagesBase64Images) {
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent?key=" + geminiApiKey;

        String extractPrompt = "";
        String finalPrompt = "";
        try {
            // Replace with the actual path to your prevPromptsExamples file
//            Path filePath = Path.of("src/main/resources/gptprompt.txt");
            // TODO: prompt for extracting from one pdf page ( 4 images ).
            Path filePath = Path.of("src/main/resources/onepageprompt.txt");
            Path finalPromptFilePath = Path.of("src/main/resources/wholeProductTypePrompt.txt");
//            Path filePath = Path.of("src/main/resources/gptweightprompt.txt");

            // Read entire content into one String
            extractPrompt = Files.readString(filePath);
            finalPrompt = Files.readString(finalPromptFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectMapper mapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();

        List<String> pageResponses = new ArrayList<>();

        for (List<String> pageImages : pdfPagesBase64Images) {
            // Content parts for this single page
            List<Map<String, Object>> parts = new ArrayList<>();

            // Add up to 4 images for this page
            for (int i = 0; i < 4 && i < pageImages.size(); i++) {
                Map<String, Object> imagePart = new HashMap<>();
                Map<String, String> inlineData = new HashMap<>();
                inlineData.put("mimeType", "image/png");
                inlineData.put("data", pageImages.get(i).replace("data:image/png;base64,", ""));
                imagePart.put("inlineData", inlineData);
                parts.add(imagePart);
            }

            // Add text part (your extracting prompt)
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", extractPrompt);
            parts.add(textPart);

            // Root request structure
            Map<String, Object> content = new HashMap<>();
            content.put("parts", parts);

            Map<String, Object> root = new HashMap<>();
            root.put("contents", Collections.singletonList(content));

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("responseMimeType", "application/json");
            root.put("generationConfig", generationConfig);

            // Build JSON
            String jsonRequest;
            try {
                jsonRequest = mapper.writeValueAsString(root);
            } catch (JsonProcessingException e) {
                log.error("Error while creating JSON object", e);
                throw new RuntimeException(e);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);

            JsonNode rootResponse;
            try {
                rootResponse = mapper.readTree(response.getBody());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            String geminiResponse = rootResponse
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            System.out.println("Gemini Response for page: " + geminiResponse);

            // Save each page's extracted result to the list
            pageResponses.add(geminiResponse);
            // (Optional) Save each to Excel immediately if needed, or skip for now
        }


    / /        with finalPrompt and pageResponses I must send request to gemini api again with another prompt and that response will be sent back from this method.

        // Return all individual page extracted responses
        return pageResponses;
    }*/

    /*public String extractAllPagesFromGemini(List<List<String>> pdfPagesBase64Images) {
        System.out.println("extractAllPagesFromGemini started.");
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent?key=" + geminiApiKey;

        String extractPrompt = "";
        String finalPrompt = "";
        try {
            // Prompt for per-page extraction
            Path extractPromptPath = Path.of("src/main/resources/onepageprompt.txt");
            // Prompt for final combined analysis
            Path finalPromptPath = Path.of("src/main/resources/wholeProductTypePrompt.txt");

            extractPrompt = Files.readString(extractPromptPath);
            finalPrompt = Files.readString(finalPromptPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectMapper mapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();

        List<String> pageResponses = new ArrayList<>();

        // --- Step 1: Process each page individually ---
        int counter = 1;
        for (List<String> pageImages : pdfPagesBase64Images) {
            System.out.println(counter++ + " is being processed.");
            List<Map<String, Object>> parts = new ArrayList<>();

            // Add up to 4 images per page
            for (int i = 0; i < 4 && i < pageImages.size(); i++) {
                Map<String, Object> imagePart = new HashMap<>();
                Map<String, String> inlineData = new HashMap<>();
                inlineData.put("mimeType", "image/png");
                inlineData.put("data", pageImages.get(i).replace("data:image/png;base64,", ""));
                imagePart.put("inlineData", inlineData);
                parts.add(imagePart);
            }

            // Add text part with extract prompt
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", extractPrompt);
            parts.add(textPart);

            // Build request
            Map<String, Object> content = new HashMap<>();
            content.put("parts", parts);

            Map<String, Object> root = new HashMap<>();
            root.put("contents", Collections.singletonList(content));

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("responseMimeType", "application/json");
            root.put("generationConfig", generationConfig);

            String jsonRequest;
            try {
                jsonRequest = mapper.writeValueAsString(root);
            } catch (JsonProcessingException e) {
                log.error("Error while creating JSON object", e);
                throw new RuntimeException(e);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);

            JsonNode rootResponse;
            try {
                rootResponse = mapper.readTree(response.getBody());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            String geminiResponse = rootResponse
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

//            System.out.println("Gemini Response for page: " + geminiResponse);

            pageResponses.add(geminiResponse);
        }
        System.out.println("Separate page responses accepted.");
        // --- Step 2: Build final summary prompt and send combined request ---
        // Combine page responses into one text
        StringBuilder combinedContent = new StringBuilder();
        for (int i = 0; i < pageResponses.size(); i++) {
            combinedContent.append("Page ").append(i + 1).append(": ").append(pageResponses.get(i)).append("\n\n");
        }

        // Prepare parts for final request
        List<Map<String, Object>> finalParts = new ArrayList<>();

        // Add text part with all extracted page results
        Map<String, Object> combinedPart = new HashMap<>();
        combinedPart.put("text", combinedContent.toString());
        finalParts.add(combinedPart);

        // Add final instruction prompt
        Map<String, Object> finalPromptPart = new HashMap<>();
        finalPromptPart.put("text", finalPrompt);
        finalParts.add(finalPromptPart);

        Map<String, Object> finalContent = new HashMap<>();
        finalContent.put("parts", finalParts);

        Map<String, Object> finalRoot = new HashMap<>();
        finalRoot.put("contents", Collections.singletonList(finalContent));

        Map<String, Object> generationConfigFinal = new HashMap<>();
        generationConfigFinal.put("responseMimeType", "application/json");
        finalRoot.put("generationConfig", generationConfigFinal);

        String finalJsonRequest;
        try {
            finalJsonRequest = mapper.writeValueAsString(finalRoot);
        } catch (JsonProcessingException e) {
            log.error("Error while creating final JSON object", e);
            throw new RuntimeException(e);
        }

        HttpHeaders finalHeaders = new HttpHeaders();
        finalHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> finalEntity = new HttpEntity<>(finalJsonRequest, finalHeaders);

        ResponseEntity<String> finalResponse = restTemplate.postForEntity(endpoint, finalEntity, String.class);

        JsonNode finalRootResponse;
        try {
            finalRootResponse = mapper.readTree(finalResponse.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String finalGeminiResponse = finalRootResponse
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();

        System.out.println("Final Gemini Response accepted");

        // You can also optionally save finalGeminiResponse to file, DB, or Excel here

        return finalGeminiResponse;
    }*/

    // TODO: Add 2 file input logic and 2 file handling logic.
    // TODO: In the last product it's not showing internalProducts as nested.
    public String extractAllPagesFromGeminiWithParallelism(List<List<String>> pdfPagesBase64Images) {
        System.out.println("extractAllPagesFromGemini started.");
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent?key=" + geminiApiKey;

        String extractPrompt = "";
        String finalPrompt = "";
        try {
            // Prompt for per-page extraction
            Path extractPromptPath = Path.of("src/main/resources/onepageprompt.txt");
            // Prompt for final combined analysis
            Path finalPromptPath = Path.of("src/main/resources/wholeProductTypePrompt.txt");

            extractPrompt = Files.readString(extractPromptPath);
            finalPrompt = Files.readString(finalPromptPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectMapper mapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();

        List<String> pageResponses = new ArrayList<>();

        // --- Step 1: Process each page individually ---
        int counter = 1;

        // Multithreading logic is used for performance purposes.
        ExecutorService executor = Executors.newFixedThreadPool(10); // You can adjust to control parallelism

        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (List<String> pageImages : pdfPagesBase64Images) {
            int pageNum = counter++;
            final String extractPromptFinal = extractPrompt;
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                System.out.println(pageNum + " is being processed.");

                List<Map<String, Object>> parts = new ArrayList<>();

                // Add up to 4 images per page
                for (int i = 0; i < 4 && i < pageImages.size(); i++) {
                    Map<String, Object> imagePart = new HashMap<>();
                    Map<String, String> inlineData = new HashMap<>();
                    inlineData.put("mimeType", "image/png");
                    inlineData.put("data", pageImages.get(i).replace("data:image/png;base64,", ""));
                    imagePart.put("inlineData", inlineData);
                    parts.add(imagePart);
                }

                // Add text part with extract prompt
                Map<String, Object> textPart = new HashMap<>();
                textPart.put("text", extractPromptFinal);
                parts.add(textPart);

                // Build request
                Map<String, Object> content = new HashMap<>();
                content.put("parts", parts);

                Map<String, Object> root = new HashMap<>();
                root.put("contents", Collections.singletonList(content));

                Map<String, Object> generationConfig = new HashMap<>();
                generationConfig.put("responseMimeType", "application/json");
                root.put("generationConfig", generationConfig);

                String jsonRequest;
                try {
                    jsonRequest = mapper.writeValueAsString(root);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error while creating JSON object", e);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);

                JsonNode rootResponse;
                try {
                    rootResponse = mapper.readTree(response.getBody());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error while parsing Gemini response", e);
                }

                String geminiResponse = rootResponse
                        .path("candidates")
                        .get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text")
                        .asText();

                return geminiResponse;

            }, executor);
            futures.add(future);
        }

        // Wait for all to complete and collect results
        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        try {
            Files.createDirectories(Paths.get(OUTPUT_DIR));
            Path filePath = Paths.get(OUTPUT_DIR, "response.txt");
            Files.write(
                    filePath,
                    results,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // Add to your pageResponses list
        pageResponses.addAll(results);

        // Shutdown executor
        executor.shutdown();


        System.out.println("Separate page responses accepted.");
        // --- Step 2: Build final summary prompt and send combined request ---
        // Combine page responses into one text
        StringBuilder combinedContent = new StringBuilder();
        for (int i = 0; i < pageResponses.size(); i++) {
            combinedContent.append("Page ").append(i + 1).append(": ").append(pageResponses.get(i)).append("\n\n");
        }

        // Prepare parts for final request
        List<Map<String, Object>> finalParts = new ArrayList<>();

        // Add text part with all extracted page results
        Map<String, Object> combinedPart = new HashMap<>();
        combinedPart.put("text", combinedContent.toString());
        finalParts.add(combinedPart);

        // Add final instruction prompt
        Map<String, Object> finalPromptPart = new HashMap<>();
        finalPromptPart.put("text", finalPrompt);
        finalParts.add(finalPromptPart);

        Map<String, Object> finalContent = new HashMap<>();
        finalContent.put("parts", finalParts);

        Map<String, Object> finalRoot = new HashMap<>();
        finalRoot.put("contents", Collections.singletonList(finalContent));

        Map<String, Object> generationConfigFinal = new HashMap<>();
        generationConfigFinal.put("responseMimeType", "application/json");
        finalRoot.put("generationConfig", generationConfigFinal);

        String finalJsonRequest;
        try {
            finalJsonRequest = mapper.writeValueAsString(finalRoot);
        } catch (JsonProcessingException e) {
            log.error("Error while creating final JSON object", e);
            throw new RuntimeException(e);
        }

        HttpHeaders finalHeaders = new HttpHeaders();
        finalHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> finalEntity = new HttpEntity<>(finalJsonRequest, finalHeaders);

        ResponseEntity<String> finalResponse = restTemplate.postForEntity(endpoint, finalEntity, String.class);

        JsonNode finalRootResponse;
        try {
            finalRootResponse = mapper.readTree(finalResponse.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String finalGeminiResponse = finalRootResponse
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();

        System.out.println("Final Gemini Response accepted");

        // You can also optionally save finalGeminiResponse to file, DB, or Excel here

        return finalGeminiResponse;
    }


    public byte[] generateExcelFromJson(JsonNode json) throws IOException {
        System.out.println("generateExcelFromJson started!!!");
        System.out.println("generateExcelFromJson json: " + json);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Product Info");

        CellStyle boldStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldStyle.setFont(boldFont);

//        int rowIdx = 0;
        AtomicInteger rowIdx = new AtomicInteger(0);

        // Vendor info
//        Row vendorRow1 = sheet.createRow(rowIdx++);
        Row vendorRow1 = sheet.createRow(rowIdx.getAndIncrement());
//        vendorRow1.createCell(0).setCellValue("Vendor Name:");
        vendorRow1.createCell(0).setCellValue("Наименование поставщика:");
        vendorRow1.createCell(1).setCellValue(json.path("vendor").path("name").asText());

        Row vendorRow2 = sheet.createRow(rowIdx.getAndIncrement());
        vendorRow2.createCell(0).setCellValue("Город поставщика:");
        vendorRow2.createCell(1).setCellValue(json.path("vendor").path("city").asText());

        rowIdx.getAndIncrement(); // empty row

        // Header
        Row headerRow = sheet.createRow(rowIdx.getAndIncrement());
//        String[] headers = {"Product Name", "Code", "Amount", "Measure Type", "Product Level", "Internal Products Names"};
        String[] headers = {"Наименование", "Код", "Количество", "Ед. измерения", "Уровень", "Внутренние продукты"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(boldStyle);
        }

        // Helper method to add one product row
        BiConsumer<JsonNode, String> addProductRow = (product, internalNames) -> {
            Row row = sheet.createRow(rowIdx.getAndIncrement());
            row.createCell(0).setCellValue(product.path("productName").asText());
            row.createCell(1).setCellValue(product.path("code").asText());
            row.createCell(2).setCellValue(product.path("amount").asDouble());
            row.createCell(3).setCellValue(product.path("materialMeasureType").asText());
            row.createCell(4).setCellValue(product.path("productLevel").asText());
            row.createCell(5).setCellValue(internalNames != null ? internalNames : "");
        };

        for (JsonNode product : json.path("products")) {
            List<String> internalNames = new ArrayList<>();
            for (JsonNode internal : product.path("internalProducts")) {
                internalNames.add(internal.path("productName").asText());
            }

            // Add parent product row with internal names
            addProductRow.accept(product, String.join(", ", internalNames));

            // Add all internal products as separate rows (without internal products of their own)
            for (JsonNode internal : product.path("internalProducts")) {
                addProductRow.accept(internal, null);
            }
        }

        // Autosize
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        System.out.println("generateExcelFromJson ended!!!");

        return out.toByteArray();
    }

    public void saveExcelToResources(byte[] excelData, String fileName) throws IOException {
        System.out.println("saveExcelToResources started!!!");
        // Define the directory
        String directoryPath = "src/main/resources/results/excel";
        File directory = new File(directoryPath);

        // Create the directory if it doesn't exist
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new IOException("Failed to create directory: " + directoryPath);
            }
        }

        // Prepare the file
        File file = new File(directory, fileName);

        // Write to file
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(excelData);
        }
        System.out.println("saveExcelToResources ended!!!");
    }


}

