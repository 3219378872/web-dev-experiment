package com.hmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.domain.po.Coupon;
import java.util.List;

public interface ICouponService extends IService<Coupon> {
    List<Coupon> getAvailableCoupons();
    void claimCoupon(Long userId, Long couponId);
    List<Coupon> getUserCoupons(Long userId);

    /**
     * 获取用户可用的优惠券（过滤满减门槛、有效期、未使用）
     * @param userId 用户id
     * @param amount 订单金额，单位为分
     * @return 可用优惠券列表
     */
    List<Coupon> getAvailableCouponsForAmount(Long userId, Integer amount);

    /**
     * 使用优惠券：标记用户优惠券为已使用
     * @param userId 用户id
     * @param couponId 优惠券id
     * @param orderId 订单id
     */
    void useCoupon(Long userId, Long couponId, Long orderId);
}
