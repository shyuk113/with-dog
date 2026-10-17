package com.example.withdog.user.domain;

import com.example.withdog.global.BaseEntity;
import com.example.withdog.region.domain.Region;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
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

    //시군구 행정구역 코드 (좌표로부터 정규화, 좌표가 없거나 변환 실패 시 null)
    @Column(name = "region_code", length = 5)
    private String regionCode;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    private String providerId;

    @Column
    private Double latitude;
    @Column
    private Double longitude;

    private String address;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private int level = 1;

    @Column(nullable = false)
    private int experience = 0;

    @Builder
    private User(String nickname, String name, String email, String password, String region, Provider provider, String providerId, Role role) {
        this.nickname = nickname;
        this.name = name;
        this.email = email;
        this.password = password;
        this.region = region;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
    }

    public static User createUserLocal(String name, String email, String password, String region){
        return User.builder()
                .nickname(name)
                .name(name)
                .email(email)
                .password(password)
                .region(region)
                .provider(Provider.LOCAL)
                .role(Role.USER)
                .build();
    }

    public static User createUserSocial(String nickname, String email, String region, Provider provider, String providerId){
        return User.builder()
                .nickname(nickname)
                .email(email)
                .region(region)
                .provider(provider)
                .providerId(providerId)
                .role(Role.USER)
                .build();
    }

    public void updateUserProfile(String nickname, String region, Double latitude, Double longitude, String address){
        this.nickname = nickname;
        this.region = region;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    //정규화된 시군구로 지역 정보를 덮어쓴다 (표시용 region도 표준 명칭으로 통일)
    public void assignRegion(Region region){
        this.regionCode = region.code();
        this.region = region.name();
    }

    //좌표가 바뀌었는데 지역을 확인할 수 없으면 이전 좌표 기준 코드가 남지 않도록 비운다
    public void clearRegionCode(){
        this.regionCode = null;
    }

    public void updateProfileImage(String imageUrl){
        this.imageUrl = imageUrl;
    }

    //경험치 획득 및 레벨업 처리 (레벨 N에서 다음 레벨까지 필요한 경험치는 N*100)
    public void gainExp(int amount){
        this.experience += amount;
        while (this.experience >= level * 100){
            this.experience -= level * 100;
            this.level++;
        }
    }
}
