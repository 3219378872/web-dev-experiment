package com.hmall.api.client;

import com.hmall.api.dto.CouponDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("trade-service")
public interface CouponClient {

    @GetMapping("/coupons")
    List<CouponDTO> getAvailableCoupons();

    @PostMapping("/coupons/{id}/claim")
    void claimCoupon(@PathVariable("id") Long id);

    @GetMapping("/my-coupons")
    List<CouponDTO> getMyCoupons();
}
