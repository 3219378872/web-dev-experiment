import request from './request';
export const getItems = (params) => request.get('/admin/items', { params });
export const getItemById = (id) => request.get(`/items/${id}`);
export const saveItem = (data) => request.post('/items', data);
export const updateItem = (id, data) => request.put(`/admin/items/${id}`, data);
export const updateItemStatus = (id, status) =>
  request.put(`/admin/items/${id}/status`, null, { params: { status } });
export const deleteItem = (id) => request.delete(`/admin/items/${id}`);
export const getCategories = () => request.get('/categories');
export const saveCategory = (data) => request.post('/categories', data);
export const updateCategory = (id, data) => request.put(`/categories/${id}`, data);
export const deleteCategory = (id) => request.delete(`/categories/${id}`);
export const getReviews = (params) => request.get('/admin/reviews', { params });
export const deleteReview = (id) => request.delete(`/admin/reviews/${id}`);
