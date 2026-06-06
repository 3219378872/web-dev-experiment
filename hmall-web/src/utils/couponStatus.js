export function isExpiredCoupon(coupon, now = new Date()) {
  return Boolean(coupon?.endTime && new Date(coupon.endTime) < now);
}

export function couponDisplayState(coupon, fallbackState = '', now = new Date()) {
  if (fallbackState === 'used' || coupon?.userCouponStatus === 0) return 'used';
  const state = coupon?.userCouponStatus === 1 ? 'use' : fallbackState;
  if (state !== '' && isExpiredCoupon(coupon, now)) return 'expired';
  return state;
}

export function mapCoupon(coupon, state = '', now = new Date()) {
  const isPercent = coupon.discountType === 2;
  const minYuan = coupon.minAmount ? Math.round(coupon.minAmount / 100) : 0;
  return {
    id: coupon.id,
    value: isPercent
      ? (coupon.discountValue || 0) / 10
      : Math.round((coupon.discountValue || 0) / 100),
    unit: isPercent ? '折' : '',
    condition: minYuan > 0 ? `满${minYuan}可用` : '无门槛',
    name: coupon.name || '优惠券',
    scope: coupon.description || '全场商品可用',
    expiry: coupon.endTime ? String(coupon.endTime).slice(0, 10) : '长期有效',
    state: couponDisplayState(coupon, state, now),
  };
}

export function mapMyCoupon(coupon, now = new Date()) {
  return mapCoupon(coupon, '', now);
}
