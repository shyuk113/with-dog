package com.example.withdog.feed.domain;

import com.example.withdog.post.domain.event.EngagementType;

//반응 종류별 인기 점수 가중치 (댓글은 좋아요보다 참여 비용이 커서 2배로 반영)
public final class EngagementScorePolicy {

    public static final int LIKE_WEIGHT = 1;
    public static final int COMMENT_WEIGHT = 2;

    private EngagementScorePolicy() {
    }

    public static double delta(EngagementType type, int count) {
        return switch (type) {
            case LIKED -> LIKE_WEIGHT * count;
            case UNLIKED -> -LIKE_WEIGHT * count;
            case COMMENTED -> COMMENT_WEIGHT * count;
            case COMMENT_DELETED -> -COMMENT_WEIGHT * count;
        };
    }
}
