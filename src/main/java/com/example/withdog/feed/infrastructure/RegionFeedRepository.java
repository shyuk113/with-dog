package com.example.withdog.feed.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//동네 피드 타임라인: Redis Sorted Set (key=feed:region:{시군구코드}, member=postId, score=postId)
//score를 작성 시각 대신 postId로 두면 같은 ms에 작성된 글도 순서가 겹치지 않고, DB fallback의 커서(id < cursor)와 기준이 일치한다
@Repository
@RequiredArgsConstructor
public class RegionFeedRepository {

    public static final int MAX_FEED_SIZE = 1000;
    private static final String KEY_PREFIX = "feed:region:";

    private final StringRedisTemplate stringRedisTemplate;

    public void add(String regionCode, Long postId) {
        addAll(regionCode, List.of(postId));
    }

    public void addAll(String regionCode, Collection<Long> postIds) {
        if (postIds.isEmpty()) {
            return;
        }
        String key = key(regionCode);
        Set<TypedTuple<String>> tuples = postIds.stream()
                .map(id -> TypedTuple.of(id.toString(), id.doubleValue()))
                .collect(Collectors.toSet());
        stringRedisTemplate.opsForZSet().add(key, tuples);
        //점수 오름차순 기준 앞쪽(오래된 글)을 잘라 최신 MAX_FEED_SIZE개만 유지, 그 이전 글은 DB에서 이어서 읽는다
        stringRedisTemplate.opsForZSet().removeRange(key, 0, -(MAX_FEED_SIZE + 1));
    }

    public void remove(String regionCode, Collection<Long> postIds) {
        if (postIds.isEmpty()) {
            return;
        }
        stringRedisTemplate.opsForZSet().remove(key(regionCode), postIds.stream().map(String::valueOf).toArray());
    }

    //cursor(postId)보다 오래된 글을 최신순으로 최대 limit개 (cursor가 null이면 가장 최신부터)
    public List<Long> findPostIds(String regionCode, Long cursor, int limit) {
        double max = cursor == null ? Double.POSITIVE_INFINITY : cursor - 1;
        Set<String> ids = stringRedisTemplate.opsForZSet()
                .reverseRangeByScore(key(regionCode), Double.NEGATIVE_INFINITY, max, 0, limit);
        return ids == null ? List.of() : ids.stream().map(Long::valueOf).toList();
    }

    public void deleteFeed(String regionCode) {
        stringRedisTemplate.delete(key(regionCode));
    }

    private String key(String regionCode) {
        return KEY_PREFIX + regionCode;
    }
}
