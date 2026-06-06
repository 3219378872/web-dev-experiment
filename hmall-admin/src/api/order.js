import request from './request';
export const getOrders = (params) => request.get('/admin/orders', { params });
export const getOrderById = (id) => request.get(`/admin/orders/${id}`);
export const shipOrder = (id, trackingNumber) =>
  request.put(`/admin/orders/${id}/ship`, null, { params: { trackingNumber } });
export const updateOrderStatus = (id, status) =>
  request.put(`/admin/orders/${id}/status`, null, { params: { status } });
export const refundAuditOrder = (id, data) => request.put(`/admin/orders/${id}/refund-audit`, data);
export const exportOrders = (params) =>
  request.get('/admin/orders/export', { params, responseType: 'blob' });
export const getOrderLogistics = (id) => request.get(`/orders/${id}/logistics`);
