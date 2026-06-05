import { describe, it, expect } from 'vitest';
import {
  hasImages,
  matchesFilter,
  filterReviews,
  countReviews,
  REVIEW_FILTERS,
} from '@/utils/reviewFilters';

const reviews = [
  { id: 1, rating: 5, images: 'a.png,b.png', content: '好' },
  { id: 2, rating: 4, images: '', content: '不错' },
  { id: 3, rating: 3, images: 'c.png', content: '一般' },
  { id: 4, rating: 2, images: null, content: '差' },
  { id: 5, rating: 1, images: '', content: '很差' },
];

describe('hasImages', () => {
  it('字符串多图判定为有图', () => {
    expect(hasImages({ images: 'a.png,b.png' })).toBe(true);
  });
  it('数组图片判定为有图', () => {
    expect(hasImages({ images: ['a.png'] })).toBe(true);
  });
  it('空 / null 判定为无图', () => {
    expect(hasImages({ images: '' })).toBe(false);
    expect(hasImages({ images: null })).toBe(false);
    expect(hasImages({})).toBe(false);
  });
});

describe('matchesFilter', () => {
  it('好评为 4-5 星', () => {
    expect(matchesFilter({ rating: 5 }, 'good')).toBe(true);
    expect(matchesFilter({ rating: 4 }, 'good')).toBe(true);
    expect(matchesFilter({ rating: 3 }, 'good')).toBe(false);
  });
  it('中评为 3 星', () => {
    expect(matchesFilter({ rating: 3 }, 'medium')).toBe(true);
    expect(matchesFilter({ rating: 4 }, 'medium')).toBe(false);
  });
  it('差评为 1-2 星', () => {
    expect(matchesFilter({ rating: 2 }, 'bad')).toBe(true);
    expect(matchesFilter({ rating: 1 }, 'bad')).toBe(true);
    expect(matchesFilter({ rating: 3 }, 'bad')).toBe(false);
    expect(matchesFilter({ rating: 0 }, 'bad')).toBe(false);
  });
  it('all 永远匹配', () => {
    expect(matchesFilter({ rating: 1 }, 'all')).toBe(true);
  });
});

describe('filterReviews', () => {
  it('all 返回全部副本', () => {
    const out = filterReviews(reviews, 'all');
    expect(out).toHaveLength(5);
    expect(out).not.toBe(reviews);
  });
  it('有图只返回带图评价', () => {
    expect(filterReviews(reviews, 'image').map((r) => r.id)).toEqual([1, 3]);
  });
  it('好评返回 4-5 星', () => {
    expect(filterReviews(reviews, 'good').map((r) => r.id)).toEqual([1, 2]);
  });
  it('中评返回 3 星', () => {
    expect(filterReviews(reviews, 'medium').map((r) => r.id)).toEqual([3]);
  });
  it('差评返回 1-2 星', () => {
    expect(filterReviews(reviews, 'bad').map((r) => r.id)).toEqual([4, 5]);
  });
  it('入参非数组返回空数组', () => {
    expect(filterReviews(null, 'all')).toEqual([]);
  });
  it('不修改原数组', () => {
    const original = [...reviews];
    filterReviews(reviews, 'good');
    expect(reviews).toEqual(original);
  });
});

describe('countReviews', () => {
  it('为每个筛选维度计数', () => {
    const counts = countReviews(reviews);
    expect(counts.all).toBe(5);
    expect(counts.image).toBe(2);
    expect(counts.good).toBe(2);
    expect(counts.medium).toBe(1);
    expect(counts.bad).toBe(2);
  });
  it('覆盖全部筛选 key', () => {
    const counts = countReviews(reviews);
    REVIEW_FILTERS.forEach((f) => expect(counts[f.key]).toBeTypeOf('number'));
  });
  it('空列表全部为 0', () => {
    const counts = countReviews([]);
    REVIEW_FILTERS.forEach((f) => expect(counts[f.key]).toBe(0));
  });
});
