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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GPTRequestSenderService {

    @Value("${openai.api.key}")
    private String apiKey;

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
        textObject.put("text", "There are images, but they are actually pages of the pdf file. Order: Top left, top right, bottom left, bottom right. Every 4 images are 1 pdf page. Don't response with like top left or top right, but create one whole page of information. Stick them together. Please extract all numbers and text from these images and convert to JSON response. If products or items have the same name then attach it's unique field to it's name. Images can have cyrillic symbols. I do not need any other words than JSON response in the response.");
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
