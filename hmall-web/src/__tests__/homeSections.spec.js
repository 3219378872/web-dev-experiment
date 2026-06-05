import { describe, it, expect } from 'vitest';
import { takeTagged, buildPromoItems } from '@/utils/homeSections';

const items = [
  { id: 1, name: 'a', sold: 100 },
  { id: 2, name: 'b', sold: 90, isRecommend: true },
  { id: 3, name: 'c', sold: 80, isRecommend: true },
  { id: 4, name: 'd', sold: 70 },
  { id: 5, name: 'e', sold: 60 },
  { id: 6, name: 'f', sold: 50 },
];

describe('takeTagged', () => {
  it('取前 n 条并打标签', () => {
    const out = takeTagged(items, '热卖', 3);
    expect(out).toHaveLength(3);
    out.forEach((x) => expect(x.tag).toBe('热卖'));
  });
  it('保留已有 tag', () => {
    const out = takeTagged([{ id: 9, tag: '专属' }], '热卖', 1);
    expect(out[0].tag).toBe('专属');
  });
  it('非数组返回空数组', () => {
    expect(takeTagged(null, '热卖', 3)).toEqual([]);
  });
  it('不修改原始对象（不可变）', () => {
    const input = [{ id: 1, name: 'a' }];
    takeTagged(input, '热卖', 1);
    expect(input[0].tag).toBeUndefined();
  });
});

describe('buildPromoItems', () => {
  it('优先返回 isRecommend 的商品', () => {
    const out = buildPromoItems(items, 5);
    expect(out.map((x) => x.id)).toEqual([2, 3]);
    out.forEach((x) => expect(x.tag).toBe('促销'));
  });
  it('无推荐商品时降级为切片', () => {
    const noRec = items.map((x) => ({ ...x, isRecommend: false }));
    const out = buildPromoItems(noRec, 3);
    expect(out.map((x) => x.id)).toEqual([1, 2, 3]);
  });
  it('限制为 n 条', () => {
    const allRec = items.map((x) => ({ ...x, isRecommend: true }));
    expect(buildPromoItems(allRec, 2)).toHaveLength(2);
  });
  it('非数组返回空数组', () => {
    expect(buildPromoItems(undefined)).toEqual([]);
  });
});
