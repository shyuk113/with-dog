package com.example.withdog.feed.infrastructure.messaging;

public final class FeedTopics {

    //게시글 작성/삭제 (key = regionCode: 같은 게시글의 작성/삭제는 항상 같은 파티션에서 순서대로 처리된다)
    public static final String POST = "feed.post";

    //좋아요/댓글 반응 (key = postId)
    public static final String POST_ENGAGEMENT = "feed.post-engagement";

    //재시도 후에도 처리하지 못한 메시지를 보관하는 Dead Letter Topic 접미사
    public static final String DLT_SUFFIX = ".DLT";

    public static final int PARTITIONS = 3;

    private FeedTopics() {
    }
}
