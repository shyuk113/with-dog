package com.example.withdog.auth.presentation;

import com.example.withdog.auth.application.AuthService;
import com.example.withdog.auth.application.dto.LoginRequest;
import com.example.withdog.auth.application.dto.SignupRequest;
import com.example.withdog.auth.application.dto.SignupResponse;
import com.example.withdog.auth.application.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest request){
        return ResponseEntity.ok(authService.signup(request));
    }

    //로그인
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal  Long userId, @RequestHeader("Authorization") String bearerToken){
        if(bearerToken == null || !bearerToken.startsWith("Bearer ")){
            throw new IllegalArgumentException("Bearer Token is invalid");
        }
        String accessToken = bearerToken.substring(7);
        authService.logout(userId, accessToken);
        return ResponseEntity.noContent().build();
    }

    //토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody Map<String, String> body){
        String refreshToken = body.get("refreshToken");
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }


}
