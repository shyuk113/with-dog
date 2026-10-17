package com.example.withdog.post.domain.event;

//게시글 삭제 커밋 이후 발행하는 이벤트 (삭제 후에는 엔티티가 사라지므로 필요한 값을 미리 담는다)
public record PostDeletedEvent(Long postId, String regionCode) {
}
