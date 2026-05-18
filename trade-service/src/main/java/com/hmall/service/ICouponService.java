package com.hmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.domain.po.Coupon;
import java.util.List;

public interface ICouponService extends IService<Coupon> {
    List<Coupon> getAvailableCoupons();
    void claimCoupon(Long userId, Long couponId);
    List<Coupon> getUserCoupons(Long userId);
}
