package com.hmall.service.impl;

import com.hmall.api.client.CartClient;
import com.hmall.api.client.ItemClient;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.po.Coupon;
import com.hmall.domain.po.UserCoupon;
import com.hmall.mapper.CouponMapper;
import com.hmall.mapper.UserCouponMapper;
import com.hmall.service.ICouponService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
    "spring.cloud.bootstrap.enabled=false",
    "spring.cloud.nacos.discovery.enabled=false",
    "spring.cloud.nacos.config.enabled=false"
})
@Transactional
class CouponServiceImplTest {

    @MockBean
    private ItemClient itemClient;

    @MockBean
    private CartClient cartClient;

    @Autowired
    private ICouponService couponService;

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        UserContext.setUser(TEST_USER_ID);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private Coupon coupon(String name, int discount, int stock, int status,
                          LocalDateTime start, LocalDateTime end) {
        Coupon c = new Coupon();
        c.setName(name);
        c.setDiscountValue(discount);
        c.setRemainingStock(stock);
        c.setStatus(status);
        c.setStartTime(start);
        c.setEndTime(end);
        c.setDiscountType(1);
        return c;
    }

    @Test
    void getAvailableCoupons_returnsValidCoupons() {
        LocalDateTime now = LocalDateTime.now();
        Coupon valid = coupon("满减券", 1000, 10, 1, now.minusDays(1), now.plusDays(1));
        Coupon expired = coupon("过期券", 1000, 10, 1, now.minusDays(10), now.minusDays(1));
        Coupon soldOut = coupon("已抢光", 1000, 0, 1, now.minusDays(1), now.plusDays(1));
        Coupon disabled = coupon("已禁用", 1000, 10, 2, now.minusDays(1), now.plusDays(1));
        couponMapper.insert(valid);
        couponMapper.insert(expired);
        couponMapper.insert(soldOut);
        couponMapper.insert(disabled);

        List<Coupon> result = couponService.getAvailableCoupons();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("满减券");
    }

