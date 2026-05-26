package com.hmall.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PayStatusTest {

    @Test
    @DisplayName("value/desc 对齐 DB 字典")
    void valueAndDesc_alignWithDatabase() {
        assertThat(PayStatus.NOT_COMMIT.getValue()).isZero();
        assertThat(PayStatus.WAIT_BUYER_PAY.getValue()).isEqualTo(1);
        assertThat(PayStatus.TRADE_CLOSED.getValue()).isEqualTo(2);
        assertThat(PayStatus.TRADE_SUCCESS.getValue()).isEqualTo(3);
        // TRADE_FINISHED 故意复用 value=3（业务上"完成"= "成功"的最终态）
        assertThat(PayStatus.TRADE_FINISHED.getValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("equalsValue 严格比较 int 值")
    void equalsValue_matchesIntValue() {
        assertThat(PayStatus.NOT_COMMIT.equalsValue(0)).isTrue();
        assertThat(PayStatus.WAIT_BUYER_PAY.equalsValue(1)).isTrue();
        assertThat(PayStatus.TRADE_SUCCESS.equalsValue(3)).isTrue();
        assertThat(PayStatus.TRADE_CLOSED.equalsValue(3)).isFalse();
        assertThat(PayStatus.NOT_COMMIT.equalsValue(1)).isFalse();
    }

    @Test
    @DisplayName("equalsValue(null) 返回 false —— 防御 DB NULL 字段")
    void equalsValue_nullInput_returnsFalse() {
        assertThat(PayStatus.WAIT_BUYER_PAY.equalsValue(null)).isFalse();
        assertThat(PayStatus.TRADE_SUCCESS.equalsValue(null)).isFalse();
    }

    @Test
    @DisplayName("TRADE_SUCCESS 与 TRADE_FINISHED 共享 value=3（设计约定 / 已知特性）")
    void successAndFinished_shareValue() {
        // 反向（用 value 找 enum）时只能拿到声明在前的 TRADE_SUCCESS；
        // 若有反向查询需求需走 valueOf(name)。
        assertThat(PayStatus.TRADE_SUCCESS.equalsValue(3)).isTrue();
        assertThat(PayStatus.TRADE_FINISHED.equalsValue(3)).isTrue();
        assertThat(PayStatus.TRADE_SUCCESS.getValue())
                .isEqualTo(PayStatus.TRADE_FINISHED.getValue());
    }
}
