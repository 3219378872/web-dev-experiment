package com.hmall.controller;

import com.hmall.common.domain.R;
import com.hmall.common.utils.BeanUtils;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.po.Coupon;
import com.hmall.domain.vo.CouponVO;
import com.hmall.service.ICouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Api(tags = "优惠券接口")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CouponController {

    private final ICouponService couponService;

    @ApiOperation("可领取的优惠券列表")
    @GetMapping("/coupons")
    public List<Coupon> available() {
        return couponService.getAvailableCoupons();
    }

    @ApiOperation("领取优惠券")
    @PostMapping("/coupons/{id}/claim")
    public R<Void> claim(@PathVariable Long id) {
        couponService.claimCoupon(UserContext.getUser(), id);
        return R.ok();
    }

    @ApiOperation("我的优惠券列表")
    @GetMapping("/my-coupons")
    public List<Coupon> myCoupons() {
        return couponService.getUserCoupons(UserContext.getUser());
    }

    @ApiOperation("我的可用优惠券（按订单金额过滤）")
    @GetMapping("/my-coupons/available")
    public List<CouponVO> availableForAmount(
            @RequestParam(value = "amount", required = false) Integer amount) {
        List<Coupon> coupons = couponService.getAvailableCouponsForAmount(UserContext.getUser(), amount);
        return BeanUtils.copyList(coupons, CouponVO.class);
    }

    @ApiOperation("创建优惠券")
    @PostMapping("/admin/coupons")
    public R<Void> create(@RequestBody Coupon coupon) {
        coupon.setCreateTime(LocalDateTime.now());
        coupon.setRemainingStock(coupon.getTotalStock());
        couponService.save(coupon);
        return R.ok();
    }

    @ApiOperation("删除优惠券")
    @DeleteMapping("/admin/coupons/{id}")
    public R<Void> delete(@PathVariable Long id) {
        couponService.removeById(id);
        return R.ok();
    }
}
