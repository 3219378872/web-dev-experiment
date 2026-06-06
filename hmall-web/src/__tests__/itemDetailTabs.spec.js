import { describe, expect, it } from 'vitest';
import { productTabFromRouteQuery } from '@/utils/itemDetailTabs';

describe('itemDetailTabs', () => {
  it('opens the review tab from route query when the order review link targets item detail', () => {
    expect(productTabFromRouteQuery({ tab: 'reviews' })).toBe('reviews');
  });

  it('falls back to detail for unsupported tab query values', () => {
    expect(productTabFromRouteQuery({ tab: 'feedback' })).toBe('detail');
    expect(productTabFromRouteQuery({})).toBe('detail');
  });
});
