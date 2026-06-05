import { describe, it, expect } from 'vitest';
import { seckillPercent, mapSeckillItem } from '@/utils/seckill';

describe('seckillPercent', () => {
  it('优先使用后端 stockPercent', () => {
    expect(seckillPercent({ stockPercent: 42 })).toBe(42);
  });
  it('其次使用 percent', () => {
    expect(seckillPercent({ percent: 30 })).toBe(30);
  });
  it('否则按 sold/stock 推算并封顶 100', () => {
    expect(seckillPercent({ sold: 5, stock: 10 })).toBe(50);
    expect(seckillPercent({ sold: 20, stock: 10 })).toBe(100);
  });
  it('缺字段不抛错', () => {
    expect(seckillPercent({})).toBe(0);
    expect(seckillPercent(null)).toBe(0);
  });
});

describe('mapSeckillItem', () => {
  it('映射后端 SeckillVO 字段', () => {
    const out = mapSeckillItem({
      itemId: 7,
      itemName: '秒杀手机',
      seckillPrice: 199900,
      originalPrice: 299900,
      stock: 100,
      sold: 30,
      stockPercent: 30,
      category: '手机数码',
      itemImage: 'p.png',
    });
    expect(out.id).toBe(7);
    expect(out.name).toBe('秒杀手机');
    expect(out.price).toBe(199900);
    expect(out.originalPrice).toBe(299900);
    expect(out.percent).toBe(30);
    expect(out.image).toBe('p.png');
    expect(out.glyph).toBe('▣');
    expect(out.shade).toBe('s8');
  });
  it('缺失字段时提供降级值', () => {
    const out = mapSeckillItem({});
    expect(out.id).toBe(0);
    expect(out.name).toBe('秒杀商品');
    expect(out.glyph).toBe('◆');
    expect(out.image).toBe('');
  });
});
