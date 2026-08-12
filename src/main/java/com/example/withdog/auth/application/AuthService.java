package com.example.withdog.auth.application;

import com.example.withdog.auth.application.dto.LoginRequest;
import com.example.withdog.auth.application.dto.SignupRequest;
import com.example.withdog.auth.application.dto.SignupResponse;
import com.example.withdog.auth.application.dto.TokenResponse;
import com.example.withdog.global.application.RedisTokenService;
import com.example.withdog.global.security.JwtProvider;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTokenService redisTokenService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public SignupResponse signup(SignupRequest request){
        if(userRepository.existsByEmail(request.email())){
            throw new IllegalArgumentException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.createUserLocal(request.name(), request.email(), encodedPassword, request.region());
        userRepository.save(user);
        return SignupResponse.from(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.email()).orElseThrow(()->new IllegalArgumentException("Invalid email"));

        if(!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new IllegalArgumentException("Invalid password");
        }

        return issueTokens(user, false);
    }

    private TokenResponse issueTokens(User user, boolean isNewUser){
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        redisTokenService.saveRefreshToken(user.getId(), refreshToken, refreshTokenExpiration/1000);
        return new TokenResponse(accessToken, refreshToken, accessTokenExpiration/1000, isNewUser);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken){
        if(!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        Long userId = jwtProvider.getUserId(refreshToken);

        String savedToken = redisTokenService.getRefreshToken(userId);
        if(savedToken == null || !savedToken.equals(refreshToken)){
            throw new IllegalArgumentException("Invalid refresh token");
        }

        User user = userRepository.findById(userId).orElseThrow(()->new IllegalArgumentException("Invalid user"));
        return issueTokens(user, false);
    }

    @Transactional
    public void logout(Long userId, String accessToken){
        redisTokenService.deleteRefreshToken(userId);
        redisTokenService.addBlackList(accessToken, accessTokenExpiration/1000);
    }
}
