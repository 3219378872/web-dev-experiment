import request from './request';
import { normalizePageParams } from './pageParams';
export const getItems = (params) =>
  request.get('/items/page', { params: normalizePageParams(params) });
export const getItemById = (id) => request.get(`/items/${id}`);
export const searchItems = (params) => request.get('/search/list', { params });
export const getReviews = (itemId) => request.get(`/items/${itemId}/reviews`);
export const submitReview = (itemId, data) => request.post(`/items/${itemId}/reviews`, data);
export const getCategories = () => request.get('/categories');
export const getBanners = () => request.get('/banners');
export const getActiveSeckills = (params) => request.get('/seckill/active', { params });
