import { defineStore } from 'pinia';
import { login as loginApi } from '@/api/user';

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    userId: (state) => state.userInfo?.userId,
  },
  actions: {
    async login(loginForm) {
      const data = await loginApi(loginForm);
      this.token = data.token;
      this.userInfo = { userId: data.userId, username: data.username, balance: data.balance };
      localStorage.setItem('token', data.token);
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo));
    },
    logout() {
      this.token = '';
      this.userInfo = null;
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
    },
  },
});
