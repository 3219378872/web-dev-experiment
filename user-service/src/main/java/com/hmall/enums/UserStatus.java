package com.hmall.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.hmall.common.exception.BadRequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {
    NORMAL(1, "正常"),
    FROZEN(2, "冻结");

    @EnumValue
    private final int value;
    private final String desc;

    public static UserStatus of(int value) {
        if (value == 1) {
            return NORMAL;
        }
        if (value == 2) {
            return FROZEN;
        }
        throw new BadRequestException("账户状态错误");
    }
}