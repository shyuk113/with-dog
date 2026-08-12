package com.example.withdog.global.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String OAUTH_STATE_PREFIX = "oauth:state:";

    public void saveRefreshToken(Long userId, String refreshToken, long expireSeconds){
        stringRedisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + userId, refreshToken,expireSeconds, TimeUnit.SECONDS);
    }

    public String getRefreshToken(Long userId){
        return stringRedisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
    }

    public void deleteRefreshToken(Long userId){
        stringRedisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    public void addBlackList(String accessToken, long expireSeconds){
        stringRedisTemplate.opsForValue().set(BLACKLIST_PREFIX + accessToken, "logout", expireSeconds, TimeUnit.SECONDS);
    }

    public Boolean isBlackList(String accessToken){
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(BLACKLIST_PREFIX + accessToken));
    }

    public void saveOAuthState(String state, long expireSeconds){
        stringRedisTemplate.opsForValue().set(OAUTH_STATE_PREFIX + state, "valid", expireSeconds, TimeUnit.SECONDS);
    }

    public boolean validateAndConsumeOAuthState(String state){
        Boolean deleted = stringRedisTemplate.delete(OAUTH_STATE_PREFIX + state);
        return Boolean.TRUE.equals(deleted);
    }
}
