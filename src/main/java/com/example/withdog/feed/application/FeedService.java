package com.example.withdog.feed.application;

import com.example.withdog.feed.application.dto.RegionFeedResponse;
import com.example.withdog.feed.infrastructure.RegionFeedRepository;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.post.application.PostSummaryAssembler;
import com.example.withdog.post.domain.Post;
import com.example.withdog.post.infrastructure.PostRepository;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    public static final int MAX_PAGE_SIZE = 50;

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final RegionFeedRepository regionFeedRepository;
    private final PostSummaryAssembler postSummaryAssembler;

    //내 동네(시군구) 최신 글 피드: Redis 타임라인을 먼저 읽고, 부족한 만큼은 DB에서 같은 커서 기준으로 이어서 채운다
    @Transactional(readOnly = true)
    public RegionFeedResponse getRegionFeed(Long userId, Long cursor, int size) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String regionCode = user.getRegionCode();
        if (regionCode == null) {
            throw new BusinessException(ErrorCode.REGION_REQUIRED);
        }

        int pageSize = Math.clamp(size, 1, MAX_PAGE_SIZE);
        int fetchSize = pageSize + 1; //다음 페이지 존재 여부를 알기 위해 1개 더 조회

        List<Long> ids = new ArrayList<>(readFromCache(regionCode, cursor, fetchSize));
        if (ids.size() < fetchSize) {
            //캐시가 비었거나(콜드 스타트) 보관 개수를 넘어선 오래된 구간은 DB에서 이어서 조회
            Long dbCursor = ids.isEmpty() ? cursor : ids.getLast();
            List<Long> fromDb = postRepository.findIdsByRegionCode(
                    regionCode, dbCursor == null ? Long.MAX_VALUE : dbCursor, PageRequest.of(0, fetchSize - ids.size()));
            if (cursor == null && ids.isEmpty()) {
                warmUpCache(regionCode, fromDb);
            }
            ids.addAll(fromDb);
        }

        boolean hasNext = ids.size() > pageSize;
        List<Long> pageIds = hasNext ? ids.subList(0, pageSize) : ids;

        Map<Long, Post> postsById = postRepository.findAllById(pageIds).stream()
                .collect(Collectors.toMap(Post::getId, Function.identity()));
        removeStaleIds(regionCode, pageIds.stream().filter(id -> !postsById.containsKey(id)).toList());
        List<Post> posts = pageIds.stream().map(postsById::get).filter(Objects::nonNull).toList();

        Long nextCursor = hasNext ? pageIds.getLast() : null;
        return new RegionFeedResponse(regionCode, user.getRegion(),
                postSummaryAssembler.assemble(posts, userId), nextCursor, hasNext);
    }

    //Redis 장애 시에도 피드는 DB만으로 응답할 수 있도록 캐시 조회 실패는 빈 결과로 취급
    private List<Long> readFromCache(String regionCode, Long cursor, int limit) {
        try {
            return regionFeedRepository.findPostIds(regionCode, cursor, limit);
        } catch (Exception e) {
            log.warn("동네 피드 캐시 조회 실패 region={}: {}", regionCode, e.getMessage());
            return List.of();
        }
    }

    private void warmUpCache(String regionCode, List<Long> postIds) {
        try {
            regionFeedRepository.addAll(regionCode, postIds);
        } catch (Exception e) {
            log.warn("동네 피드 캐시 적재 실패 region={}: {}", regionCode, e.getMessage());
        }
    }

    //삭제 이벤트가 유실돼 캐시에 남은 글은 조회 시점에 정리 (lazy cleanup)
    private void removeStaleIds(String regionCode, List<Long> staleIds) {
        if (staleIds.isEmpty()) {
            return;
        }
        try {
            regionFeedRepository.remove(regionCode, staleIds);
        } catch (Exception e) {
            log.warn("동네 피드 캐시 정리 실패 region={}: {}", regionCode, e.getMessage());
        }
    }
}
