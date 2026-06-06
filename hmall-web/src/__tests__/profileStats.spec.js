import { describe, it, expect } from 'vitest';
import { countOrderPage, mergeOrderStats, hasNextPage, ORDER_STATUS } from '@/utils/profileStats';

describe('countOrderPage', () => {
  it('统计单页待付款/待收货数量', () => {
    const list = [
      { status: ORDER_STATUS.PENDING_PAY },
      { status: ORDER_STATUS.PENDING_PAY },
      { status: ORDER_STATUS.PENDING_RECEIVE },
      { status: 5 },
    ];
    expect(countOrderPage(list)).toEqual({ pendingPay: 2, pendingRecv: 1 });
  });

  it('空列表-计数为 0', () => {
    expect(countOrderPage([])).toEqual({ pendingPay: 0, pendingRecv: 0 });
  });

  it('非数组入参-按空处理', () => {
    expect(countOrderPage(null)).toEqual({ pendingPay: 0, pendingRecv: 0 });
    expect(countOrderPage(undefined)).toEqual({ pendingPay: 0, pendingRecv: 0 });
  });
});

describe('mergeOrderStats', () => {
  it('累加两页统计结果', () => {
    const acc = { pendingPay: 2, pendingRecv: 1 };
    const page = { pendingPay: 3, pendingRecv: 4 };
    expect(mergeOrderStats(acc, page)).toEqual({ pendingPay: 5, pendingRecv: 5 });
  });

  it('入参缺失时按 0 处理', () => {
    expect(mergeOrderStats(null, null)).toEqual({ pendingPay: 0, pendingRecv: 0 });
  });

  it('不修改入参（不可变）', () => {
    const acc = Object.freeze({ pendingPay: 1, pendingRecv: 1 });
    const page = Object.freeze({ pendingPay: 1, pendingRecv: 1 });
    expect(mergeOrderStats(acc, page)).toEqual({ pendingPay: 2, pendingRecv: 2 });
  });
});

describe('hasNextPage', () => {
  it('当前页小于总页数-有下一页', () => {
    expect(hasNextPage(1, 3)).toBe(true);
  });
  it('当前页等于总页数-无下一页', () => {
    expect(hasNextPage(3, 3)).toBe(false);
  });
  it('总页数缺省按 1 处理', () => {
    expect(hasNextPage(1, undefined)).toBe(false);
  });
});

describe('多页汇总模拟', () => {
  it('超过单页（100）订单跨页累加，结果不丢失', () => {
    // 模拟 2 页，每页含若干待付款/待收货
    const pages = [
      Array.from({ length: 100 }, (_, i) => ({ status: i < 60 ? 1 : 3 })),
      Array.from({ length: 40 }, (_, i) => ({ status: i < 10 ? 1 : 3 })),
    ];
    let agg = { pendingPay: 0, pendingRecv: 0 };
    pages.forEach((p) => {
      agg = mergeOrderStats(agg, countOrderPage(p));
    });
    expect(agg.pendingPay).toBe(70);
    expect(agg.pendingRecv).toBe(70);
  });
});
