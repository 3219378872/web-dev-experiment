import { describe, it, expect } from 'vitest';
import {
  BALANCE_PAYMENT_TYPE,
  paymentOptions,
  deliverySummary,
  invoiceSummary,
  buildOrderPayload,
} from '@/utils/checkoutOptions';

describe('checkoutOptions', () => {
  it('only exposes the backend-supported balance payment option', () => {
    expect(paymentOptions).toEqual([
      { value: BALANCE_PAYMENT_TYPE, label: '余额支付', icon: '余', color: '#FF9D00' },
    ]);
  });

  it('documents delivery and invoice as non-editable checkout summaries', () => {
    expect(deliverySummary.method).toBe('普通快递');
    expect(deliverySummary.description).toContain('后端实时计算');
    expect(invoiceSummary.description).toContain('暂不支持在线开票');
  });

  it('builds an order payload using only OrderFormDTO-supported fields', () => {
    const payload = buildOrderPayload({
      addressId: 9,
      freight: 1500,
      couponId: 3,
      items: [
        { id: 101, num: 2 },
        { itemId: 202, id: 999, num: 1 },
      ],
      remark: 'do not submit',
      invoice: 'do not submit',
      deliveryTime: 'do not submit',
    });

    expect(payload).toEqual({
      addressId: 9,
      paymentType: 3,
      freight: 1500,
      details: [
        { itemId: 101, num: 2 },
        { itemId: 202, num: 1 },
      ],
      couponId: 3,
    });
    expect(payload).not.toHaveProperty('remark');
    expect(payload).not.toHaveProperty('invoice');
    expect(payload).not.toHaveProperty('deliveryTime');
  });

  it('omits couponId when no coupon is selected', () => {
    expect(buildOrderPayload({ addressId: 1, items: [], couponId: null })).toEqual({
      addressId: 1,
      paymentType: 3,
      freight: 0,
      details: [],
      couponId: undefined,
    });
  });
});
