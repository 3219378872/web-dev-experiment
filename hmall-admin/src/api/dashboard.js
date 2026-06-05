import request from './request';

export const getSummary = () => request.get('/admin/dashboard/summary');
export const getTrend = (days) => request.get('/admin/dashboard/trend', { params: { days } });
export const getCategoryShare = () => request.get('/admin/dashboard/category-share');
export const getTopItems = () => request.get('/admin/dashboard/top-items');
export const getTodo = () => request.get('/admin/dashboard/todo');
export const getLatestOrders = () => request.get('/admin/dashboard/latest-orders');
