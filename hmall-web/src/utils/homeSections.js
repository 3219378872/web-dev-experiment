/**
 * 首页分区数据构建工具（issue #127）。
 *
 * 热门：后端按 sold 倒序分页（pageNo/pageSize + sortBy=sold）。
 * 新品：后端按 create_time 倒序分页。
 * 促销：后端 ItemDTO 暴露 isRecommend 标记，优先取被推荐到首页的商品；
 *      若当前页无推荐商品（样例数据未配 flag），降级为列表切片，保证分区不空。
 *
 * 所有函数不可变：返回新数组并附加 tag，不修改入参。
 */

function withTag(item, tag) {
  return { ...item, tag: item.tag || tag };
}

/** 取列表前 n 条并打标签 */
export function takeTagged(list, tag, n) {
  const arr = Array.isArray(list) ? list : [];
  return arr.slice(0, n).map((x) => withTag(x, tag));
}

/**
 * 促销分区：优先 isRecommend === true 的商品；不足时用其余商品补齐到 n 条。
 */
export function buildPromoItems(list, n = 5) {
  const arr = Array.isArray(list) ? list : [];
  const recommended = arr.filter((x) => x.isRecommend === true);
  const base = recommended.length ? recommended : arr;
  return base.slice(0, n).map((x) => withTag(x, '促销'));
}
