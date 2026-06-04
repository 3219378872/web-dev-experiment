package com.hmall.enums;

import com.hmall.common.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserStatusTest {

    @Test
    @DisplayName("of(1) -> NORMAL，of(2) -> FROZEN")
    void of_validValues_returnsCorrespondingEnum() {
        assertThat(UserStatus.of(1)).isEqualTo(UserStatus.NORMAL);
        assertThat(UserStatus.of(2)).isEqualTo(UserStatus.FROZEN);
    }

    @Test
    @DisplayName("of(其它整数) -> BadRequestException 账户状态错误（防御 DB 脏数据）")
    void of_unknownValue_throwsBadRequest() {
        assertThatThrownBy(() -> UserStatus.of(99))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("账户状态错误");
    }

    @Test
    @DisplayName("value/desc 字段对齐当前 DB 字典 (NORMAL=1, FROZEN=2)")
    void valueAndDesc_alignWithDatabase() {
        assertThat(UserStatus.NORMAL.getValue()).isEqualTo(1);
        assertThat(UserStatus.FROZEN.getValue()).isEqualTo(2);
        assertThat(UserStatus.NORMAL.getDesc()).isEqualTo("正常");
        assertThat(UserStatus.FROZEN.getDesc()).isEqualTo("冻结");
    }
}
