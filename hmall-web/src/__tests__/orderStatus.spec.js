import { describe, expect, it } from 'vitest';
import {
  canRequestRefund,
  orderStatusClass,
  orderStatusText,
  orderTabs,
  primaryReviewItemRoute,
} from '@/utils/orderStatus';

describe('orderStatus', () => {
  it('uses backend refund status 6 for the refund/after-sales tab', () => {
    expect(orderTabs.find((tab) => tab.label === '退款/售后')?.value).toBe('6');
  });

  it('labels status 6 as refund processing', () => {
    expect(orderStatusText(6)).toBe('退款处理中');
    expect(orderStatusClass(6)).toBe('refund');
  });

  it('only allows refund requests from paid or shipped orders', () => {
    expect(canRequestRefund(2)).toBe(true);
    expect(canRequestRefund(3)).toBe(true);
    expect(canRequestRefund(1)).toBe(false);
    expect(canRequestRefund(4)).toBe(false);
    expect(canRequestRefund(5)).toBe(false);
    expect(canRequestRefund(6)).toBe(false);
  });

  it('routes completed order reviews to the first item detail review tab when item id exists', () => {
    expect(primaryReviewItemRoute({ details: [{ itemId: 101 }] })).toEqual({
      path: '/item/101',
      query: { tab: 'reviews' },
    });
    expect(primaryReviewItemRoute({ details: [] })).toBeNull();
  });
});
