/**
 * 商品评价筛选工具（issue #128）。
 *
 * 后端 /items/{id}/reviews 返回当前商品全部评价（含 rating 与 images 字段），
 * 但不支持按"有图/好评/中评/差评"筛选。这里在前端对已加载的评价做过滤，
 * 与后端 rating 口径一致：好评 4-5 星、中评 3 星、差评 1-2 星。
 *
 * 所有函数不可变：返回新数组，不修改入参。
 */

export const REVIEW_FILTERS = [
  { key: 'all', label: '全部' },
  { key: 'image', label: '有图' },
  { key: 'good', label: '好评' },
  { key: 'medium', label: '中评' },
  { key: 'bad', label: '差评' },
];

/** 该评价是否带图 */
export function hasImages(review) {
  const imgs = review?.images;
  if (!imgs) return false;
  if (Array.isArray(imgs)) return imgs.some((u) => String(u).trim());
  return String(imgs)
    .split(',')
    .some((u) => u.trim());
}

/** 单条评价是否匹配指定筛选 key */
export function matchesFilter(review, key) {
  const rating = Number(review?.rating) || 0;
  switch (key) {
    case 'image':
      return hasImages(review);
    case 'good':
      return rating >= 4;
    case 'medium':
      return rating === 3;
    case 'bad':
      return rating > 0 && rating <= 2;
    case 'all':
    default:
      return true;
  }
}

/** 按筛选 key 过滤评价列表 */
export function filterReviews(reviews, key) {
  const list = Array.isArray(reviews) ? reviews : [];
  if (!key || key === 'all') return [...list];
  return list.filter((r) => matchesFilter(r, key));
}

/** 统计每个筛选维度的评价数量 */
export function countReviews(reviews) {
  const list = Array.isArray(reviews) ? reviews : [];
  return REVIEW_FILTERS.reduce((acc, f) => {
    acc[f.key] = list.filter((r) => matchesFilter(r, f.key)).length;
    return acc;
  }, {});
}
