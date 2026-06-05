import request from './request';

export const getDashboardSummary = () => request.get('/admin/dashboard/summary');
export const getDashboardTrend = (days = 7) =>
  request.get('/admin/dashboard/trend', { params: { days } });
export const getDashboardCategoryShare = () => request.get('/admin/dashboard/category-share');
export const getDashboardTopItems = () => request.get('/admin/dashboard/top-items');
export const getDashboardTodo = () => request.get('/admin/dashboard/todo');
export const getDashboardLatestOrders = () => request.get('/admin/dashboard/latest-orders');
