package com.hmall.controller;

import com.hmall.common.domain.R;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.po.Coupon;
import com.hmall.service.ICouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CouponController {

    private final ICouponService couponService;

    @GetMapping("/coupons")
    public List<Coupon> available() {
        return couponService.getAvailableCoupons();
    }

    @PostMapping("/coupons/{id}/claim")
    public R<Void> claim(@PathVariable Long id) {
        couponService.claimCoupon(UserContext.getUser(), id);
        return R.ok();
    }

    @GetMapping("/my-coupons")
    public List<Coupon> myCoupons() {
        return couponService.getUserCoupons(UserContext.getUser());
    }

    @PostMapping("/admin/coupons")
    public R<Void> create(@RequestBody Coupon coupon) {
        coupon.setCreateTime(LocalDateTime.now());
        coupon.setRemainingStock(coupon.getTotalStock());
        couponService.save(coupon);
        return R.ok();
    }

    @DeleteMapping("/admin/coupons/{id}")
    public R<Void> delete(@PathVariable Long id) {
        couponService.removeById(id);
        return R.ok();
    }
}
