import request from './request';
export const getItems = (params) => request.get('/items/page', { params });
export const getItemById = (id) => request.get(`/items/${id}`);
export const searchItems = (params) => request.get('/search/list', { params });
export const getReviews = (itemId) => request.get(`/items/${itemId}/reviews`);
export const submitReview = (itemId, data) => request.post(`/items/${itemId}/reviews`, data);
export const getCategories = () => request.get('/categories');
