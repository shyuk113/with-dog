package com.example.withdog.feed.domain;

import com.example.withdog.post.domain.event.EngagementType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EngagementScorePolicyTest {

    @Test
    void 댓글은_좋아요보다_두배로_반영되고_취소_삭제는_같은_크기로_빠진다() {
        assertThat(EngagementScorePolicy.delta(EngagementType.LIKED, 1)).isEqualTo(1);
        assertThat(EngagementScorePolicy.delta(EngagementType.UNLIKED, 1)).isEqualTo(-1);
        assertThat(EngagementScorePolicy.delta(EngagementType.COMMENTED, 1)).isEqualTo(2);
        assertThat(EngagementScorePolicy.delta(EngagementType.COMMENT_DELETED, 1)).isEqualTo(-2);
    }

    @Test
    void 대댓글과_함께_삭제된_댓글은_개수만큼_빠진다() {
        assertThat(EngagementScorePolicy.delta(EngagementType.COMMENT_DELETED, 3)).isEqualTo(-6);
    }
}
