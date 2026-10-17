package com.example.withdog.region.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegionTest {

    @Test
    void 법정동_10자리_코드에서_시군구_5자리만_남긴다() {
        Region region = Region.ofSigungu("1168010100", "서울특별시 강남구");

        assertThat(region.code()).isEqualTo("11680");
        assertThat(region.name()).isEqualTo("서울특별시 강남구");
    }

    @Test
    void 시군구_코드가_5자리가_아니면_생성할_수_없다() {
        assertThatThrownBy(() -> new Region("1168", "서울특별시 강남구"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Region.ofSigungu(null, "서울특별시 강남구"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
