import { describe, it, expect } from 'vitest';
import { normalizePageParams } from '@/api/pageParams';

describe('normalizePageParams', () => {
  it('把 page/size 映射为 pageNo/pageSize', () => {
    const out = normalizePageParams({ page: 2, size: 10 });
    expect(out.pageNo).toBe(2);
    expect(out.pageSize).toBe(10);
    expect(out.page).toBeUndefined();
    expect(out.size).toBeUndefined();
  });

  it('保留 sortBy / isAsc 等其它字段', () => {
    const out = normalizePageParams({ page: 1, size: 5, sortBy: 'sold', isAsc: false });
    expect(out.sortBy).toBe('sold');
    expect(out.isAsc).toBe(false);
  });

  it('已使用 pageNo/pageSize 时不被 page/size 覆盖', () => {
    const out = normalizePageParams({ pageNo: 3, pageSize: 50, page: 1, size: 10 });
    expect(out.pageNo).toBe(3);
    expect(out.pageSize).toBe(50);
  });

  it('不修改入参（不可变）', () => {
    const input = { page: 1, size: 10 };
    normalizePageParams(input);
    expect(input).toEqual({ page: 1, size: 10 });
  });

  it('入参为 undefined 时原样返回', () => {
    expect(normalizePageParams(undefined)).toBeUndefined();
  });

  it('缺少 page/size 时不注入 undefined 字段', () => {
    const out = normalizePageParams({ sortBy: 'create_time' });
    expect('pageNo' in out).toBe(false);
    expect('pageSize' in out).toBe(false);
  });
});
