import request from './request';
export const login = (data) => request.post('/users/login', data);
