/**
 * 商品图片解析工具。
 *
 * 后端 ItemDTO 仅有单个 `image` 字段（逗号分隔可承载多图），没有独立的详情图列表。
 * 这里把 `image` 解析为图片 URL 数组，供主图 / 缩略图 / 详情图渲染。
 * 没有真实图片时返回空数组，由组件降级到占位色块（保持原型观感且不杜撰图片）。
 */

/** 将后端 image 字段解析为去重后的 URL 列表（不可变，不修改入参） */
export function parseItemImages(image) {
  if (!image || typeof image !== 'string') return [];
  const urls = image
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean);
  return [...new Set(urls)];
}

/** 主图：图片列表的第一张；无图返回 null */
export function mainImage(images) {
  return images && images.length ? images[0] : null;
}
