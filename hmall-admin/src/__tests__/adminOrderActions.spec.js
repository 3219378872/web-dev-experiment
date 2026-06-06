import { describe, expect, it } from 'vitest';
import {
  adminOrderQueryParams,
  canCancelOrder,
  canRequestRefundAudit,
  canShipOrder,
  mapLogisticsTrace,
  paymentTypeText,
} from '@/utils/adminOrderActions';

describe('adminOrderActions', () => {
  it('builds backend query params for order id, user id, status tab, and pagination', () => {
    expect(
      adminOrderQueryParams({
        page: 2,
        size: 20,
        orderId: '9001',
        userId: '42',
        activeTab: 'refund',
      })
    ).toEqual({ page: 2, size: 20, orderId: '9001', userId: '42', status: 6 });
  });

  it('only exposes shipping, cancellation, and refund audit actions for supported states', () => {
    expect(canShipOrder({ status: 2 })).toBe(true);
    expect(canShipOrder({ status: 3 })).toBe(false);

    expect(canCancelOrder({ status: 1 })).toBe(true);
    expect(canCancelOrder({ status: 2 })).toBe(true);
    expect(canCancelOrder({ status: 3 })).toBe(false);
    expect(canCancelOrder({ status: 4 })).toBe(false);
    expect(canCancelOrder({ status: 6 })).toBe(false);

    expect(canRequestRefundAudit({ status: 6 })).toBe(true);
    expect(canRequestRefundAudit({ status: 2 })).toBe(false);
  });

  it('maps OrderVO.paymentType instead of a legacy payType field', () => {
    expect(paymentTypeText(1)).toBe('支付宝');
    expect(paymentTypeText(2)).toBe('微信');
    expect(paymentTypeText(3)).toBe('余额');
    expect(paymentTypeText(undefined)).toBe('-');
  });

  it('maps real logistics traces from backend fields', () => {
    expect(
      mapLogisticsTrace({
        node: '已发货',
        description: '商品已交给快递',
        traceTime: '2026-06-06T10:30:00',
      })
    ).toEqual({
      title: '已发货',
      description: '商品已交给快递',
      time: '2026-06-06 10:30',
    });
  });
});
