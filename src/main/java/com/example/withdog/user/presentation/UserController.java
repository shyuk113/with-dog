package com.example.withdog.user.presentation;

import com.example.withdog.user.application.UserService;
import com.example.withdog.user.application.dto.UpdateUserProfileRequest;
import com.example.withdog.user.application.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //유저 프로필 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserInfo(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserDetails(id));
    }

    //유저 프로필 수정
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUserInfo(@RequestBody UpdateUserProfileRequest request, @PathVariable Long id){
        userService.updateUserProfile(request, id);
        return ResponseEntity.noContent().build();
    }
}
