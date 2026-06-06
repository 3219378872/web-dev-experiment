export const orderTabs = [
  { label: '全部订单', value: '' },
  { label: '待付款', value: '1', badge: '1', badgeColor: 'var(--price)' },
  { label: '待发货', value: '2' },
  { label: '待收货', value: '3', badge: '1', badgeColor: 'var(--info)' },
  { label: '待评价', value: '4' },
  { label: '退款/售后', value: '6' },
];

const STATUS_TEXT = {
  1: '待付款',
  2: '待发货',
  3: '卖家已发货',
  4: '交易完成',
  5: '交易关闭',
  6: '退款处理中',
};

const STATUS_CLASS = {
  1: 'wait-pay',
  2: 'wait-send',
  3: 'wait-recv',
  4: 'done',
  5: 'closed',
  6: 'refund',
};

export function orderStatusText(status) {
  return STATUS_TEXT[status] || '-';
}

export function orderStatusClass(status) {
  return STATUS_CLASS[status] || '';
}

export function canRequestRefund(status) {
  return status === 2 || status === 3;
}

export function primaryReviewItemRoute(order) {
  const itemId = order?.details?.find((detail) => detail?.itemId)?.itemId;
  return itemId ? { path: `/item/${itemId}`, query: { tab: 'reviews' } } : null;
}
