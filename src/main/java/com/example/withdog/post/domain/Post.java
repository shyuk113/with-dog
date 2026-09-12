package com.example.withdog.post.domain;

import com.example.withdog.global.BaseEntity;
import com.example.withdog.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    private String imageUrl;

    // 산책 코스 공유 — 작성 시점의 코스 스냅샷 (추천받은 경로 또는 실제 산책 기록 중 하나를 선택해 첨부)
    private String courseName;

    private Double distanceKm;

    private Integer durationMinutes;

    @ElementCollection
    @CollectionTable(name = "post_route_point", joinColumns = @JoinColumn(name = "post_id"))
    @OrderColumn(name = "sequence")
    private List<PostRoutePoint> route = new ArrayList<>();

    @Builder
    private Post(String title, String content, User author, String courseName, Double distanceKm, Integer durationMinutes, List<PostRoutePoint> route) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.courseName = courseName;
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        if (route != null) {
            this.route = route;
        }
    }

    public static Post createPost(String title, String content, User author) {
        return Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();
    }

    public static Post createPostWithRoute(String title, String content, User author,
                                            String courseName, double distanceKm, int durationMinutes, List<PostRoutePoint> route) {
        return Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .courseName(courseName)
                .distanceKm(distanceKm)
                .durationMinutes(durationMinutes)
                .route(route)
                .build();
    }

    public boolean hasRoute() {
        return courseName != null;
    }

    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void attachImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
