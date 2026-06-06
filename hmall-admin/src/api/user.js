import request from './request';
export const getUsers = (params) => request.get('/admin/users', { params });
export const getUserById = (id) => request.get(`/admin/users/${id}`);
export const toggleUserStatus = (id, data) => request.put(`/admin/users/${id}/status`, data);
export const getAdminProfile = () => request.get('/admin/profile');
export const updateAdminProfile = (data) => request.put('/admin/profile', data);
export const getAdminPermissions = () => request.get('/admin/profile/permissions');
