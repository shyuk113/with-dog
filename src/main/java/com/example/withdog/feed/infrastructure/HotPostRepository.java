package com.example.withdog.feed.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.zset.Aggregate;
import org.springframework.data.redis.connection.zset.Weights;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

//동네 인기글 점수: 일자별 Sorted Set (key=hot:region:{시군구코드}:{yyyyMMdd}, member=postId, score=반응 점수 합)
//조회 시 최근 WINDOW_DAYS일치를 날짜 가중치(오늘 1, 어제 0.5, 그제 0.25)로 합산해 오래된 반응일수록 덜 반영한다
@Repository
@RequiredArgsConstructor
public class HotPostRepository {

    public static final int WINDOW_DAYS = 3;
    private static final double[] DAY_WEIGHTS = {1.0, 0.5, 0.25};

    private static final String KEY_PREFIX = "hot:region:";
    private static final String PROCESSED_PREFIX = "hot:processed:";
    private static final Duration DAILY_KEY_TTL = Duration.ofDays(WINDOW_DAYS + 1);
    private static final Duration PROCESSED_TTL = Duration.ofDays(1);
    private static final Duration WINDOW_KEY_TTL = Duration.ofSeconds(10);
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    //"처리한 이벤트 기록 + 점수 증가"를 하나의 원자적 연산으로 묶는다
    //따로 실행하면 둘 사이에 실패했을 때 중복 반영(증가 후 기록 실패) 또는 유실(기록 후 증가 실패)이 생긴다
    private static final RedisScript<Long> INCREMENT_ONCE = new DefaultRedisScript<>("""
            if redis.call('SET', KEYS[1], '1', 'NX', 'EX', ARGV[1]) then
              redis.call('ZINCRBY', KEYS[2], ARGV[2], ARGV[3])
              redis.call('EXPIRE', KEYS[2], ARGV[4])
              return 1
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    //같은 eventId는 한 번만 반영 (Kafka at-least-once 중복 수신 방어). 반영했으면 true
    public boolean incrementOnce(String eventId, String regionCode, Long postId, double delta, LocalDate day) {
        Long applied = stringRedisTemplate.execute(INCREMENT_ONCE,
                List.of(PROCESSED_PREFIX + eventId, dailyKey(regionCode, day)),
                String.valueOf(PROCESSED_TTL.toSeconds()), String.valueOf(delta), postId.toString(),
                String.valueOf(DAILY_KEY_TTL.toSeconds()));
        return applied != null && applied == 1L;
    }

    //최근 WINDOW_DAYS일 점수를 날짜 가중치로 합산해 점수가 양수인 글만 높은 순으로 반환
    public List<ScoredPost> findTop(String regionCode, LocalDate today, int limit) {
        List<String> dayKeys = windowKeys(regionCode, today);
        //요청마다 고유한 임시 키에 합산 결과를 만들고 바로 지운다 (동시 요청끼리 결과가 섞이지 않도록)
        String windowKey = KEY_PREFIX + regionCode + ":window:" + UUID.randomUUID();
        try {
            stringRedisTemplate.opsForZSet().unionAndStore(dayKeys.getFirst(), dayKeys.subList(1, dayKeys.size()),
                    windowKey, Aggregate.SUM, Weights.of(DAY_WEIGHTS));
            stringRedisTemplate.expire(windowKey, WINDOW_KEY_TTL);
            Set<TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet()
                    .reverseRangeByScoreWithScores(windowKey, Double.MIN_VALUE, Double.POSITIVE_INFINITY, 0, limit);
            if (tuples == null) {
                return List.of();
            }
            return tuples.stream()
                    .map(t -> new ScoredPost(Long.valueOf(t.getValue()), t.getScore()))
                    .toList();
        } finally {
            stringRedisTemplate.delete(windowKey);
        }
    }

    public void remove(String regionCode, Collection<Long> postIds, LocalDate today) {
        if (postIds.isEmpty()) {
            return;
        }
        Object[] members = postIds.stream().map(String::valueOf).toArray();
        windowKeys(regionCode, today).forEach(key -> stringRedisTemplate.opsForZSet().remove(key, members));
    }

    public void deleteRecent(String regionCode, LocalDate today) {
        stringRedisTemplate.delete(windowKeys(regionCode, today));
    }

    private List<String> windowKeys(String regionCode, LocalDate today) {
        return IntStream.range(0, WINDOW_DAYS)
                .mapToObj(daysAgo -> dailyKey(regionCode, today.minusDays(daysAgo)))
                .toList();
    }

    private String dailyKey(String regionCode, LocalDate day) {
        return KEY_PREFIX + regionCode + ":" + day.format(DAY_FORMAT);
    }

    public record ScoredPost(Long postId, double score) {
    }
}
