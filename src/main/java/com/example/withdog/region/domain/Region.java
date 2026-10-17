package com.example.withdog.region.domain;

//시군구 단위 지역. code는 행정구역 코드 앞 5자리(예: 서울 강남구 11680), name은 표시용(예: 서울특별시 강남구)
public record Region(String code, String name) {

    public static final int SIGUNGU_CODE_LENGTH = 5;

    public Region {
        if (code == null || code.length() != SIGUNGU_CODE_LENGTH) {
            throw new IllegalArgumentException("시군구 코드는 5자리여야 합니다: " + code);
        }
    }

    //법정동/행정동 10자리 코드에서 앞 5자리(시군구)만 잘라 생성
    public static Region ofSigungu(String fullCode, String name) {
        if (fullCode == null || fullCode.length() < SIGUNGU_CODE_LENGTH) {
            throw new IllegalArgumentException("행정구역 코드가 올바르지 않습니다: " + fullCode);
        }
        return new Region(fullCode.substring(0, SIGUNGU_CODE_LENGTH), name);
    }
}
