import request from './request';
export const getAddresses = () => request.get('/addresses');
export const saveAddress = (data) => request.post('/addresses', data);
export const updateAddress = (id, data) => request.put(`/addresses/${id}`, data);
export const deleteAddress = (id) => request.delete(`/addresses/${id}`);
export const setDefaultAddress = (id) => request.put(`/addresses/${id}/default`);
