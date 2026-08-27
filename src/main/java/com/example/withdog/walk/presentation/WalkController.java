package com.example.withdog.walk.presentation;

import com.example.withdog.walk.application.WalkService;
import com.example.withdog.walk.application.dto.CreateWalkRequest;
import com.example.withdog.walk.application.dto.UpdateWalkRequest;
import com.example.withdog.walk.application.dto.WalkResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/walks")
@RequiredArgsConstructor
public class WalkController {

    private final WalkService walkService;

    @GetMapping
    public ResponseEntity<Page<WalkResponse>> getWalkHistories(@PageableDefault(size = 15, sort = "startedAt") Pageable pageable, @AuthenticationPrincipal Long userId){
        return ResponseEntity.ok(walkService.getWalkHistories(pageable,userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalkResponse> getWalkHistory(@PathVariable Long id, @AuthenticationPrincipal Long userId){
        return ResponseEntity.ok(walkService.getWalkHistory(id,userId));
    }

    @PostMapping
    public ResponseEntity<WalkResponse> startWalk(@Valid @RequestBody CreateWalkRequest request, @AuthenticationPrincipal Long userId){
        return ResponseEntity.status(HttpStatus.CREATED).body(walkService.createWalk(userId,request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> endWalk(@PathVariable Long id, @Valid @RequestBody UpdateWalkRequest request, @AuthenticationPrincipal Long userId){
        walkService.updateWalk(userId, id, request);
        return ResponseEntity.noContent().build();
    }

    //진행중인 산책 조회
    @GetMapping("/ongoing")
    public ResponseEntity<WalkResponse> getOngoingWalk(@AuthenticationPrincipal Long userId){
        return walkService.getOngoingWalk(userId).map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }
}
