import request from './request'
export const getCart = () => request.get('/carts')
export const addToCart = (data) => request.post('/carts', data)
export const updateCartItem = (id, data) => request.put(`/carts/${id}`, data)
export const deleteCartItem = (id) => request.delete(`/carts/${id}`)
