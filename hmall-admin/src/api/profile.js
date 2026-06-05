import request from './request';

export const updateAdminProfile = (data) => request.put('/admin/profile', data);
export const getAdminPermissions = () => request.get('/admin/profile/permissions');
