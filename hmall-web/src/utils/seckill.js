/**
 * 秒杀活动数据解析工具（issue #127）。
 *
 * 后端 SeckillVO 字段：itemId, itemName, itemImage, seckillPrice, originalPrice,
 * startTime, endTime, stock, sold, stockPercent, status。
 * 首页秒杀区与 FlashSale.vue 共用同一解析口径，避免再用普通商品列表切片冒充秒杀。
 *
 * 不可变：返回新对象，不修改入参。
 */

const GLYPH_MAP = {
  手机数码: '▣',
  家用电器: '▤',
  服饰鞋包: '◈',
  美妆个护: '✿',
  食品生鲜: '◉',
  家居家装: '▦',
  母婴玩具: '❀',
  运动户外: '◐',
};

/** 计算已抢百分比：优先后端 stockPercent，其次 sold/stock 推算 */
export function seckillPercent(x) {
  if (x?.stockPercent != null) return x.stockPercent;
  if (x?.percent != null) return x.percent;
  const sold = x?.sold || 0;
  const stock = x?.stock || 1;
  return Math.min(100, Math.round((sold / stock) * 100));
}

/** 将后端秒杀项映射为前端展示结构 */
export function mapSeckillItem(x) {
  const id = x?.itemId || x?.id || 0;
  return {
    id,
    name: x?.itemName || x?.name || '秒杀商品',
    image: x?.itemImage || x?.image || '',
    category: x?.category || '好物',
    price: x?.seckillPrice || 0,
    originalPrice: x?.originalPrice || x?.price || 0,
    stock: x?.stock || 0,
    sold: x?.sold || 0,
    percent: seckillPercent(x),
    status: x?.status,
    shade: `s${(id % 8) + 1}`,
    glyph: GLYPH_MAP[x?.category] || '◆',
  };
}
