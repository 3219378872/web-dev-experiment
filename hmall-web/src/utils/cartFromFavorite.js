// 从收藏记录构造加入购物车的请求体。
// 返回 { ok, payload, reason } —— 不可变地生成新对象，便于在视图与测试间复用。

export function buildCartPayloadFromFavorite(fav) {
  if (!fav || fav.itemId == null) {
    return { ok: false, payload: null, reason: 'missing-item' };
  }
  return {
    ok: true,
    payload: {
      itemId: fav.itemId,
      num: 1,
      spec: fav.spec || '',
    },
    reason: null,
  };
}
