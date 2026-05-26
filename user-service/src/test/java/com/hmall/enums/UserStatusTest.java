package com.hmall.enums;

import com.hmall.common.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserStatusTest {

    @Test
    @DisplayName("of(0) -> FROZEN，of(1) -> NORMAL")
    void of_validValues_returnsCorrespondingEnum() {
        assertThat(UserStatus.of(0)).isEqualTo(UserStatus.FROZEN);
        assertThat(UserStatus.of(1)).isEqualTo(UserStatus.NORMAL);
    }

    @Test
    @DisplayName("of(其它整数) -> BadRequestException 账户状态错误（防御 DB 脏数据）")
    void of_unknownValue_throwsBadRequest() {
        assertThatThrownBy(() -> UserStatus.of(99))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("账户状态错误");
    }

    @Test
    @DisplayName("value/desc 字段对齐当前 DB 字典 (FROZEN=0, NORMAL=1)")
    void valueAndDesc_alignWithDatabase() {
        assertThat(UserStatus.FROZEN.getValue()).isZero();
        assertThat(UserStatus.NORMAL.getValue()).isEqualTo(1);
        assertThat(UserStatus.FROZEN.getDesc()).isEqualTo("禁止使用");
        assertThat(UserStatus.NORMAL.getDesc()).isEqualTo("已激活");
    }
}
