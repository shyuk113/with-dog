package com.example.withdog.dog.presentation;

import com.example.withdog.dog.application.DogService;
import com.example.withdog.dog.application.dto.CreateDogRequest;
import com.example.withdog.dog.application.dto.DogResponse;
import com.example.withdog.dog.application.dto.UpdateDogRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dogs")
@RequiredArgsConstructor
public class DogController {

    private final DogService dogService;

    //강아지 프로핋 생성
    @PostMapping
    public ResponseEntity<DogResponse> createDog(@Valid @RequestBody CreateDogRequest request, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dogService.createDog(request,userId));
    }

    //강아지 프로필 조회
    @GetMapping("/{id}")
    public ResponseEntity<DogResponse> getDog(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(dogService.getDog(id,userId));
    }

    @GetMapping
    public ResponseEntity<List<DogResponse>> getAllDogs(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(dogService.getDogs(userId));
    }

    //강아지 프로필 수정
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateDog(@PathVariable Long id, @Valid @RequestBody UpdateDogRequest request, @AuthenticationPrincipal Long userId) {
        dogService.updateDog(id, request,userId);
        return ResponseEntity.noContent().build();
    }

    //강아지 프로필 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDog(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        dogService.deleteDog(id, userId);
        return ResponseEntity.noContent().build();
    }

}
