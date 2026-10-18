package com.example.withdog.feed.application;

import com.example.withdog.feed.application.dto.HotFeedResponse;
import com.example.withdog.feed.application.dto.HotPostResponse;
import com.example.withdog.feed.domain.EngagementScorePolicy;
import com.example.withdog.feed.infrastructure.HotPostRepository;
import com.example.withdog.feed.infrastructure.HotPostRepository.ScoredPost;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.post.application.PostSummaryAssembler;
import com.example.withdog.post.application.dto.PostSummaryResponse;
import com.example.withdog.post.domain.Post;
import com.example.withdog.post.domain.event.EngagementType;
import com.example.withdog.post.infrastructure.PostRepository;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotPostService {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    public static final int MAX_SIZE = 50;

    private final HotPostRepository hotPostRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostSummaryAssembler postSummaryAssembler;

    //반응 이벤트를 발생한 날짜의 점수에 반영. 같은 eventId는 한 번만 반영된다
    public void record(String eventId, EngagementType type, Long postId, String regionCode, int count, Instant occurredAt) {
        if (regionCode == null) {
            return; //동네가 없는 글은 동네 인기글 대상이 아니다
        }
        LocalDate day = LocalDate.ofInstant(occurredAt, ZONE);
        boolean applied = hotPostRepository.incrementOnce(
                eventId, regionCode, postId, EngagementScorePolicy.delta(type, count), day);
        if (!applied) {
            log.info("이미 반영된 반응 이벤트라 건너뜀 eventId={}", eventId);
        }
    }

    //내 동네 인기글
    @Transactional(readOnly = true)
    public HotFeedResponse getHotPosts(Long userId, int size) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String regionCode = user.getRegionCode();
        if (regionCode == null) {
            throw new BusinessException(ErrorCode.REGION_REQUIRED);
        }

        LocalDate today = LocalDate.now(ZONE);
        List<ScoredPost> scored = hotPostRepository.findTop(regionCode, today, Math.clamp(size, 1, MAX_SIZE));
        List<Long> ids = scored.stream().map(ScoredPost::postId).toList();

        Map<Long, Post> postsById = postRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Post::getId, Function.identity()));
        removeDeletedPosts(regionCode, ids.stream().filter(id -> !postsById.containsKey(id)).toList(), today);

        List<ScoredPost> alive = scored.stream().filter(s -> postsById.containsKey(s.postId())).toList();
        List<PostSummaryResponse> summaries = postSummaryAssembler.assemble(
                alive.stream().map(s -> postsById.get(s.postId())).filter(Objects::nonNull).toList(), userId);

        List<HotPostResponse> posts = IntStream.range(0, alive.size())
                .mapToObj(i -> new HotPostResponse(summaries.get(i), alive.get(i).score()))
                .toList();
        return new HotFeedResponse(regionCode, user.getRegion(), posts);
    }

    //삭제된 글의 점수는 조회 시점에 정리 (lazy cleanup)
    private void removeDeletedPosts(String regionCode, List<Long> deletedIds, LocalDate today) {
        if (deletedIds.isEmpty()) {
            return;
        }
        try {
            hotPostRepository.remove(regionCode, deletedIds, today);
        } catch (Exception e) {
            log.warn("동네 인기글 점수 정리 실패 region={}: {}", regionCode, e.getMessage());
        }
    }
}
