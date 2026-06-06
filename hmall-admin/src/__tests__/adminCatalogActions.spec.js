import { describe, expect, it } from 'vitest';
import {
  bannerPreviewStyle,
  buildItemPayload,
  catalogItemTabs,
  nextSort,
} from '@/utils/adminCatalogActions';

describe('adminCatalogActions', () => {
  it('does not expose unsupported import or pending-review catalog actions', () => {
    const tabs = catalogItemTabs({ total: 8, onSale: 5, offSale: 2, lowStock: 1 });

    expect(tabs.map((t) => t.value)).toEqual(['all', 'on', 'off', 'low']);
    expect(tabs.some((t) => t.value === 'pending')).toBe(false);
  });

  it('builds item payload only from fields persisted by the backend contract', () => {
    const payload = buildItemPayload(
      {
        name: '测试商品',
        category: '食品饮料',
        brand: '好集严选 HAOJI',
        price: '19.90',
        stock: '12',
        status: 1,
        spec: '',
        subTitle: '不会持久化',
        marketPrice: '29.90',
        stockAlert: '5',
        detail: '不会持久化',
        shippingTemplate: '全国包邮',
        isSeckill: true,
        isRecommend: false,
        isSevenDayReturn: true,
      },
      ['/files/items/a.jpg'],
      [{ name: '颜色', values: ['红色'] }]
    );

    expect(payload).toEqual({
      name: '测试商品',
      category: '食品饮料',
      brand: '好集严选 HAOJI',
      price: 1990,
      stock: 12,
      status: 1,
      spec: JSON.stringify([{ name: '颜色', values: ['红色'] }]),
      image: '/files/items/a.jpg',
      isSeckill: true,
      isRecommend: false,
      isSevenDayReturn: true,
    });
    expect(payload).not.toHaveProperty('subTitle');
    expect(payload).not.toHaveProperty('marketPrice');
    expect(payload).not.toHaveProperty('images');
    expect(payload).not.toHaveProperty('detail');
    expect(payload).not.toHaveProperty('shippingTemplate');
  });

  it('renders banner previews from real imageUrl with a fallback only when missing', () => {
    expect(bannerPreviewStyle({ imageUrl: '/files/banners/a.jpg' })).toBe(
      'background:url(/files/banners/a.jpg) center/cover'
    );
    expect(bannerPreviewStyle({})).toContain('linear-gradient');
  });

  it('computes adjacent banner sort values for move controls', () => {
    expect(nextSort({ sort: 3 }, 'up')).toBe(2);
    expect(nextSort({ sort: 3 }, 'down')).toBe(4);
    expect(nextSort({ sort: 0 }, 'up')).toBe(0);
  });
});
