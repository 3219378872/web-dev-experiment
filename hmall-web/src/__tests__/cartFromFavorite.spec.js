import { describe, it, expect } from 'vitest';
import { buildCartPayloadFromFavorite } from '@/utils/cartFromFavorite';

describe('buildCartPayloadFromFavorite', () => {
  it('正常收藏记录-生成 itemId/num/spec 负载', () => {
    const { ok, payload, reason } = buildCartPayloadFromFavorite({ itemId: 101, spec: '红色' });
    expect(ok).toBe(true);
    expect(reason).toBeNull();
    expect(payload).toEqual({ itemId: 101, num: 1, spec: '红色' });
  });

  it('无 spec 时 spec 默认为空字符串', () => {
    const { payload } = buildCartPayloadFromFavorite({ itemId: 5 });
    expect(payload.spec).toBe('');
    expect(payload.num).toBe(1);
  });

  it('itemId 为 0 仍视为有效（合法主键）', () => {
    const { ok, payload } = buildCartPayloadFromFavorite({ itemId: 0 });
    expect(ok).toBe(true);
    expect(payload.itemId).toBe(0);
  });

  it('缺少 itemId-返回 ok=false 与 reason', () => {
    expect(buildCartPayloadFromFavorite({ spec: 'x' })).toEqual({
      ok: false,
      payload: null,
      reason: 'missing-item',
    });
  });

  it('入参为 null/undefined-返回 ok=false', () => {
    expect(buildCartPayloadFromFavorite(null).ok).toBe(false);
    expect(buildCartPayloadFromFavorite(undefined).ok).toBe(false);
  });

  it('不修改入参对象（不可变）', () => {
    const fav = Object.freeze({ itemId: 9, spec: 'a' });
    expect(() => buildCartPayloadFromFavorite(fav)).not.toThrow();
  });
});
