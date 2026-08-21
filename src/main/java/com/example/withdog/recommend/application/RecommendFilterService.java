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

        String notice = weatherNotice(fv);
        if (notice.isEmpty()) {
            return base;
        }

        return base.stream()
                .map(r-> new RecommendResult(r.courseName(), r.distanceKm(), r.durationMinutes(), r.reason()
                +notice, r.route())).toList();
    }

    private boolean isSuitable(FeatureVector fv, RecommendResult rr){
        boolean isSmallOrOld = (fv.weight() != null && fv.weight() <= SMALL_DOG_WEIGHT_LIMIT)
        || fv.age() >= OLD_DOG_AGE;

        if(isSmallOrOld && rr.distanceKm() > MAX_DISTANCE_SMALL_DOG){
            return false;
        }
        return true;
    }

    private String weatherNotice(FeatureVector fv){
        if(fv.weather() == null){
            return "";
        }
        return switch(fv.weather().condition()){
            case RAIN -> " (우천 시 우비 착용을 권장합니다.)";
            case SHOWER -> " (소나기 예보가 있어 우비 착용을 권장합니다.)";
            case RAIN_SNOW -> " (비/눈 예보가 있어 미끄럼 주의 및 방수 용품을 권장합니다.)";
            case SNOW -> " (적설로 미끄러울 수 있어 미끄럼 방지용품을 권장합니다.)";
            case POLLEN -> " (꽃가루 농도가 높을 수 있어 알레르기 있는 반려견은 주의하세요)";
            case CLEAR, CLOUDY -> "";
        };
    }
}
