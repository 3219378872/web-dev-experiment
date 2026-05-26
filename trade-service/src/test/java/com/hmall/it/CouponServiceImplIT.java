package com.hmall.it;

import com.hmall.common.exception.BadRequestException;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.po.Coupon;
import com.hmall.service.ICouponService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {"spring.cloud.bootstrap.enabled=false"})
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/sql/data-order.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class CouponServiceImplIT {

    @BeforeEach
    void setUp() { UserContext.setUser(1L); }

    @AfterEach
    void tearDown() { UserContext.removeUser(); }

    @Autowired
    private ICouponService couponService;

    @Test
    void getAvailableCoupons_shouldReturnValidOnly() {
        List<Coupon> result = couponService.getAvailableCoupons();
        assertThat(result).allMatch(c -> c.getStatus() == 1);
        assertThat(result).allMatch(c -> c.getRemainingStock() > 0);
    }

    @Test
    void claimCoupon_shouldReduceStock() {
        couponService.claimCoupon(1L, 500L);
        Coupon coupon = couponService.getById(500L);
        assertThat(coupon.getRemainingStock()).isEqualTo(99);
    }

    @Test
    void claimCoupon_duplicate_shouldThrow() {
        couponService.claimCoupon(1L, 500L);
        assertThatThrownBy(() -> couponService.claimCoupon(1L, 500L))
                .isInstanceOf(BizIllegalException.class)
                .hasMessageContaining("已领取");
    }

    @Test
    void claimCoupon_outOfStock_shouldThrow() {
        assertThatThrownBy(() -> couponService.claimCoupon(1L, 501L))
                .isInstanceOf(BizIllegalException.class)
                .hasMessageContaining("已被抢光");
    }

    @Test
    void claimCoupon_notFound_shouldThrow() {
        assertThatThrownBy(() -> couponService.claimCoupon(1L, 999L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    void getUserCoupons_shouldReturnClaimedCoupons() {
        couponService.claimCoupon(1L, 500L);
        List<Coupon> result = couponService.getUserCoupons(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(500L);
    }
}
