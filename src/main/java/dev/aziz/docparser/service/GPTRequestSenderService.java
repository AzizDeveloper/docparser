package dev.aziz.docparser.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GPTRequestSenderService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${spring.ai.gemini.api-key}")
    private String geminiApiKey;

    //    public String sendToGptVision(String base64Image) {
    public String sendToGptVision(List<String> base64Images) {
        String endpoint = "https://api.openai.com/v1/chat/completions";
// Daurens prompt
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

// Add text object
        Map<String, Object> textObject = new HashMap<>();
        textObject.put("type", "text");
        textObject.put("text", "There are images, but they are actually pages of the pdf file. Order: Top left, top right, bottom left, bottom right. Every 4 images are 1 pdf page. Don't response with like top left or top right, but create one whole page of information. Stick them together. Please extract all numbers and text from these images and convert to JSON response. If products or items have the same name then attach it's unique field to it's name. Images can have cyrillic symbols. I do not need any other words than JSON response in the response." +
                " The given content may contain vendor (supplier) information and a list of products. \n" +
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
                "                    Do NOT include any extra fields not mentioned above.");
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

        System.out.println("root:");
        System.out.println(root);

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

        System.out.println("jsonRequest:");
        System.out.println(jsonRequest);

        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response =
                restTemplate.postForEntity(endpoint, entity, String.class);

        System.out.println(response.getBody());

        return response.getBody();
    }

    public String sendToGemini(List<String> base64Images) {
//        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro-vision:generateContent?key=" + geminiApiKey;
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey;

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
        textPart.put("text", "There are images, but they are actually pages of the pdf file. Order: Top left, top right, bottom left, bottom right. Every 4 images are 1 pdf page. Don't response with like top left or top right, but create one whole page of information. Stick them together. Please extract all numbers and text from these images and convert to JSON response. If products or items have the same name then attach it's unique field to it's name. Images can have cyrillic symbols. I do not need any other words than JSON response in the response.\" +\n" +
                "                \" The given content may contain vendor (supplier) information and a list of products. \\n\" +\n" +
                "                \"                    Your task is to extract only the relevant fields and return the data as structured JSON objects.\\n\" +\n" +
                "                \"                    \\n\" +\n" +
                "                \"                    Ignore any unrelated data.\\n\" +\n" +
                "                \"                    If there are no relevant data then response: No relevant data found.\\n\" +\n" +
                "                \"                    If you couldn't read the file properly or text doesn't exists then response: No text data found.\\n\" +\n" +
                "                \"                    ---\\n\" +\n" +
                "                \"                    \\n\" +\n" +
                "                \"                    Vendor fields to extract (set missing fields to null if not found):\\n\" +\n" +
                "                \"                    - name : String\\n\" +\n" +
                "                \"                    - description : String\\n\" +\n" +
                "                \"                    - email : String\\n\" +\n" +
                "                \"                    - district : String\\n\" +\n" +
                "                \"                    - city : String\\n\" +\n" +
                "                \"                    - address : String\\n\" +\n" +
                "                \"                    - contactName : String\\n\" +\n" +
                "                \"                    - deliveryTime : Integer\\n\" +\n" +
                "                \"                    \\n\" +\n" +
                "                \"                    ---\\n\" +\n" +
                "                \"                    \\n\" +\n" +
                "                \"                    Product fields to extract (can be multiple products, set missing fields to null):\\n\" +\n" +
                "                \"                    - code : String\\n\" +\n" +
                "                \"                    - productName : String\\n\" +\n" +
                "                \"                    - price : BigDecimal\\n\" +\n" +
                "                \"                    - amount : BigDecimal\\n\" +\n" +
                "                \"                    - warehouse : String\\n\" +\n" +
                "                \"                    - materialMeasureType : String\\n\" +\n" +
                "                \"                    \\n\" +\n" +
                "                \"                    UNITS(1, \\\"amount\\\", \\\"Amount\\\", \\\"platform.measureTypes.units\\\"),\\n\" +\n" +
                "                \"                    WEIGHTED_KILO(2, \\\"kg\\\", \\\"KG\\\", \\\"platform.measureTypes.weighted_kilo\\\"),\\n\" +\n" +
                "                \"                    WEIGHTED_GRAM(3, \\\"gr\\\", \\\"GR\\\", \\\"platform.measureTypes.weighted_gram\\\"),\\n\" +\n" +
                "                \"                    VOLUME_L(4, \\\"ltr\\\", \\\"LTR\\\", \\\"platform.measureTypes.volume_litres\\\"),\\n\" +\n" +
                "                \"                    VOLUME_ML(5, \\\"ml\\\", \\\"ML\\\", \\\"platform.measureTypes.volume_mLitres\\\"),\\n\" +\n" +
                "                \"                    SQUARE_M(6, \\\"square meters\\\", \\\"sq. m\\\", \\\"platform.measureTypes.square_meters\\\"),\\n\" +\n" +
                "                \"                    LINEAR_METERS(7, \\\"linear meters\\\", \\\"ln. m\\\", \\\"platform.measureTypes.linear_meters\\\"),\\n\" +\n" +
                "                \"                    CUBIC_M(8, \\\"cubic meters\\\", \\\"cub. m\\\", \\\"platform.measureTypes.cubic_meters\\\"),\\n\" +\n" +
                "                \"                    TONS(9, \\\"tons\\\", \\\"t\\\", \\\"platform.measureTypes.tons\\\");\\n\" +\n" +
                "                \"                    \\n\" +\n" +
                "                \"                    platform.measureTypes.name=Ед.изм.  - means Единица измерения\\n\" +\n" +
                "                \"                    platform.measureTypes.units=шт - штук\\n\" +\n" +
                "                \"                    platform.measureTypes.weighted_kilo=кг - килограмм\\n\" +\n" +
                "                \"                    platform.measureTypes.weighted_gram=г - грамм\\n\" +\n" +
                "                \"                    platform.measureTypes.volume_litres=л - литр\\n\" +\n" +
                "                \"                    platform.measureTypes.volume_mLitres=мл - миллилитр\\n\" +\n" +
                "                \"                    platform.measureTypes.square_meters=м² - квадратный метр\\n\" +\n" +
                "                \"                    platform.measureTypes.linear_meters=п.м. - погонные метры\\n\" +\n" +
                "                \"                    platform.measureTypes.cubic_meters=м³ - кубический метр\\n\" +\n" +
                "                \"                    platform.measureTypes.tons=т - тонна\\n\" +\n" +
                "                \"                    \\n\" +\n" +
                "                \"                    when creating result use those enums like UNITS or TONS or etc, not raw т or кг.\\n\" +\n" +
                "                \"                    \\n\" +\n" +
                "                \"                    Do NOT include any extra fields not mentioned above.");
        parts.add(textPart);

        // Root request structure
        Map<String, Object> content = new HashMap<>();
        content.put("parts", parts);

        Map<String, Object> root = new HashMap<>();
        root.put("contents", Collections.singletonList(content));

        // Build JSON
        String jsonRequest;
        try {
            jsonRequest = mapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            log.error("Error while creating JSON object", e);
            throw new RuntimeException(e);
        }

        System.out.println("Gemini JSON request:");
        System.out.println(jsonRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
        System.out.println("Gemini response:");
        System.out.println(response.getBody());

        return response.getBody();
    }

}

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


