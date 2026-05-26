package com.hmall.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PayTypeTest {

    @Test
    @DisplayName("枚举值与 DB 字典对齐：JSAPI=1, MINI_APP=2, APP=3, NATIVE=4, BALANCE=5")
    void enumValues_alignWithDictionary() {
        assertThat(PayType.JSAPI.getValue()).isEqualTo(1);
        assertThat(PayType.MINI_APP.getValue()).isEqualTo(2);
        assertThat(PayType.APP.getValue()).isEqualTo(3);
        assertThat(PayType.NATIVE.getValue()).isEqualTo(4);
        assertThat(PayType.BALANCE.getValue()).isEqualTo(5);
    }

    @Test
    @DisplayName("equalsValue 严格比较，null 安全")
    void equalsValue_strictMatchAndNullSafe() {
        assertThat(PayType.BALANCE.equalsValue(5)).isTrue();
        assertThat(PayType.BALANCE.equalsValue(1)).isFalse();
        assertThat(PayType.BALANCE.equalsValue(null)).isFalse();
    }

    @Test
    @DisplayName("desc 描述跟随 value，不重复")
    void desc_isUnique() {
        assertThat(PayType.JSAPI.getDesc()).isEqualTo("网页支付JS");
        assertThat(PayType.BALANCE.getDesc()).isEqualTo("余额支付");
        assertThat(PayType.values()).extracting(PayType::getDesc)
                .doesNotHaveDuplicates();
    }
}
