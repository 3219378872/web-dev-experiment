import request from './request';
export const login = (data) => request.post('/users/login', data);
export const register = (data) => request.post('/users/register', data);
export const sendCode = (email) => request.post('/users/send-code', null, { params: { email } });
export const resetPassword = (data) => request.post('/users/reset-password', data);
export const updateProfile = (data) => request.put('/users/profile', data);
