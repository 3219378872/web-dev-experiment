import request from './request';
export const getItems = (params) => request.get('/admin/items', { params });
export const getItemById = (id) => request.get(`/items/${id}`);
export const saveItem = (data) => request.post('/items', data);
export const updateItem = (id, data) => request.put(`/admin/items/${id}`, data);
export const updateItemStatus = (id, status) =>
  request.put(`/admin/items/${id}/status`, null, { params: { status } });
export const batchUpdateItemStatus = (ids, status) =>
  request.put('/admin/items/status', { ids, status });
export const deleteItem = (id) => request.delete(`/admin/items/${id}`);
export const batchDeleteItems = (ids) => request.delete('/admin/items', { data: { ids } });
export const getItemStats = () => request.get('/admin/items/stats');
export const getCategories = () => request.get('/categories');
export const getAdminCategories = () => request.get('/admin/categories');
export const saveCategory = (data) => request.post('/categories', data);
export const updateCategory = (id, data) => request.put(`/categories/${id}`, data);
export const deleteCategory = (id) => request.delete(`/categories/${id}`);
export const getReviews = (params) => request.get('/admin/reviews', { params });
export const deleteReview = (id) => request.delete(`/admin/reviews/${id}`);

// Banner APIs
export const getBanners = (params) => request.get('/admin/banners', { params });
export const saveBanner = (data) => request.post('/admin/banners', data);
export const updateBanner = (id, data) => request.put(`/admin/banners/${id}`, data);
export const deleteBanner = (id) => request.delete(`/admin/banners/${id}`);
export const updateBannerStatus = (id, status) =>
  request.put(`/admin/banners/${id}/status`, status);
export const updateBannerSort = (id, sort) => request.put(`/admin/banners/${id}/sort`, sort);
