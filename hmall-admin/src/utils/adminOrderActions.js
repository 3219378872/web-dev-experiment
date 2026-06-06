const tabStatusMap = {
  pendingPay: 1,
  pendingShip: 2,
  shipped: 3,
  done: 4,
  closed: 5,
  refund: 6,
};

export function adminOrderQueryParams({ page, size, orderId, userId, activeTab }) {
  const params = { page, size };
  const normalizedOrderId = String(orderId || '').trim();
  const normalizedUserId = String(userId || '').trim();
  if (normalizedOrderId) params.orderId = normalizedOrderId;
  if (normalizedUserId) params.userId = normalizedUserId;
  if (tabStatusMap[activeTab]) params.status = tabStatusMap[activeTab];
  return params;
}

export function canShipOrder(order) {
  return Number(order?.status) === 2;
}

export function canCancelOrder(order) {
  return [1, 2].includes(Number(order?.status));
}

export function canRequestRefundAudit(order) {
  return Number(order?.status) === 6;
}

export function paymentTypeText(paymentType) {
  return { 1: '支付宝', 2: '微信', 3: '余额' }[paymentType] || '-';
}

export function mapLogisticsTrace(trace) {
  const rawTime = trace?.traceTime ? String(trace.traceTime) : '';
  return {
    title: trace?.node || '-',
    description: trace?.description || '',
    time: rawTime ? rawTime.slice(0, 16).replace('T', ' ') : '',
  };
}
