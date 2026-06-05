import request from './request';
export const createOrder = (data) => request.post('/orders', data);
export const getOrders = (params) => request.get('/orders', { params });
export const getOrderById = (id) => request.get(`/orders/${id}`);
export const cancelOrder = (id) => request.put(`/orders/${id}/cancel`);
export const confirmOrder = (id) => request.put(`/orders/${id}/confirm`);
export const refundOrder = (id) => request.post(`/orders/${id}/refund`);
export const deleteOrder = (id) => request.delete(`/orders/${id}`);
export const getCoupons = () => request.get('/coupons');
export const claimCoupon = (id) => request.post(`/coupons/${id}/claim`);
export const getMyCoupons = () => request.get('/my-coupons');

// 支付相关：先创建支付单，再执行余额支付
// POST /pay-orders body={bizOrderNo, amount, payChannelCode, payType, orderInfo} → 返回支付单id(string)
export const createPayOrder = (data) => request.post('/pay-orders', data);
// POST /pay-orders/{id} body={pw} → void
export const payOrderByBalance = (payOrderId, pw) =>
  request.post(`/pay-orders/${payOrderId}`, { pw });
