package com.example.withdog.post.domain.event;

//게시글 작성 커밋 이후 피드 등 다른 도메인이 반응할 수 있도록 발행하는 이벤트
public record PostCreatedEvent(Long postId, String regionCode) {
}
