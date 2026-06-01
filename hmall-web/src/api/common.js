import request from './request';
export const submitFeedback = (data) => request.post('/feedbacks', data);
export const getNotifications = () => request.get('/notifications/active');
export const addFavorite = (itemId) => request.post('/favorites', null, { params: { itemId } });
export const removeFavorite = (itemId) => request.delete(`/favorites/${itemId}`);
export const getFavorites = () => request.get('/favorites');
export const checkFavorite = (itemId) => request.get(`/favorites/check/${itemId}`);
