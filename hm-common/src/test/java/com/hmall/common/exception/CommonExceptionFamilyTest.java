package com.hmall.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommonExceptionFamilyTest {

    @Test
    @DisplayName("CommonException 自身：保留 code、message、cause")
    void commonException_holdsCodeMessageCause() {
        Throwable cause = new IllegalStateException("root");
        CommonException e = new CommonException("boom", cause, 599);
        assertThat(e.getCode()).isEqualTo(599);
        assertThat(e.getMessage()).isEqualTo("boom");
        assertThat(e.getCause()).isSameAs(cause);
    }

    @Test
    @DisplayName("CommonException(cause, code) 把 cause 的描述当作 message")
    void commonException_causeOnlyConstructor_inheritsCauseToString() {
        IllegalArgumentException cause = new IllegalArgumentException("inner");
        CommonException e = new CommonException(cause, 502);
        assertThat(e.getCode()).isEqualTo(502);
        assertThat(e.getCause()).isSameAs(cause);
        assertThat(e.getMessage()).contains("inner");
    }

    @Test
    @DisplayName("BadRequestException code = 400 且继承 CommonException")
    void badRequestException_is400AndCommon() {
        BadRequestException e = new BadRequestException("bad");
        assertThat(e.getCode()).isEqualTo(400);
        assertThat(e).isInstanceOf(CommonException.class);
        assertThat(e.getMessage()).isEqualTo("bad");
    }

    @Test
    @DisplayName("UnauthorizedException code = 401")
    void unauthorizedException_is401() {
        assertThat(new UnauthorizedException("nope").getCode()).isEqualTo(401);
        assertThat(new UnauthorizedException(new RuntimeException()).getCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("ForbiddenException code = 403")
    void forbiddenException_is403() {
        assertThat(new ForbiddenException("no").getCode()).isEqualTo(403);
        assertThat(new ForbiddenException("no", new RuntimeException("x")).getCause())
                .hasMessage("x");
    }

    @Test
    @DisplayName("BizIllegalException code = 500")
    void bizIllegalException_is500() {
        assertThat(new BizIllegalException("biz").getCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("DbException code = 500")
    void dbException_is500() {
        assertThat(new DbException("db").getCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("所有子类都接受 (message, cause) 三参构造")
    void allSubclasses_supportMessageAndCauseConstructor() {
        Throwable cause = new RuntimeException("root");
        assertThat(new BadRequestException("x", cause).getCause()).isSameAs(cause);
        assertThat(new UnauthorizedException("x", cause).getCause()).isSameAs(cause);
        assertThat(new ForbiddenException("x", cause).getCause()).isSameAs(cause);
        assertThat(new BizIllegalException("x", cause).getCause()).isSameAs(cause);
        assertThat(new DbException("x", cause).getCause()).isSameAs(cause);
    }
}
