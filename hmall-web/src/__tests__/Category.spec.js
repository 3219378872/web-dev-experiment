import { describe, it, expect } from 'vitest';

// 纯逻辑单元测试：筛选与计数
// 直接测试从 Category.vue 提取出来的核心逻辑函数，无需 mount 组件

/** 计算每个分类的商品数量（客户端计算，匹配 category 名称字段） */
function computeCategoriesWithCount(categories, items) {
  return categories.map((cat) => ({
    ...cat,
    count: items.filter((i) => i.category === cat.name).length,
  }));
}

/** 按分类名称过滤商品列表 */
function filterItemsByCategory(items, activeCatId, categories) {
  if (activeCatId == null) return [...items];
  const catName = categories.find((c) => c.id === activeCatId)?.name ?? null;
  if (!catName) return [];
  return items.filter((i) => i.category === catName);
}

/** 对商品列表进行排序（不可变，返回新数组） */
function sortItems(list, key) {
  const sorted = [...list];
  switch (key) {
    case 'sold':
      sorted.sort((a, b) => (b.sold || 0) - (a.sold || 0));
      break;
    case 'priceAsc':
      sorted.sort((a, b) => (a.price || 0) - (b.price || 0));
      break;
    case 'priceDesc':
      sorted.sort((a, b) => (b.price || 0) - (a.price || 0));
      break;
    case 'new':
      sorted.sort((a, b) => (b.createTime || '').localeCompare(a.createTime || ''));
      break;
    default:
      break;
  }
  return sorted;
}

// 测试数据

const categories = [
  { id: 1, name: '电子产品' },
  { id: 2, name: '服装' },
  { id: 3, name: '食品' },
];

const items = [
  { id: 101, name: '手机', category: '电子产品', price: 3999, sold: 500, createTime: '2026-01-15' },
  { id: 102, name: '平板', category: '电子产品', price: 2999, sold: 200, createTime: '2026-02-10' },
  { id: 103, name: 'T恤', category: '服装', price: 99, sold: 1000, createTime: '2026-03-05' },
  { id: 104, name: '饼干', category: '食品', price: 15, sold: 300, createTime: '2026-01-20' },
];

// computeCategoriesWithCount

describe('computeCategoriesWithCount', () => {
  it('为每个分类计算正确的商品数量', () => {
    const result = computeCategoriesWithCount(categories, items);
    expect(result).toHaveLength(3);
    expect(result.find((c) => c.name === '电子产品')?.count).toBe(2);
    expect(result.find((c) => c.name === '服装')?.count).toBe(1);
    expect(result.find((c) => c.name === '食品')?.count).toBe(1);
  });

  it('没有商品的分类 count 为 0', () => {
    const result = computeCategoriesWithCount([{ id: 99, name: '玩具' }], items);
    expect(result[0].count).toBe(0);
  });

  it('商品列表为空时所有分类 count 均为 0', () => {
    const result = computeCategoriesWithCount(categories, []);
    result.forEach((c) => expect(c.count).toBe(0));
  });

  it('分类列表为空时返回空数组', () => {
    const result = computeCategoriesWithCount([], items);
    expect(result).toHaveLength(0);
  });

  it('保留分类的原始字段（id、name）', () => {
    const result = computeCategoriesWithCount(categories, items);
    const electronics = result.find((c) => c.id === 1);
    expect(electronics?.id).toBe(1);
    expect(electronics?.name).toBe('电子产品');
  });

  it('按 category 名称精确匹配，不做模糊匹配', () => {
    const itemsWithPartialName = [{ id: 200, name: '电子', category: '电子' }];
    const result = computeCategoriesWithCount(categories, itemsWithPartialName);
    expect(result.find((c) => c.name === '电子产品')?.count).toBe(0);
  });
});

// filterItemsByCategory

describe('filterItemsByCategory', () => {
  it('activeCatId 为 null 时返回全部商品', () => {
    const result = filterItemsByCategory(items, null, categories);
    expect(result).toHaveLength(items.length);
  });

  it('选中"电子产品"时只返回电子产品商品', () => {
    const result = filterItemsByCategory(items, 1, categories);
    expect(result).toHaveLength(2);
    result.forEach((i) => expect(i.category).toBe('电子产品'));
  });

  it('选中"服装"时只返回服装商品', () => {
    const result = filterItemsByCategory(items, 2, categories);
    expect(result).toHaveLength(1);
    expect(result[0].name).toBe('T恤');
  });

  it('activeCatId 对应分类不存在时返回空数组', () => {
    const result = filterItemsByCategory(items, 999, categories);
    expect(result).toHaveLength(0);
  });

  it('不改变原始数组（不可变）', () => {
    const original = [...items];
    filterItemsByCategory(items, 1, categories);
    expect(items).toEqual(original);
  });

  it('切换分类时正确切换结果', () => {
    const electronics = filterItemsByCategory(items, 1, categories);
    const clothing = filterItemsByCategory(items, 2, categories);
    expect(electronics.every((i) => i.category === '电子产品')).toBe(true);
    expect(clothing.every((i) => i.category === '服装')).toBe(true);
  });
});

// sortItems

describe('sortItems', () => {
  it('default 排序不改变顺序', () => {
    const result = sortItems(items, 'default');
    expect(result.map((i) => i.id)).toEqual(items.map((i) => i.id));
  });

  it('sold 排序按销量从高到低', () => {
    const result = sortItems(items, 'sold');
    expect(result[0].sold).toBeGreaterThanOrEqual(result[1].sold ?? 0);
    expect(result[1].sold).toBeGreaterThanOrEqual(result[2].sold ?? 0);
  });

  it('priceAsc 排序按价格从低到高', () => {
    const result = sortItems(items, 'priceAsc');
    for (let i = 0; i < result.length - 1; i++) {
      expect(result[i].price ?? 0).toBeLessThanOrEqual(result[i + 1].price ?? 0);
    }
  });

  it('priceDesc 排序按价格从高到低', () => {
    const result = sortItems(items, 'priceDesc');
    for (let i = 0; i < result.length - 1; i++) {
      expect(result[i].price ?? 0).toBeGreaterThanOrEqual(result[i + 1].price ?? 0);
    }
  });

  it('new 排序按 createTime 降序', () => {
    const result = sortItems(items, 'new');
    for (let i = 0; i < result.length - 1; i++) {
      const a = result[i].createTime ?? '';
      const b = result[i + 1].createTime ?? '';
      expect(a.localeCompare(b)).toBeGreaterThanOrEqual(0);
    }
  });

  it('不改变原始数组（不可变）', () => {
    const original = [...items];
    sortItems(items, 'priceAsc');
    expect(items).toEqual(original);
  });
});
