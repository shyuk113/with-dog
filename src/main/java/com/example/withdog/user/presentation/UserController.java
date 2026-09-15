package com.example.withdog.user.presentation;

import com.example.withdog.user.application.UserService;
import com.example.withdog.user.application.dto.UpdateUserProfileRequest;
import com.example.withdog.user.application.dto.UserProfileResponse;
import com.example.withdog.user.application.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //유저 프로필 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserInfo(@PathVariable Long id, @AuthenticationPrincipal Long userId){
        return ResponseEntity.ok(userService.getUserDetails(id, userId));
    }

    //유저 프로필 수정
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUserInfo(@RequestBody UpdateUserProfileRequest request, @PathVariable Long id, @AuthenticationPrincipal Long userId){
        userService.updateUserProfile(request, id, userId);
        return ResponseEntity.noContent().build();
    }

    //유저 프로필 이미지 업로드
    @PostMapping(value = "/{id}/image", consumes = "multipart/form-data")
    public ResponseEntity<UserResponse> updateProfileImage(@PathVariable Long id, @RequestParam("image") MultipartFile image, @AuthenticationPrincipal Long userId){
        return ResponseEntity.ok(userService.updateProfileImage(id, userId, image));
    }
}
