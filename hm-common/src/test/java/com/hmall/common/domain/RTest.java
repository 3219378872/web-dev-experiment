package com.hmall.common.domain;

import com.hmall.common.exception.BadRequestException;
import com.hmall.common.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RTest {

    @Test
    @DisplayName("ok() 返回 code=200, msg=OK, data=null")
    void ok_withoutData_returnsSuccessEnvelope() {
        R<Void> r = R.ok();
        assertThat(r.getCode()).isEqualTo(200);
        assertThat(r.getMsg()).isEqualTo("OK");
        assertThat(r.getData()).isNull();
        assertThat(r.success()).isTrue();
    }

    @Test
    @DisplayName("ok(data) 把 data 透传到响应里")
    void ok_withData_carriesPayload() {
        R<String> r = R.ok("hello");
        assertThat(r.getCode()).isEqualTo(200);
        assertThat(r.getData()).isEqualTo("hello");
        assertThat(r.success()).isTrue();
    }

    @Test
    @DisplayName("error(msg) 默认 code=500，success() 为 false")
    void error_withMsg_defaultsTo500AndFails() {
        R<Object> r = R.error("boom");
        assertThat(r.getCode()).isEqualTo(500);
        assertThat(r.getMsg()).isEqualTo("boom");
        assertThat(r.getData()).isNull();
        assertThat(r.success()).isFalse();
    }

    @Test
    @DisplayName("error(code, msg) 用调用方指定的 code")
    void error_withCodeAndMsg_usesProvidedCode() {
        R<Object> r = R.error(418, "I'm a teapot");
        assertThat(r.getCode()).isEqualTo(418);
        assertThat(r.getMsg()).isEqualTo("I'm a teapot");
        assertThat(r.success()).isFalse();
    }

    @Test
    @DisplayName("error(CommonException) 继承异常的 code 与 message")
    void error_fromException_inheritsCodeAndMessage() {
        R<Object> r400 = R.error(new BadRequestException("bad input"));
        assertThat(r400.getCode()).isEqualTo(400);
        assertThat(r400.getMsg()).isEqualTo("bad input");

        R<Object> r401 = R.error(new UnauthorizedException("nope"));
        assertThat(r401.getCode()).isEqualTo(401);
        assertThat(r401.getMsg()).isEqualTo("nope");
    }

    @Test
    @DisplayName("无参构造器允许 Jackson 反序列化，字段默认 0/null/false")
    void noArgsConstructor_returnsDefaultEnvelope() {
        R<Object> r = new R<>();
        assertThat(r.getCode()).isZero();
        assertThat(r.getMsg()).isNull();
        assertThat(r.getData()).isNull();
        assertThat(r.success()).isFalse();
    }
}
