package com.example.withdog.feed.presentation;

import com.example.withdog.feed.application.FeedService;
import com.example.withdog.feed.application.dto.RegionFeedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    //내 동네 최신 글 피드 (커서 기반 페이징: 첫 요청은 cursor 없이, 이후엔 응답의 nextCursor 전달)
    @GetMapping("/region")
    public ResponseEntity<RegionFeedResponse> getRegionFeed(@AuthenticationPrincipal Long userId,
                                                            @RequestParam(required = false) Long cursor,
                                                            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(feedService.getRegionFeed(userId, cursor, size));
    }
}
