export function catalogItemTabs(stats = {}) {
  return [
    { label: '全部', value: 'all', badge: stats.total || 0 },
    { label: '在售中', value: 'on', badge: stats.onSale || 0 },
    { label: '已下架', value: 'off', badge: stats.offSale || 0 },
    { label: '库存预警', value: 'low', badge: stats.lowStock || 0 },
  ];
}

function toFen(value) {
  const parsed = parseFloat(value);
  return Number.isFinite(parsed) ? Math.round(parsed * 100) : 0;
}

function toInteger(value) {
  const parsed = parseInt(value, 10);
  return Number.isFinite(parsed) ? parsed : 0;
}

export function buildItemPayload(form, imageList = [], specGroups = []) {
  const spec = specGroups.length > 0 ? JSON.stringify(specGroups) : form.spec || '';
  return {
    name: form.name,
    category: form.category,
    brand: form.brand,
    price: toFen(form.price),
    stock: toInteger(form.stock),
    status: form.status,
    spec,
    image: imageList[0] || '',
    isSeckill: Boolean(form.isSeckill),
    isRecommend: Boolean(form.isRecommend),
    isSevenDayReturn: Boolean(form.isSevenDayReturn),
  };
}

export function bannerPreviewStyle(banner = {}) {
  if (banner.imageUrl) {
    return `background:url(${banner.imageUrl}) center/cover`;
  }
  return 'background:linear-gradient(120deg,#FF7A45,#D62E10)';
}

export function nextSort(banner, direction) {
  const current = Number(banner?.sort || 0);
  if (direction === 'up') return Math.max(0, current - 1);
  return current + 1;
}
