export const BALANCE_PAYMENT_TYPE = 3;

export const paymentOptions = Object.freeze([
  { value: BALANCE_PAYMENT_TYPE, label: '余额支付', icon: '余', color: '#FF9D00' },
]);

export const deliverySummary = Object.freeze({
  method: '普通快递',
  description: '运费由后端实时计算并随订单提交',
});

export const invoiceSummary = Object.freeze({
  description: '当前订单暂不支持在线开票',
});

export function buildOrderPayload({ addressId, items, couponId, freight = 0 }) {
  return {
    addressId,
    paymentType: BALANCE_PAYMENT_TYPE,
    freight,
    details: (items || []).map((i) => ({
      itemId: i.itemId || i.id,
      num: i.num,
    })),
    couponId: couponId || undefined,
  };
}
