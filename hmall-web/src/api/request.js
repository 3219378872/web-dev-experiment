import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.authorization = token;
  }
  return config;
});

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      const hadToken = !!localStorage.getItem('token');
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      // 仅当本地原本有 token（会话过期）时才跳转登录页；
      // 游客未登录时静默失败，避免打断浏览漏斗
      if (hadToken) {
        router.push('/login');
      }
    }
    ElMessage.error(error.response?.data?.msg || '请求失败');
    return Promise.reject(error);
  }
);

export default request;
