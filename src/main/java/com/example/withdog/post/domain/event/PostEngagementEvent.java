package com.example.withdog.post.domain.event;

//게시글 반응(좋아요/댓글) 이벤트. count는 반영할 개수 (대댓글이 달린 댓글 삭제 시 함께 지워지는 대댓글 수까지 포함)
public record PostEngagementEvent(Long postId, String regionCode, Long userId, EngagementType type, int count) {

    public static PostEngagementEvent of(Long postId, String regionCode, Long userId, EngagementType type) {
        return new PostEngagementEvent(postId, regionCode, userId, type, 1);
    }
}
