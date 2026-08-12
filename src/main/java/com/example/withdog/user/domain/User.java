package com.example.withdog.user.domain;

import com.example.withdog.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;
    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String region;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    private String providerId;

    @Builder
    private User(String nickname, String name, String email, String password, String region, Provider provider, String providerId) {
        this.nickname = nickname;
        this.name = name;
        this.email = email;
        this.password = password;
        this.region = region;
        this.provider = provider;
        this.providerId = providerId;
    }

    public static User createUserLocal(String name, String email, String password, String region){
        return User.builder()
                .nickname(name)
                .name(name)
                .email(email)
                .password(password)
                .region(region)
                .provider(Provider.LOCAL)
                .build();
    }

    public static User createUserSocial(String nickname, String email, String region, Provider provider, String providerId){
        return User.builder()
                .nickname(nickname)
                .email(email)
                .region(region)
                .provider(provider)
                .providerId(providerId)
                .build();
    }

    public void updateUserProfile(String nickname, String region){
        this.nickname = nickname;
        this.region = region;
    }
}