    @Test
    void claimCoupon_success_deductsStockAndCreatesRecord() {
        LocalDateTime now = LocalDateTime.now();
        Coupon c = coupon("新人券", 1000, 10, 1, now.minusDays(1), now.plusDays(1));
        couponMapper.insert(c);

        couponService.claimCoupon(TEST_USER_ID, c.getId());

        Coupon updated = couponMapper.selectById(c.getId());
        assertThat(updated.getRemainingStock()).isEqualTo(9);

        List<UserCoupon> ucs = userCouponMapper.selectList(null);
        assertThat(ucs).hasSize(1);
        assertThat(ucs.get(0).getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(ucs.get(0).getCouponId()).isEqualTo(c.getId());
    }

    @Test
    void claimCoupon_notFound_throwsBadRequest() {
        assertThatThrownBy(() -> couponService.claimCoupon(TEST_USER_ID, 999L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    void claimCoupon_disabled_throwsBadRequest() {
        LocalDateTime now = LocalDateTime.now();
        Coupon c = coupon("已禁用券", 1000, 10, 2, now.minusDays(1), now.plusDays(1));
        couponMapper.insert(c);

        assertThatThrownBy(() -> couponService.claimCoupon(TEST_USER_ID, c.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    void claimCoupon_soldOut_throwsBizIllegal() {
        LocalDateTime now = LocalDateTime.now();
        Coupon c = coupon("已抢光", 1000, 0, 1, now.minusDays(1), now.plusDays(1));
        couponMapper.insert(c);

        assertThatThrownBy(() -> couponService.claimCoupon(TEST_USER_ID, c.getId()))
                .isInstanceOf(BizIllegalException.class)
                .hasMessageContaining("抢光");
    }

    @Test
    void claimCoupon_duplicateClaim_throwsBizIllegal() {
        LocalDateTime now = LocalDateTime.now();
        Coupon c = coupon("重复领取", 1000, 10, 1, now.minusDays(1), now.plusDays(1));
        couponMapper.insert(c);

        couponService.claimCoupon(TEST_USER_ID, c.getId());

        assertThatThrownBy(() -> couponService.claimCoupon(TEST_USER_ID, c.getId()))
                .isInstanceOf(BizIllegalException.class)
                .hasMessageContaining("已领取");
    }

    @Test
    void getUserCoupons_empty_returnsEmptyList() {
        List<Coupon> result = couponService.getUserCoupons(TEST_USER_ID);
        assertThat(result).isEmpty();
    }

    @Test
    void getUserCoupons_returnsClaimedCoupons() {
        LocalDateTime now = LocalDateTime.now();
        Coupon c1 = coupon("优惠券A", 1000, 10, 1, now.minusDays(1), now.plusDays(1));
        Coupon c2 = coupon("优惠券B", 500, 10, 1, now.minusDays(1), now.plusDays(1));
        couponMapper.insert(c1);
        couponMapper.insert(c2);

        couponService.claimCoupon(TEST_USER_ID, c1.getId());

        List<Coupon> result = couponService.getUserCoupons(TEST_USER_ID);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(c1.getId());
    }

    // ===== getAvailableCouponsForAmount tests =====

    @Test
    void getAvailableCouponsForAmount_filtersByMinAmount() {
        LocalDateTime now = LocalDateTime.now();
        Coupon c1 = coupon("满100减10", 1000, 10, 1, now.minusDays(1), now.plusDays(1));
        c1.setMinAmount(10000);
        couponMapper.insert(c1);
        Coupon c2 = coupon("满50减5", 500, 10, 1, now.minusDays(1), now.plusDays(1));
        c2.setMinAmount(5000);
        couponMapper.insert(c2);

        // 金额8000分，只能用满50的
        List<Coupon> result = couponService.getAvailableCouponsForAmount(TEST_USER_ID, 8000);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("满50减5");
    }

    @Test
    void getAvailableCouponsForAmount_excludesClaimedCoupons() {
        LocalDateTime now = LocalDateTime.now();
        Coupon c1 = coupon("未领取券", 1000, 10, 1, now.minusDays(1), now.plusDays(1));
        c1.setMinAmount(0);
        couponMapper.insert(c1);
        Coupon c2 = coupon("已领取券", 500, 10, 1, now.minusDays(1), now.plusDays(1));
        c2.setMinAmount(0);
        couponMapper.insert(c2);

        couponService.claimCoupon(TEST_USER_ID, c2.getId());

        List<Coupon> result = couponService.getAvailableCouponsForAmount(TEST_USER_ID, 10000);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("未领取券");
    }

    @Test
    void getAvailableCouponsForAmount_nullAmount_returnsAllValid() {
        LocalDateTime now = LocalDateTime.now();
        Coupon c1 = coupon("满减券", 1000, 10, 1, now.minusDays(1), now.plusDays(1));
        c1.setMinAmount(99999);
        couponMapper.insert(c1);

        List<Coupon> result = couponService.getAvailableCouponsForAmount(TEST_USER_ID, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAvailableCouponsForAmount_excludesExpiredAndDisabled() {
        LocalDateTime now = LocalDateTime.now();
        Coupon expired = coupon("过期券", 1000, 10, 1, now.minusDays(10), now.minusDays(1));
        expired.setMinAmount(0);
        couponMapper.insert(expired);
        Coupon disabled = coupon("禁用券", 1000, 10, 2, now.minusDays(1), now.plusDays(1));
        disabled.setMinAmount(0);
        couponMapper.insert(disabled);

        List<Coupon> result = couponService.getAvailableCouponsForAmount(TEST_USER_ID, 10000);

        assertThat(result).isEmpty();
    }

    @Test
    void getAvailableCouponsForAmount_noCoupons_returnsEmpty() {
        List<Coupon> result = couponService.getAvailableCouponsForAmount(TEST_USER_ID, 10000);

        assertThat(result).isEmpty();
    }
}
