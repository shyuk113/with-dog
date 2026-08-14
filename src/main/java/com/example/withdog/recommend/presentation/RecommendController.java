package com.example.withdog.recommend.presentation;

import com.example.withdog.recommend.application.RecommendService;
import com.example.withdog.recommend.application.dto.RecommendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dogs/{dogId}/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping
    public ResponseEntity<List<RecommendResponse>> recommend(@PathVariable Long dogId, @AuthenticationPrincipal Long userId){
        return ResponseEntity.ok(recommendService.recommend(dogId, userId));
    }
}
