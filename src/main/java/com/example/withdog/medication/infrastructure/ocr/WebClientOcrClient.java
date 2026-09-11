package com.example.withdog.medication.infrastructure.ocr;

import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.medication.infrastructure.ocr.dto.AiOcrResponse;
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
public class WebClientOcrClient implements OcrClient {

    private final WebClient aiWebClient;

    @Override
    public OcrResult extractMedicationInfo(byte[] imageBytes, String filename) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        });

        AiOcrResponse response;
        try {
            response = aiWebClient.post()
                    .uri("/ocr/medication")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(AiOcrResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
        } catch (Exception e) {
            log.error("OCR 서버 호출 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.OCR_SERVER_ERROR);
        }

        if (response == null) {
            throw new BusinessException(ErrorCode.OCR_SERVER_ERROR);
        }

        return new OcrResult(response.rawText(), response.drugName(), response.dosage(), response.hospitalName());
    }
}
