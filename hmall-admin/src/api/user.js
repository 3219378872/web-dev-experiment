import request from './request'
export const getUsers = (params) => request.get('/admin/users', { params })
export const toggleUserStatus = (id) => request.put(`/admin/users/${id}/status`)
