// 个人中心订单统计纯逻辑：跨分页汇总待付款/待收货计数。
// 订单状态约定：1=待付款，3=待收货（与 OrderList 一致）。

export const ORDER_STATUS = Object.freeze({
  PENDING_PAY: 1,
  PENDING_RECEIVE: 3,
});

/** 统计单页订单中待付款/待收货数量（不可变，返回新对象）。 */
export function countOrderPage(list) {
  const orders = Array.isArray(list) ? list : [];
  return {
    pendingPay: orders.filter((o) => o.status === ORDER_STATUS.PENDING_PAY).length,
    pendingRecv: orders.filter((o) => o.status === ORDER_STATUS.PENDING_RECEIVE).length,
  };
}

/** 累加分页统计结果（不可变）。 */
export function mergeOrderStats(acc, page) {
  return {
    pendingPay: (acc?.pendingPay || 0) + (page?.pendingPay || 0),
    pendingRecv: (acc?.pendingRecv || 0) + (page?.pendingRecv || 0),
  };
}

/** 是否还有下一页需要拉取。 */
export function hasNextPage(currentPage, totalPages) {
  return currentPage < (totalPages || 1);
}
