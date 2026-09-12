package com.example.withdog.mission.domain;

public enum MissionType {

    WALK_ONCE(Metric.WALK_COUNT, 1, "이번 주 산책 1회 완료하기"),
    WALK_THREE_TIMES(Metric.WALK_COUNT, 3, "이번 주 산책 3회 완료하기"),
    WALK_FIVE_TIMES(Metric.WALK_COUNT, 5, "이번 주 산책 5회 완료하기"),
    WALK_DISTANCE_5KM(Metric.DISTANCE_KM, 5, "이번 주 누적 산책 거리 5km 달성하기"),
    WALK_DURATION_60MIN(Metric.DURATION_MINUTES, 60, "이번 주 누적 산책 시간 60분 달성하기");

    public static final int POINTS_PER_MISSION = 20;
    public static final int WEEKLY_TOTAL_POINTS = POINTS_PER_MISSION * 5; // = 100

    private final Metric metric;
    private final double threshold;
    private final String description;

    MissionType(Metric metric, double threshold, String description) {
        this.metric = metric;
        this.threshold = threshold;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAchieved(double walkCount, double totalDistanceKm, double totalDurationMinutes) {
        double actual = switch (metric) {
            case WALK_COUNT -> walkCount;
            case DISTANCE_KM -> totalDistanceKm;
            case DURATION_MINUTES -> totalDurationMinutes;
        };
        return actual >= threshold;
    }

    private enum Metric {
        WALK_COUNT, DISTANCE_KM, DURATION_MINUTES
    }
}
