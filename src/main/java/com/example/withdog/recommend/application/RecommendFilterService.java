package com.example.withdog.recommend.application;

import com.example.withdog.recommend.domain.FeatureVector;
import com.example.withdog.recommend.domain.RecommendResult;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecommendFilterService {

    private static final double MAX_DISTANCE_SMALL_DOG = 2.0;
    private static final double SMALL_DOG_WEIGHT_LIMIT = 5.0;
    private static final int OLD_DOG_AGE = 8;

    public List<RecommendResult> filter(FeatureVector fv, List<RecommendResult> results){
        List<RecommendResult> base = results.stream()
                .filter(r -> isSuitable(fv, r))
                .collect(Collectors.toMap(RecommendResult::courseName, r -> r, (a,b)->a, LinkedHashMap::new))
                .values().stream().toList();

        if (fv.weather() != null && fv.weather().isRaining()) {
            return base.stream()
                    .map(r -> new RecommendResult(r.courseName(), r.distanceKm(), r.durationMinutes(),
                            r.reason() + " (우천 시 우비 착용을 권장합니다)", r.route()))
                    .toList();
        }
        return base;
    }

    private boolean isSuitable(FeatureVector fv, RecommendResult rr){
        boolean isSmallOrOld = (fv.weight() != null && fv.weight() <= SMALL_DOG_WEIGHT_LIMIT)
        || fv.age() >= OLD_DOG_AGE;

        if(isSmallOrOld && rr.distanceKm() > MAX_DISTANCE_SMALL_DOG){
            return false;
        }
        return true;
    }
}
