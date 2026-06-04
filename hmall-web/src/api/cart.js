import request from './request';
export const getCart = () => request.get('/carts');
export const addToCart = (data) => request.post('/carts', data);
// 后端为 PUT /carts，body 携带 id + num（无 /carts/{id} 路径）
export const updateCartItem = (id, data) => request.put('/carts', { id, ...data });
export const deleteCartItem = (id) => request.delete(`/carts/${id}`);
