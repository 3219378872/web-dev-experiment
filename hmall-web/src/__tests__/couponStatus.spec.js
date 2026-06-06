import { describe, expect, it, vi } from 'vitest';
import { mapCoupon, mapMyCoupon } from '@/utils/couponStatus';

describe('couponStatus', () => {
  it('maps user coupon status 1 to unused when the coupon is still valid', () => {
    vi.setSystemTime(new Date('2026-06-06T10:00:00Z'));

    expect(
      mapMyCoupon({
        id: 1,
        name: '未使用券',
        discountType: 1,
        discountValue: 1000,
        minAmount: 5000,
        endTime: '2026-06-08T00:00:00',
        userCouponStatus: 1,
      }).state
    ).toBe('use');

    vi.useRealTimers();
  });

  it('maps user coupon status 0 to used even when the coupon end time has passed', () => {
    vi.setSystemTime(new Date('2026-06-06T10:00:00Z'));

    expect(
      mapMyCoupon({
        id: 2,
        name: '已使用券',
        discountType: 1,
        discountValue: 500,
        endTime: '2026-06-01T00:00:00',
        userCouponStatus: 0,
      }).state
    ).toBe('used');

    vi.useRealTimers();
  });

  it('marks unused expired user coupons as expired', () => {
    vi.setSystemTime(new Date('2026-06-06T10:00:00Z'));

    expect(
      mapMyCoupon({
        id: 3,
        name: '过期券',
        discountType: 2,
        discountValue: 90,
        endTime: '2026-06-01T00:00:00',
        userCouponStatus: 1,
      }).state
    ).toBe('expired');

    vi.useRealTimers();
  });

  it('keeps claimable coupon state empty for the public coupon lists', () => {
    expect(
      mapCoupon({
        id: 4,
        name: '平台券',
        discountType: 1,
        discountValue: 300,
        endTime: '2026-06-08T00:00:00',
      }).state
    ).toBe('');
  });
});
