package com.hmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.domain.po.Coupon;
import com.hmall.domain.po.UserCoupon;
import com.hmall.mapper.CouponMapper;
import com.hmall.mapper.UserCouponMapper;
import com.hmall.service.ICouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon>
        implements ICouponService {

    private final UserCouponMapper userCouponMapper;

    @Override
    public List<Coupon> getAvailableCoupons() {
        return lambdaQuery()
                .eq(Coupon::getStatus, 1)
                .gt(Coupon::getRemainingStock, 0)
                .lt(Coupon::getStartTime, LocalDateTime.now())
                .gt(Coupon::getEndTime, LocalDateTime.now())
                .list();
    }

    @Override
    @Transactional
    public void claimCoupon(Long userId, Long couponId) {
        Coupon coupon = getById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            throw new BadRequestException("优惠券不存在或已失效");
        }
        if (coupon.getRemainingStock() <= 0) {
            throw new BizIllegalException("优惠券已被抢光");
        }
        long count = userCouponMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getCouponId, couponId));
        if (count > 0) {
            throw new BizIllegalException("已领取过该优惠券");
        }
        coupon.setRemainingStock(coupon.getRemainingStock() - 1);
        updateById(coupon);
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus(1);
        uc.setCreateTime(LocalDateTime.now());
        userCouponMapper.insert(uc);
    }

    @Override
    public List<Coupon> getUserCoupons(Long userId) {
        List<Long> couponIds = userCouponMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getStatus, 1))
                .stream().map(UserCoupon::getCouponId).collect(Collectors.toList());
        if (couponIds.isEmpty()) return List.of();
        return listByIds(couponIds);
    }

    @Override
    public List<Coupon> getAvailableCouponsForAmount(Long userId, Integer amount) {
        // 获取用户已领取且未使用的优惠券id
        List<Long> claimedCouponIds = userCouponMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getStatus, 1))
                .stream().map(UserCoupon::getCouponId).collect(Collectors.toList());

        if (claimedCouponIds.isEmpty()) return List.of();

        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<Coupon>()
                .in(Coupon::getId, claimedCouponIds)
                .eq(Coupon::getStatus, 1)
                .lt(Coupon::getStartTime, now)
                .gt(Coupon::getEndTime, now);

        // 如果传了金额，过滤满减门槛
        if (amount != null) {
            wrapper.le(Coupon::getMinAmount, amount);
        }

        return list(wrapper);
    }

    @Override
    @Transactional
    public void useCoupon(Long userId, Long couponId, Long orderId) {
        UserCoupon uc = userCouponMapper.selectOne(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getCouponId, couponId)
                        .eq(UserCoupon::getStatus, 1));
        if (uc == null) {
            throw new BadRequestException("优惠券不存在或已使用");
        }
        uc.setStatus(0);
        uc.setUsedOrderId(orderId);
        uc.setUseTime(LocalDateTime.now());
        userCouponMapper.updateById(uc);
    }
}
