package com.example.withdog.diet.infrastructure.ocr;

import com.example.withdog.diet.infrastructure.ocr.dto.AiFoodOcrResponse;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!local")
public class WebClientFoodOcrClient implements FoodOcrClient {

    private final WebClient aiWebClient;

    @Override
    public FoodOcrResult extractFoodInfo(byte[] imageBytes, String filename) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        });

        AiFoodOcrResponse response;
        try {
            response = aiWebClient.post()
                    .uri("/ocr/food")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(AiFoodOcrResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
        } catch (Exception e) {
            log.error("사료/간식 OCR 서버 호출 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FOOD_OCR_ERROR);
        }

        if (response == null) {
            throw new BusinessException(ErrorCode.FOOD_OCR_ERROR);
        }

        return new FoodOcrResult(response.rawText(), response.productName(), response.caloriesPerGram());
    }
}
