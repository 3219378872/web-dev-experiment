import request from './request';
export const submitFeedback = (data) => request.post('/feedbacks', data);
export const sendCustomerMessage = (data) => request.post('/messages', data);
export const getMyFeedbacks = (params) => request.get('/my-feedbacks', { params });
export const getNotifications = () => request.get('/notifications/active');
export const addFavorite = (itemId) => request.post('/favorites', null, { params: { itemId } });
export const removeFavorite = (itemId) => request.delete(`/favorites/${itemId}`);
export const getFavorites = () => request.get('/favorites');
export const checkFavorite = (itemId) => request.get(`/favorites/check/${itemId}`);
export const uploadImage = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};
export const getFaqs = () => request.get('/faqs');
